package com.tle.core.portal.service.ext;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.dytech.edge.common.valuebean.ValidationError;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.impl.PortletRecentContrib;
import com.tle.core.guice.Bind;
import com.tle.core.portal.dao.PortletDao;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.core.portal.service.PortletServiceExtension;
import com.tle.core.user.CurrentInstitution;

/**
 * @author aholland
 */
@Bind
@Singleton
public class RecentContribPortletService implements PortletServiceExtension
{
	private static final String KEY_ERROR_VALIDATION_AGE = "com.tle.core.portal.recent.age.error"; //$NON-NLS-1$

	@Inject
	private PortletDao portletDao;

	@Override
	public void loadExtra(Portlet portlet)
	{
		PortletRecentContrib contrib = portletDao.findAnyById(PortletRecentContrib.class, portlet.getId());
		portlet.setExtraData(contrib);
	}

	@Override
	public void edit(Portlet to, PortletEditingBean from)
	{
		PortletRecentContrib contrib = (PortletRecentContrib) from.getExtraData();
		contrib.setId(to.getId());
		contrib.setPortlet(to);
		portletDao.mergeAny(contrib);
	}

	@Override
	public void add(Portlet portlet)
	{
		PortletRecentContrib contrib = (PortletRecentContrib) portlet.getExtraData();
		contrib.setPortlet(portlet);
		contrib.setId(portlet.getId());
		portletDao.saveAny(contrib);
	}

	@Override
	public void deleteExtra(Portlet portlet)
	{
		PortletRecentContrib contrib = portletDao.findAnyById(PortletRecentContrib.class, portlet.getId());
		portletDao.deleteAny(contrib);
	}

	@Override
	@SuppressWarnings("nls")
	public void changeUserId(String fromUserId, String toUserId)
	{
		DetachedCriteria dc = DetachedCriteria.forClass(PortletRecentContrib.class);
		dc.add(Restrictions.eq("userId", fromUserId));
		dc.createCriteria("portlet").add(Restrictions.eq("institution", CurrentInstitution.get()));

		List<PortletRecentContrib> prcs = portletDao.findAnyByCriteria(dc, null, null);
		for( PortletRecentContrib prc : prcs )
		{
			prc.setUserId(toUserId);
			portletDao.saveAny(prc);
		}
	}

	@Override
	public void doValidation(PortletEditingBean newPortlet, List<ValidationError> errors)
	{
		PortletRecentContrib recent = (PortletRecentContrib) newPortlet.getExtraData();
		if( recent.getAgeDays() < 0 )
		{
			errors.add(new ValidationError("age", CurrentLocale.get(KEY_ERROR_VALIDATION_AGE))); //$NON-NLS-1$
		}

	}
}
