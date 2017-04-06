package com.tle.web.scheduler;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.scheduler.SchedulerService;
import com.tle.core.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@Bind
@SuppressWarnings("nls")
public class ScheduledTasksDebug extends OneColumnLayout<ScheduledTasksDebug.Model>
{
	@Inject
	private SchedulerService schedulerService;
	@EventFactory
	private EventGenerator events;

	private void ensureSystemUser()
	{
		if( !CurrentUser.getUserState().isSystem() )
		{
			throw new AccessDeniedException("You need to be the system administrator");
		}
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		ensureSystemUser();

		Model model = getModel(info);
		List<HtmlLinkState> links = model.getLinks();
		List<String> ids = schedulerService.getAllSchedulerIds();
		SubmitValuesFunction runTask = events.getSubmitValuesFunction("runTask");
		for( String extId : ids )
		{
			HtmlLinkState link = new HtmlLinkState(new TextLabel(extId));
			link.setClickHandler(new OverrideHandler(runTask, extId));
			links.add(link);
		}
		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "debug.ftl", this));
	}

	@EventHandlerMethod
	public void runTask(SectionInfo info, String extId)
	{
		schedulerService.executeTaskNow(extId);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(new TextLabel("Task debug page"));
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model extends OneColumnLayout.OneColumnLayoutModel
	{
		private List<HtmlLinkState> links = new ArrayList<HtmlLinkState>();

		public List<HtmlLinkState> getLinks()
		{
			return links;
		}

		public void setLinks(List<HtmlLinkState> links)
		{
			this.links = links;
		}
	}
}
