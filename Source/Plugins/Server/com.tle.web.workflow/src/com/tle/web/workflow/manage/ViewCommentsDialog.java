package com.tle.web.workflow.manage;

import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.WorkflowService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.workflow.tasks.comments.ViewCommentsSection;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class ViewCommentsDialog extends EquellaDialog<ViewCommentsDialog.Model>
{
	@PlugKey("comdialog.title")
	private static Label LABEL_TITLE;

	@Inject
	private WorkflowService workflowService;

	@Inject
	private ViewCommentsSection viewCommentsSection;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	public ViewCommentsDialog()
	{
		setAjax(true);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(viewCommentsSection, id);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@EventHandlerMethod
	public void openComments(SectionInfo info, ItemTaskId taskId)
	{
		getModel(info).setTaskId(taskId);
		showDialog(info);
	}

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("openComments");
	}

	@Override
	public Model instantiateDialogModel(SectionInfo info)
	{
		return new Model();
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		List<WorkflowMessage> allMessages = workflowService.getCommentsForTask(getModel(context).getTaskId());
		viewCommentsSection.setMessages(context, allMessages);
		return viewFactory.createResult("comdialog.ftl", this);
	}

	@Override
	public String getWidth()
	{
		return "600px";
	}

	public static class Model extends DialogModel
	{
		@Bookmarked
		private ItemTaskId taskId;

		public ItemTaskId getTaskId()
		{
			return taskId;
		}

		public void setTaskId(ItemTaskId taskId)
		{
			this.taskId = taskId;
		}
	}

	public ViewCommentsSection getViewCommentsSection()
	{
		return viewCommentsSection;
	}
}
