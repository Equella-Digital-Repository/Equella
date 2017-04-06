package com.tle.web.viewitem.summary.content;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.events.WorkflowEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;

@NonNullByDefault
@Bind
public class HistoryCommentDialog extends EquellaDialog<HistoryCommentDialog.HistoryCommentModel>
{
	@PlugKey("summary.content.history.comment.title")
	private static Label TITLE_LABEL;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		setAjax(true);
	}

	@Override
	@SuppressWarnings("nls")
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("showComment");
	}

	@EventHandlerMethod
	public void showComment(SectionInfo info, long commentId)
	{
		HistoryCommentModel model = getModel(info);
		model.setCommentId(commentId);
		super.showDialog(info);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return TITLE_LABEL;
	}

	@Nullable
	@Override
	@SuppressWarnings("nls")
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		final long commentId = getModel(context).getCommentId();

		WorkflowStatus ws = ParentViewItemSectionUtils.getItemInfo(context).getWorkflowStatus();
		for( WorkflowEvent we : ws.getEvents() )
		{
			if( we.getId() == commentId )
			{
				return new DivRenderer(new LabelRenderer(new TextLabel(SectionUtils.ent(we.getComment()).replaceAll(
					"\n", "<br>"), true)));
			}
		}

		return null;
	}

	@Override
	@SuppressWarnings("nls")
	public String getWidth()
	{
		return "600px";
	}

	@Override
	public HistoryCommentModel instantiateDialogModel(SectionInfo info)
	{
		return new HistoryCommentModel();
	}

	public static class HistoryCommentModel extends DialogModel
	{
		private long commentId;

		public void setCommentId(long commentId)
		{
			this.commentId = commentId;
		}

		public long getCommentId()
		{
			return commentId;
		}
	}
}
