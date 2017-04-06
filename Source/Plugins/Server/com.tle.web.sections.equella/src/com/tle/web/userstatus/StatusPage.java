package com.tle.web.userstatus;

import java.util.Collection;

import javax.inject.Inject;

import com.dytech.edge.common.valuebean.GroupBean;
import com.dytech.edge.common.valuebean.RoleBean;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.template.Decorations;

@Bind
@SuppressWarnings("nls")
public class StatusPage extends AbstractPrototypeSection<StatusPage.StatusModel> implements HtmlRenderer
{
	@Inject
	private UserService userService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	public static class StatusModel
	{
		private UserState userState;
		private Collection<GroupBean> groups;
		private Collection<RoleBean> roles;

		public UserState getUserState()
		{
			return userState;
		}

		public void setUserState(UserState userState)
		{
			this.userState = userState;
		}

		public Collection<GroupBean> getGroups()
		{
			return groups;
		}

		public void setGroups(Collection<GroupBean> groups)
		{
			this.groups = groups;
		}

		public Collection<RoleBean> getRoles()
		{
			return roles;
		}

		public void setRoles(Collection<RoleBean> roles)
		{
			this.roles = roles;
		}

	}

	@Override
	public Class<StatusModel> getModelClass()
	{
		return StatusModel.class;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations.getDecorations(context).setTitle(new TextLabel("User Status"));

		StatusModel model = getModel(context);
		UserState state = CurrentUser.getUserState();
		model.setUserState(state);
		model.setGroups(userService.getInformationForGroups(state.getUsersGroups()).values());
		model.setRoles(userService.getInformationForRoles(state.getUsersRoles()).values());
		return viewFactory.createResult("debug/statuspage.ftl", context);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	/**
	 * Remove when this is spring 2.5
	 * 
	 * @param userService
	 */
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}
}
