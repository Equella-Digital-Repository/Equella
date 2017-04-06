package com.tle.core.qti.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.exceptions.NotFoundException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.qti.dao.QtiAssessmentTestDao;
import com.tle.core.user.CurrentInstitution;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(QtiAssessmentTestDao.class)
@Singleton
public class QtiAssessmentTestDaoImpl extends GenericInstitionalDaoImpl<QtiAssessmentTest, Long>
	implements
		QtiAssessmentTestDao
{
	public QtiAssessmentTestDaoImpl()
	{
		super(QtiAssessmentTest.class);
	}

	@Override
	public QtiAssessmentTest getByUuid(String uuid)
	{
		final QtiAssessmentTest test = findByUuid(uuid);
		if( test == null )
		{
			throw new NotFoundException("Cannot find test with uuid " + uuid);
		}
		return test;
	}

	@Nullable
	@Override
	public QtiAssessmentTest findByUuid(String uuid)
	{
		final QtiAssessmentTest test = findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("uuid", uuid));
		return test;
	}

	@Override
	public QtiAssessmentTest findByItem(Item item)
	{
		final QtiAssessmentTest test = findByCriteria(Restrictions.eq("item", item));
		return test; // NOSONAR (keeping local var for readability)
	}

	@Override
	public QtiAssessmentTest findByItemId(long itemId)
	{
		final QtiAssessmentTest test = findByCriteria(Restrictions.eq("item.id", itemId));
		return test; // NOSONAR (keeping local var for readability)
	}

	@SuppressWarnings("unchecked")
	private List<QtiAssessmentTest> listAll()
	{
		return getHibernateTemplate().find("FROM QtiAssessmentTest WHERE institution = ?",
			new Object[]{CurrentInstitution.get()});
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void deleteAll()
	{
		for( QtiAssessmentTest test : listAll() )
		{
			delete(test);
			flush();
			clear();
		}
	}
}
