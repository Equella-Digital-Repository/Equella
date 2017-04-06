package com.tle.mets.importerexporters;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.FileInfo;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.mets.MetsIDElementInfo;
import com.tle.web.sections.SectionInfo;

import edu.harvard.hul.ois.mets.BinData;
import edu.harvard.hul.ois.mets.File;
import edu.harvard.hul.ois.mets.helper.MetsElement;
import edu.harvard.hul.ois.mets.helper.MetsIDElement;

/**
 * @author Aaron
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class FileMetsAttachmentImporterExporter extends AbstractMetsAttachmentImportExporter
{
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public boolean canExport(Item item, Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.FILE
			|| attachment.getAttachmentType() == AttachmentType.ZIP;
	}

	@Override
	public List<MetsIDElementInfo<? extends MetsIDElement>> export(SectionInfo info, Item item, Attachment attachment)
	{
		final List<MetsIDElementInfo<? extends MetsIDElement>> res = new ArrayList<MetsIDElementInfo<? extends MetsIDElement>>();

		final FileHandle fileHandle = new ItemFile(item);
		final String filename = attachment.getUrl();
		final FileInfo fileInfo = fileSystemService.getFileInfo(fileHandle, filename);
		res.add(exportBinaryFile(new ItemFile(item), filename, fileInfo.getLength(), attachment.getDescription(),
			"data:" + attachment.getUuid(), attachment.getUuid()));
		return res;
	}

	@Override
	public boolean canImport(File parentElem, MetsElement elem, PropBagEx xmlData, ItemNavigationNode parentNode)
	{
		return idPrefixMatch(elem, "data:");
	}

	@Override
	public void doImport(Item item, FileHandle staging, String targetFolder, File parentElem, MetsElement elem,
		PropBagEx xmlData, ItemNavigationNode parentNode, AttachmentAdder attachmentAdder)
	{
		final BinData data = getFirst(elem.getContent(), BinData.class);
		if( data != null )
		{
			final ImportInfo importInfo = importBinaryFile(data, staging, targetFolder, parentElem.getOWNERID(),
				xmlData);

			final FileAttachment attachment = new FileAttachment();
			populateStandardProperties(attachment, importInfo);

			attachmentAdder.addAttachment(parentNode, attachment, importInfo.getDescription());
		}
	}
}
