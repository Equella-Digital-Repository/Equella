package com.tle.web.viewitem.largeimageviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Pair;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.operations.WorkflowParams;

/**
 * @author Aaron
 */
@Bind
public class StartTilingOperation extends AbstractWorkflowOperation
{
	@Inject
	private LargeImageViewer largeImageViewer;
	@Inject
	private MimeTypeService mimeTypeService;

	@Override
	public boolean execute()
	{
		params.addAfterCommitHook(WorkflowParams.COMMIT_HOOK_PRIORITY_LOW, new Runnable()
		{
			@Override
			public void run()
			{
				final Item item = getItem();
				final Collection<Pair<File, File>> images = new ArrayList<Pair<File, File>>();
				final Iterator<Attachment> it = getAttachments().getIterator(AttachmentType.FILE);
				while( it.hasNext() )
				{
					final FileAttachment fa = (FileAttachment) it.next();
					if( isViewerEnabledForAttachment(fa) )
					{
						final ItemFile itemFile = new ItemFile(item.getItemId());
						final File originalImage = fileSystemService.getExternalFile(itemFile, fa.getFilename());
						final File destFolder = fileSystemService.getExternalFile(
							largeImageViewer.getTileBaseHandle(itemFile, fa.getUrl()), null);
						images.add(new Pair<File, File>(originalImage, destFolder));
					}
				}
				if( !images.isEmpty() )
				{
					largeImageViewer.startTileProcessor(images);
				}
			}
		});

		return false;
	}

	private boolean isViewerEnabledForAttachment(FileAttachment fa)
	{
		final MimeEntry entry = mimeTypeService.getEntryForFilename(fa.getFilename());
		if( entry != null )
		{
			final List<String> enabledList = new ArrayList<String>(mimeTypeService.getListFromAttribute(entry,
				MimeTypeConstants.KEY_ENABLED_VIEWERS, String.class));
			return enabledList.contains(LargeImageViewerConstants.VIEWER_ID);
		}
		return false;
	}
}
