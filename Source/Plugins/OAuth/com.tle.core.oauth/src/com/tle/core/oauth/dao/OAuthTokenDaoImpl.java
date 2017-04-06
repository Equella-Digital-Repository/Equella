package com.tle.core.oauth.dao;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(OAuthTokenDao.class)
@Singleton
public class OAuthTokenDaoImpl extends GenericInstitionalDaoImpl<OAuthToken, Long> implements OAuthTokenDao
{
	public OAuthTokenDaoImpl()
	{
		super(OAuthToken.class);
	}

	@Override
	public OAuthToken getToken(String userId, OAuthClient client)
	{
		// Tempororary hax for #6659
		List<OAuthToken> tokens = findAllByCriteria(Restrictions.eq("userId", userId),
			Restrictions.eq("client", client));
		if( !tokens.isEmpty() )
		{
			return tokens.get(0);
		}
		return null;
	}

	@Override
	public OAuthToken getToken(String tokenData)
	{
		List<OAuthToken> tokens = findAllByCriteria(Restrictions.eq("token", tokenData),
			Restrictions.eq("institution", CurrentInstitution.get()));
		if( !tokens.isEmpty() )
		{
			return tokens.get(0);
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public List<OAuthToken> enumerateAll()
	{
		return getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				final Query query = session.createQuery("from OAuthToken where client.institution = :institution");
				query.setParameter("institution", CurrentInstitution.get());
				query.setCacheable(true);
				query.setReadOnly(true);
				return query.list();
			}
		});
	}

	@Override
	public List<OAuthToken> findAllByClient(OAuthClient client)
	{
		return findAllByCriteria(Restrictions.eq("client", client));
	}

	@Override
	public void deleteAllForUser(final String userId)
	{
		getHibernateTemplate().execute(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				final Query query = session.createQuery("DELETE OAuthToken WHERE userId = :userId"
					+ " AND client IN (From OAuthClient WHERE institution = :institution)");
				query.setParameter("userId", userId);
				query.setParameter("institution", CurrentInstitution.get());
				return query.executeUpdate();
			}
		});
	}

	@Override
	public void changeUserId(final String fromUserId, final String toUserId)
	{
		getHibernateTemplate().execute(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				final Query query = session.createQuery("UPDATE OAuthToken SET userId = :toUserId"
					+ " WHERE userId = :fromUserId AND client IN (FROM OAuthClient WHERE institution = :institution)");
				query.setParameter("toUserId", toUserId);
				query.setParameter("fromUserId", fromUserId);
				query.setParameter("institution", CurrentInstitution.get());
				return query.executeUpdate();
			}
		});
	}
}
