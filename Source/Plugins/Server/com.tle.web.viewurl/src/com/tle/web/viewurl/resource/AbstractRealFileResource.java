package com.tle.web.viewurl.resource;

import com.tle.core.services.FileSystemService;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.Bookmark;
import com.tle.web.stream.ContentStream;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;

public abstract class AbstractRealFileResource extends AbstractWrappedResource
{
	private final String filePath;
	private final String mimeType;
	private final ViewItemUrlFactory urlFactory;
	private final FileSystemService fileSystemService;

	public AbstractRealFileResource(ViewableResource inner, String filePath, String mimeType,
		ViewItemUrlFactory urlFactory, FileSystemService fileSystemService)
	{
		super(inner);

		this.filePath = filePath;
		this.mimeType = mimeType;
		this.urlFactory = urlFactory;
		this.fileSystemService = fileSystemService;
	}

	@Override
	public Bookmark createCanonicalUrl()
	{
		return top.getViewableItem().createStableResourceUrl(filePath);
	}

	@Override
	public ViewItemUrl createDefaultViewerUrl()
	{
		if( getBooleanAttribute(KEY_NO_FILE_PATHS) )
		{
			return super.createDefaultViewerUrl();
		}

		ViewItemUrl vurl = urlFactory.createItemUrl(getInfo(), top.getViewableItem(),
			UrlEncodedString.createFromFilePath(filePath), ViewItemUrl.FLAG_IS_RESOURCE);
		return vurl;
	}

	@Override
	public ContentStream getContentStream()
	{
		return fileSystemService.getContentStream(top.getViewableItem().getFileHandle(), top.getFilepath(),
			top.getMimeType());
	}

	@Override
	public String getMimeType()
	{
		return mimeType;
	}

	@Override
	@SuppressWarnings("nls")
	public ViewAuditEntry getViewAuditEntry()
	{
		return new ViewAuditEntry("file:" + top.getMimeType(), filePath);
	}

	@Override
	public String getFilepath()
	{
		return filePath;
	}

	@Override
	public boolean hasContentStream()
	{
		return true;
	}
}