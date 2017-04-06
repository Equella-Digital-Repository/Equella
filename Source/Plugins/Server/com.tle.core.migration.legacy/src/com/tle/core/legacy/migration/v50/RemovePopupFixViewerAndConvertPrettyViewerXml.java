package com.tle.core.legacy.migration.v50;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class RemovePopupFixViewerAndConvertPrettyViewerXml extends XmlMigrator
{
	private static final String MOV_VIEWER_ID = "movPopupFixViewer";

	private static final String PRETTY_VIEWER_ID = "pretty";
	private static final String FANCY_VIEWER_ID = "fancy";

	@Inject
	private MimeTypeService mimeService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		final SubTemporaryFile folder = new SubTemporaryFile(staging, "mimetypes");
		final List<String> entries = xmlHelper.getXmlFileList(folder);
		for( String entry : entries )
		{
			boolean modified = false;
			final MimeEntry mime = xmlHelper.readXmlFile(folder, entry);

			final String defaultViewer = mime.getAttributes().get(MimeTypeConstants.KEY_DEFAULT_VIEWERID);
			if( !Check.isEmpty(defaultViewer) )
			{
				if( defaultViewer.equals(MOV_VIEWER_ID) )
				{
					mime.getAttributes().put(MimeTypeConstants.KEY_DEFAULT_VIEWERID,
						MimeTypeConstants.VAL_DEFAULT_VIEWERID);
					modified = true;
				}
				else if( defaultViewer.equals(PRETTY_VIEWER_ID) )
				{
					mime.getAttributes().put(MimeTypeConstants.KEY_DEFAULT_VIEWERID, FANCY_VIEWER_ID);
					modified = true;
				}
			}

			final List<String> enabledViewers = mimeService.getListFromAttribute(mime,
				MimeTypeConstants.KEY_ENABLED_VIEWERS, String.class);
			final int oldPrettyIndex = enabledViewers.indexOf(PRETTY_VIEWER_ID);
			if( enabledViewers.contains(MOV_VIEWER_ID) || oldPrettyIndex >= 0 )
			{
				if( oldPrettyIndex >= 0 )
				{
					enabledViewers.set(oldPrettyIndex, FANCY_VIEWER_ID);
				}
				enabledViewers.remove(MOV_VIEWER_ID);

				if( enabledViewers.size() == 0 )
				{
					enabledViewers.add(MimeTypeConstants.VAL_DEFAULT_VIEWERID);
				}
				mimeService.setListAttribute(mime, MimeTypeConstants.KEY_ENABLED_VIEWERS, enabledViewers);
				modified = true;
			}

			if( modified )
			{
				xmlHelper.writeXmlFile(folder, entry, mime);
			}
		}
	}
}
