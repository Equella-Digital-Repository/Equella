package com.tle.web.cloneormove.model;

import com.tle.beans.entity.Schema;
import com.tle.web.sections.annotations.Bookmarked;

/**
 * @author aholland
 */
public class CloneOrMoveModel
{
	@Bookmarked
	private boolean hideClone;
	@Bookmarked
	private boolean hideCloneNoAttachments;
	@Bookmarked
	private boolean hideMove;

	private boolean move;

	private boolean showCloneOptions;
	private boolean allowCollectionChange;
	private String submitLabel;

	private Schema sourceSchema; // purely informational
	private Schema destSchema; // purely informational

	public boolean isShowCloneOptions()
	{
		return showCloneOptions;
	}

	public void setShowCloneOptions(boolean showCloneOptions)
	{
		this.showCloneOptions = showCloneOptions;
	}

	public boolean isAllowCollectionChange()
	{
		return allowCollectionChange;
	}

	public void setAllowCollectionChange(boolean allowCollectionChange)
	{
		this.allowCollectionChange = allowCollectionChange;
	}

	public boolean isSchemaChanged()
	{
		return sourceSchema != null && destSchema != null && !sourceSchema.getUuid().equals(destSchema.getUuid());
	}

	public Schema getSourceSchema()
	{
		return sourceSchema;
	}

	public void setSourceSchema(Schema sourceSchema)
	{
		this.sourceSchema = sourceSchema;
	}

	public Schema getDestSchema()
	{
		return destSchema;
	}

	public void setDestSchema(Schema destSchema)
	{
		this.destSchema = destSchema;
	}

	public boolean isHideClone()
	{
		return hideClone;
	}

	public void setHideClone(boolean hideClone)
	{
		this.hideClone = hideClone;
	}

	public boolean isMove()
	{
		return move;
	}

	public void setMove(boolean move)
	{
		this.move = move;
	}

	public boolean isHideCloneNoAttachments()
	{
		return hideCloneNoAttachments;
	}

	public void setHideCloneNoAttachments(boolean hideCloneNoAttachments)
	{
		this.hideCloneNoAttachments = hideCloneNoAttachments;
	}

	public boolean isHideMove()
	{
		return hideMove;
	}

	public void setHideMove(boolean hideMove)
	{
		this.hideMove = hideMove;
	}

	public String getSubmitLabel()
	{
		return submitLabel;
	}

	public void setSubmitLabel(String submitLabel)
	{
		this.submitLabel = submitLabel;
	}
}
