package com.tle.web.portal.standard.menu;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.web.WebConstants;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.section.MenuContributor;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DashboardMenuContributor implements MenuContributor
{
	private static final Label LABEL = new KeyLabel(ResourcesService.getResourceHelper(DashboardMenuContributor.class)
		.key("menu.dashboard"));
	private static final String ICON_URL = ResourcesService.getResourceHelper(DashboardMenuContributor.class).url(
		"images/menu-icon-dashboard.png");

	@Inject
	private TLEAclManager aclManager;

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		if( aclManager.filterNonGrantedPrivileges(WebConstants.DASHBOARD_PAGE_PRIVILEGE).isEmpty() )
		{
			return Collections.emptyList();
		}

		HtmlLinkState hls = new HtmlLinkState(new SimpleBookmark("home.do"));
		hls.setLabel(LABEL);
		MenuContribution mc = new MenuContribution(hls, ICON_URL, 1, 1);
		return Collections.singletonList(mc);
	}

	@Override
	public void clearCachedData()
	{
		// Nothing is cached
	}
}
