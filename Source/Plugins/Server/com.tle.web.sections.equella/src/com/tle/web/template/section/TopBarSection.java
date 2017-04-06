package com.tle.web.template.section;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.web.WebConstants;
import com.tle.beans.system.AutoLogin;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.web.DebugSettings;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.navigation.TopbarLinkService;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.GenericNamedResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
public class TopBarSection extends AbstractPrototypeSection<TopBarSection.TopBarModel> implements HtmlRenderer
{
	private static final CssInclude FONT = CssInclude.include("http://fonts.googleapis.com/css?family=PT+Sans").make();

	@PlugKey("topbar.link.logout")
	private static Label LOGOUT_LABEL;
	@PlugKey("topbar.link.edituser")
	private static Label EDIT_USER_LABEL;

	@Inject
	private UrlService urlService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private TopbarLinkService linkService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	private Link editUserLink;
	@Component
	private Link logoutLink;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final Decorations decorations = Decorations.getDecorations(context);
		if( !decorations.isBanner() )
		{
			return null;
		}

		final TopBarModel model = getModel(context);
		model.setBadgeLink(urlService.institutionalise(WebConstants.DEFAULT_HOME_PAGE));

		Label title = decorations.getBannerTitle();
		if( title == null )
		{
			title = decorations.getTitle();
		}
		if( title == null )
		{
			if( DebugSettings.isAutoTestMode() || DebugSettings.isDebuggingMode() )
			{
				throw new RuntimeException("Page missing title");
			}
			title = new TextLabel("Untitled page");
		}
		model.setPageTitle(new LabelRenderer(title));

		if( !CurrentUser.isGuest() )
		{
			SimpleBookmark logon = new SimpleBookmark(CurrentInstitution.get() == null
				? "institutions.do?method=logout" : "logon.do?logout=true");
			logoutLink.setBookmark(context, logon);
			logoutLink.setLabel(context, new IconLabel(Icon.OFF, LOGOUT_LABEL));
		}
		else
		{
			logoutLink.setDisplayed(context, false);
		}

		editUserLink.setBookmark(context, new SimpleBookmark("access/user.do"));
		editUserLink.setDisabled(context, !isEditUserDetailsAvailable());
		editUserLink.getState(context).setTitle(EDIT_USER_LABEL);
		editUserLink.setLabel(context, new IconLabel(Icon.USER, new TextLabel(CurrentUser.getUsername())));

		PluginTracker<TopbarLink> topbarLinks = linkService.getTopbarLinks();
		List<SectionRenderable> linkList = new ArrayList<SectionRenderable>();
		for( TopbarLink link : topbarLinks.getBeanList() )
		{
			LinkRenderer renderer = link.getLink();
			if( renderer != null )
			{
				linkList.add(renderer);
			}
		}
		model.setLinks(linkList);
		return new GenericNamedResult("topbar", viewFactory.createResult("topbar.ftl", context));
	}

	private boolean isEditUserDetailsAvailable()
	{
		UserState us = CurrentUser.getUserState();
		if( us.isGuest() || us.isSystem() )
		{
			return false;
		}

		if( us.wasAutoLoggedIn() )
		{
			AutoLogin autoLogin = configService.getProperties(new AutoLogin());
			return !autoLogin.isEditDetailsDisallowed();
		}

		return true;
	}

	public Link getEditUserLink()
	{
		return editUserLink;
	}

	public Link getLogoutLink()
	{
		return logoutLink;
	}

	public CssInclude getFont()
	{
		return FONT;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "topbar";
	}

	@Override
	public Class<TopBarModel> getModelClass()
	{
		return TopBarModel.class;
	}

	public static class TopBarModel
	{
		private SectionRenderable pageTitle;
		private String badgeLink;
		private int taskCount;
		private int notificationCount;
		private List<SectionRenderable> links;

		public int getTaskCount()
		{
			return taskCount;
		}

		public void setTaskCount(int taskCount)
		{
			this.taskCount = taskCount;
		}

		public int getNotificationCount()
		{
			return notificationCount;
		}

		public void setNotificationCount(int notificationCount)
		{
			this.notificationCount = notificationCount;
		}

		public SectionRenderable getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(SectionRenderable pageTitle)
		{
			this.pageTitle = pageTitle;
		}

		public String getBadgeLink()
		{
			return badgeLink;
		}

		public void setBadgeLink(String badgeLink)
		{
			this.badgeLink = badgeLink;
		}

		public List<SectionRenderable> getLinks()
		{
			return links;
		}

		public void setLinks(List<SectionRenderable> links)
		{
			this.links = links;
		}

	}
}
