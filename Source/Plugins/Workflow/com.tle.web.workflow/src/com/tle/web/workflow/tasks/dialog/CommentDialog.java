package com.tle.web.workflow.tasks.dialog;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.workflow.tasks.CurrentTaskSection;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class CommentDialog extends AbstractTaskActionDialog<AbstractTaskActionDialog.AbstractTaskActionDialogModel>
{
	@PlugKey("command.taskaction.comment")
	private static Label LABEL_COMMENT_BUTTON;
	@PlugKey("command.comment.title")
	private static Label LABEL_COMMENTING_TITLE;
	@PlugKey("comments.entermsg")
	private static Label LABEL_ENTERMSG;

	@Override
	public String getDefaultPropertyName()
	{
		return "commentDialog";
	}

	@Override
	protected ButtonRenderer.ButtonType getButtonType()
	{
		return ButtonRenderer.ButtonType.SAVE;
	}

	@Override
	protected Label getButtonLabel()
	{
		return LABEL_COMMENT_BUTTON;
	}

	@Override
	protected CurrentTaskSection.CommentType getActionType()
	{
		return CurrentTaskSection.CommentType.COMMENT;
	}

	@Nullable
	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_COMMENTING_TITLE;
	}

	@Override
	public Label getPostCommentHeading()
	{
		return LABEL_ENTERMSG;
	}

	@Override
	protected Label validate(SectionInfo info)
	{
		return validateHasMessage(info);
	}
}
