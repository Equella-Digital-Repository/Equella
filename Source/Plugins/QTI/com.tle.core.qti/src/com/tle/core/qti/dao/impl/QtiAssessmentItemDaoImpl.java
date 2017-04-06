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
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.exceptions.NotFoundException;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.qti.entity.QtiAssessmentItem;
import com.tle.core.dao.helpers.ScrollableResultsIterator;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.qti.dao.QtiAssessmentItemDao;
import com.tle.core.user.CurrentInstitution;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(QtiAssessmentItemDao.class)
@Singleton
public class QtiAssessmentItemDaoImpl extends GenericInstitionalDaoImpl<QtiAssessmentItem, Long>
	implements
		QtiAssessmentItemDao
{
	public QtiAssessmentItemDaoImpl()
	{
		super(QtiAssessmentItem.class);
	}

	@Override
	public QtiAssessmentItem getByUuid(String uuid)
	{
		final QtiAssessmentItem question = findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("uuid", uuid));
		if( question == null )
		{
			throw new NotFoundException("Cannot find assessment item with uuid " + uuid);
		}
		return question;
	}

	@Override
	public Iterator<QtiAssessmentItem> getIterator()
	{
		final ScrollableResults cinnamonScroll = (ScrollableResults) getHibernateTemplate().execute(
			new HibernateCallback()
			{
				@Override
				public Object doInHibernate(Session session) throws HibernateException, SQLException
				{
					final Query query = session.createQuery("FROM QtiAssessmentItem WHERE institution = :institution");
					query.setParameter("institution", CurrentInstitution.get());
					query.setReadOnly(true);
					return query.scroll(ScrollMode.FORWARD_ONLY);
				}
			});
		return new ScrollableResultsIterator<QtiAssessmentItem>(cinnamonScroll);
	}

	@SuppressWarnings("unchecked")
	private List<QtiAssessmentItem> listAll()
	{
		return getHibernateTemplate().find("FROM QtiAssessmentItem WHERE institution = ?",
			new Object[]{CurrentInstitution.get()});
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void deleteAll()
	{
		for( QtiAssessmentItem test : listAll() )
		{
			delete(test);
			flush();
			clear();
		}
	}
}
