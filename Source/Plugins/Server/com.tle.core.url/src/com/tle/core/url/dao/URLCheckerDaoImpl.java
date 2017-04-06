package com.tle.core.url.dao;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.tle.beans.ReferencedURL;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.services.TaskService;
import com.tle.core.services.impl.AlwaysRunningTask;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.SimpleMessage;
import com.tle.core.services.impl.Task;
import com.tle.core.url.URLCheckerPolicy;
import com.tle.core.user.CurrentInstitution;

@Singleton
@SuppressWarnings("nls")
@Bind(URLCheckerDao.class)
public class URLCheckerDaoImpl extends GenericDaoImpl<ReferencedURL, Long> implements URLCheckerDao
{
	private static final long MSGTIMEOUT = TimeUnit.SECONDS.toMillis(30);

	private Cache<String, ReferencedURL> urlCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
		.build();

	@Inject
	private InstitutionService institutionService;
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private TaskService taskService;

	private String taskId;

	public URLCheckerDaoImpl()
	{
		super(ReferencedURL.class);
	}

	@Override
	@Transactional
	public ReferencedURL retrieveOrCreate(final String url, final boolean httpUrl, final boolean forImport)
	{
		ReferencedURL rurl = (ReferencedURL) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				if( forImport )
				{
					synchronized( urlCache )
					{
						ReferencedURL rurl = urlCache.getIfPresent(url);
						if( rurl == null )
						{
							rurl = loadOrCreate(session, url, httpUrl, forImport);
							ReferencedURL clone = cloneReferencedURL(rurl);
							session.evict(clone);
							urlCache.put(url, clone);
							return rurl;
						}
						ReferencedURL clone = cloneReferencedURL(rurl);
						session.evict(clone);
						return clone;
					}
				}
				return loadOrCreate(session, url, httpUrl, forImport);
			}
		});

		return rurl;
	}

	private ReferencedURL loadOrCreate(Session session, String url, boolean httpUrl, boolean forImport)
	{
		// We do this double-check to avoid calling the global
		// ReferencedUrl task if we can help it.
		ReferencedURL rurl = getFromDb(session, url);
		if( rurl == null )
		{
			if( httpUrl && !forImport )
			{
				taskService.postSynchronousMessage(getTaskId(), new CreateUrlMessage(url, CurrentInstitution.get()
					.getUniqueId()), MSGTIMEOUT);

				rurl = getFromDb(session, url);
				if( rurl == null )
				{
					throw new RuntimeException("URL was not added to DB");
				}
			}
			else
			{
				final Date epoch = new Date(0);
				rurl = new ReferencedURL();
				rurl.setUrl(url);
				rurl.setLastChecked(epoch);
				rurl.setLastIndexed(epoch);
				session.save(rurl);
			}
		}
		return rurl;
	}

	private ReferencedURL cloneReferencedURL(ReferencedURL rurl)
	{
		ReferencedURL r2 = new ReferencedURL();
		r2.setId(rurl.getId());
		r2.setLastChecked(rurl.getLastChecked());
		r2.setLastIndexed(rurl.getLastIndexed());
		r2.setMessage(rurl.getMessage());
		r2.setStatus(rurl.getStatus());
		r2.setSuccess(rurl.isSuccess());
		r2.setTries(rurl.getTries());
		r2.setUrl(rurl.getUrl());
		return r2;
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public Collection<ReferencedURL> getRecheckingBatch(final long startId, final int batchSize)
	{
		return (Collection<ReferencedURL>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Criteria c = session.createCriteria(ReferencedURL.class);

				URLCheckerPolicy.addRequiresCheckingCriteria(c);

				c.add(Restrictions.ge("id", startId));
				c.addOrder(Order.asc("id"));
				c.setMaxResults(batchSize);
				return c.list();
			}
		});
	}

	@Override
	@Transactional
	public void updateWithTransaction(ReferencedURL rurl)
	{
		update(rurl);
	}

	private ReferencedURL getFromDb(Session session, String url)
	{
		return (ReferencedURL) session.createQuery("FROM ReferencedURL WHERE urlHash = :urlHash")
			.setString("urlHash", DigestUtils.md5Hex(url)).setReadOnly(true).uniqueResult();
	}

	public synchronized String getTaskId()
	{
		if( taskId == null )
		{
			taskId = taskService.getGlobalTask(
				new BeanClusteredTask("ReferencedURLCreator", URLCheckerDao.class, "createReferencedURLCreatorTask"),
				MSGTIMEOUT).getTaskId();
		}
		return taskId;
	}

	public Task createReferencedURLCreatorTask()
	{
		// TODO: Now that we're using ZK for clustering, we should use a ZK
		// mutex and build URLs in the same calling thread when necessary rather
		// then this shit-bag approach.
		return new AlwaysRunningTask<SimpleMessage>()
		{
			@Override
			public SimpleMessage waitFor() throws InterruptedException
			{
				return waitForMessage();
			}

			@Override
			public void runTask(final SimpleMessage msg)
			{
				final CreateUrlMessage content = msg.getContents();
				runAs.executeAsSystem(institutionService.getInstitution(content.getInstitutionId()), new Runnable()
				{
					@Override
					public void run()
					{
						sendResponse(msg.getMessageId(), createUrl(content.getUrl()));
						publishStatus();
					}
				});
			}

			@Override
			protected String getTitleKey()
			{
				return null;
			}
		};
	}

	@Transactional
	public Boolean createUrl(final String url)
	{
		return (Boolean) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				if( getFromDb(session, url) != null )
				{
					return false;
				}
				else
				{
					final Date epoch = new Date(0);
					final ReferencedURL rurl = new ReferencedURL();
					rurl.setUrl(url);
					rurl.setLastChecked(epoch);
					rurl.setLastIndexed(epoch);

					session.save(rurl);

					return true;
				}
			}
		});
	}

	public static class CreateUrlMessage implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String url;
		private final long institutionId;

		public CreateUrlMessage(String url, long institutionId)
		{
			this.url = url;
			this.institutionId = institutionId;
		}

		public String getUrl()
		{
			return url;
		}

		public long getInstitutionId()
		{
			return institutionId;
		}
	}
}
