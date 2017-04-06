package com.tle.core.item.edit.impl;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractFileAttachmentEditor;
import com.tle.core.item.edit.attachment.FileAttachmentEditor;

@Bind
public class FileAttachmentEditorImpl extends AbstractFileAttachmentEditor implements FileAttachmentEditor
{
	private FileAttachment fileAttachment;

	@Override
	public void editFilename(String filename)
	{
		if( hasBeenEdited(fileAttachment.getFilename(), filename) )
		{
			updateFileDetails(filename, true);
			fileAttachment.setFilename(filename);
		}
		else if( changeTracker.isForceFileCheck() )
		{
			updateFileDetails(filename, false);
		}
	}

	@Override
	public void editConversion(boolean convertible)
	{
		if( hasBeenEdited(fileAttachment.isConversion(), convertible) )
		{
			fileAttachment.setConversion(convertible);
		}
	}

	@Override
	public boolean canEdit(Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.FILE;
	}

	@Override
	public Attachment newAttachment()
	{
		return new FileAttachment();
	}

	@Override
	public void setAttachment(Attachment attachment)
	{
		this.fileAttachment = (FileAttachment) attachment;
		super.setAttachment(attachment);
	}

	@Override
	protected void setSize(long size)
	{
		fileAttachment.setSize(size);
	}

	@Override
	public void editZipParent(String parentZip)
	{
		if( hasBeenEdited(fileAttachment.getData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID), parentZip) )
		{
			fileAttachment.setData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID, parentZip);
		}
	}
}
