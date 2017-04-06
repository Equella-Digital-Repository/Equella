package com.tle.core.qti.dao.impl;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.qti.entity.QtiItemResult;
import com.tle.core.dao.helpers.ScrollableResultsIterator;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.qti.dao.QtiItemResultDao;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(QtiItemResultDao.class)
@Singleton
public class QtiItemResultDaoImpl extends GenericDaoImpl<QtiItemResult, Long> implements QtiItemResultDao
{
	public QtiItemResultDaoImpl()
	{
		super(QtiItemResult.class);
	}

	private Query getAllQuery(Session session)
	{
		final Query query = session
			.createQuery("SELECT result FROM QtiItemResult result INNER JOIN result.assessmentResult ass WHERE ass.test.institution = :institution");
		query.setParameter("institution", CurrentInstitution.get());
		return query;
	}

	@SuppressWarnings("unchecked")
	private List<QtiItemResult> listAll()
	{
		return (List<QtiItemResult>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				final Query query = getAllQuery(session);
				return query.list();
			}
		});
	}

	@Override
	public Iterator<QtiItemResult> getIterator()
	{
		final ScrollableResults cinnamonScroll = (ScrollableResults) getHibernateTemplate().execute(
			new HibernateCallback()
			{
				@Override
				public Object doInHibernate(Session session) throws HibernateException, SQLException
				{
					final Query query = getAllQuery(session);
					query.setReadOnly(true);
					return query.scroll(ScrollMode.FORWARD_ONLY);
				}
			});
		return new ScrollableResultsIterator<QtiItemResult>(cinnamonScroll);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void deleteAll()
	{
		List<QtiItemResult> listAll = listAll();
		for( QtiItemResult result : listAll )
		{
			delete(result);
			flush();
			clear();
		}
	}
}
