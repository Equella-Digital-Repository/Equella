package com.tle.web.viewitem;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Provider;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTaskId;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.collection.AttachmentConfigConstants;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.SecurityConstants;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.item.ItemService;
import com.tle.core.user.CurrentUser;
import com.tle.core.workflow.operations.StatusOperation;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.login.LogonSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionsController;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.WrappedContentStream;
import com.tle.web.viewitem.service.FileFilterService;
import com.tle.web.viewurl.ViewAuditEntry;

@Bind
@Singleton
public class ItemFilestoreServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	@Inject
	private Provider<StatusOperation> statusOp;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ViewItemAuditor auditor;
	@Inject
	private ItemService itemService;
	@Inject
	private SectionsController controller;
	@Inject
	private ContentStreamWriter contentStreamWriter;
	@Inject
	private FileFilterService fileFilterService;

	private static PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(ItemFilestoreServlet.class);

	@SuppressWarnings("nls")
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String path = request.getPathInfo();
		int firstPath = path.indexOf('/', 1);
		String uuid = path.substring(1, firstPath);
		int secondPath = path.indexOf('/', firstPath + 1);
		String version = path.substring(firstPath + 1, secondPath);
		path = path.substring(secondPath + 1);

		FileHandle fileHandle;
		ItemKey itemId = null;
		try
		{
			if( version.equals("$") )
			{
				fileHandle = new StagingFile(uuid);
			}
			else
			{
				itemId = ItemTaskId.parse(uuid + '/' + version);
				fileHandle = new ItemFile(itemId);
				Set<String> privs = itemService.getCachedPrivileges(itemId);
				if( privs == null )
				{
					StatusOperation statusop = statusOp.get();
					itemService.operation(itemId, statusop);
					privs = statusop.getStatus().getSecurityStatus().getAllowedPrivileges();
				}
				if( privs == null
					|| (!privs.contains(SecurityConstants.VIEW_ITEM) && !privs
						.contains(AttachmentConfigConstants.VIEW_ATTACHMENTS)) )
				{
					String privileges = SecurityConstants.VIEW_ITEM + ", " + AttachmentConfigConstants.VIEW_ATTACHMENTS;
					throw new AccessDeniedException(CurrentLocale.get(urlHelper.key("missingprivileges"), privileges));
				}
				// check restricted attachments (FIXME: needs to cache this,
				// probably by invoking something on item service much like
				// getCachedPrivileges)
				if( !privs.contains(AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS) )
				{
					final IAttachment attachment;
					if( path.contains(FileSystemService.GALLERY_PREVIEW_EXTENSION) )
					{
						String sourcePath = path.replace(FileSystemService.GALLERY_PREVIEW_EXTENSION, "");
						sourcePath = sourcePath.replace(FileSystemService.THUMBS_FOLDER + "/", "");
						attachment = getAttachmentWithDifferentPriv(itemId, sourcePath);
					}
					else
					{
						attachment = getAttachmentWithDifferentPriv(itemId, path);
					}
					if( attachment != null && attachment.isRestricted() )
					{
						throw new AccessDeniedException(CurrentLocale.get(urlHelper.key("missingprivileges"),
							AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS));
					}
				}
			}
			String mimeType = mimeService.getMimeTypeForFilename(path);
			InitialFilestoreStream contentStream = new InitialFilestoreStream(itemId, fileHandle, path, mimeType);

			if( !contentStream.exists() )
			{
				response.sendError(404);
				return;
			}
			FilestoreContentStream filteredStream = contentStream;
			List<FilestoreContentFilter> filterList = fileFilterService.getFilters();
			for( FilestoreContentFilter filter : filterList )
			{
				filteredStream = filter.filter(filteredStream, request, response);
				if( filteredStream == null )
				{
					return;
				}
			}
			if( itemId != null )
			{
				auditor.audit(new ViewAuditEntry("file:" + mimeType, path), itemId); //$NON-NLS-1$
			}
			contentStreamWriter.outputStream(request, response, filteredStream);
		}
		catch( AccessDeniedException ade )
		{
			if( CurrentUser.isGuest() )
			{
				LogonSection.forwardToLogon(controller, request, response, "file" + request.getPathInfo(),
					LogonSection.STANDARD_LOGON_PATH);
			}
			else
			{
				response.sendError(403);
			}
			return;
		}
	}

	private IAttachment getAttachmentWithDifferentPriv(ItemKey itemId, String path)
	{
		try
		{
			return new UnmodifiableAttachments(itemService.get(itemId).getAttachments()).getAttachmentByFilename(path);
		}
		catch( Exception e )
		{
			return new UnmodifiableAttachments(itemService.getItemWithViewAttachmentPriv(itemId).getAttachments())
				.getAttachmentByFilename(path);
		}
	}

	public class InitialFilestoreStream extends WrappedContentStream implements FilestoreContentStream
	{
		private final ItemKey itemId;
		private final FileHandle handle;
		private final String filepath;

		public InitialFilestoreStream(ItemKey itemId, FileHandle fileHandle, String path, String mimeType)
		{
			super(fileSystemService.getContentStream(fileHandle, path, mimeType));
			this.handle = fileHandle;
			this.filepath = path;
			this.itemId = itemId;
		}

		@SuppressWarnings("nls")
		@Override
		public String getCacheControl()
		{
			// Before changing anything, read http://www.mnot.net/cache_docs/
			//
			// The following settings mean that all caches will be able to keep
			// a copy of the resource - the age values only specify how long
			// before the cache must *revalidate* their copy. With these
			// settings, browser caches can serve their copy for a whole day
			// without needing to talk to the server. Shared caches (eg, a Squid
			// proxy cache) must always revalidate before releasing their copy
			// to ensure we're not returning a resource to a user who doesn't
			// have access.
			return "max-age=86400, s-maxage=0, must-revalidate";
		}

		@Override
		public String getFileDirectoryPath()
		{
			String path = getFilepath();
			int ind = path.lastIndexOf('/');
			if( ind == -1 )
			{
				return ""; //$NON-NLS-1$
			}
			return path.substring(0, ind);
		}

		@Override
		public FileHandle getFileHandle()
		{
			return handle;
		}

		@Override
		public String getFilepath()
		{
			return filepath;
		}

		@Override
		public ItemKey getItemId()
		{
			return itemId;
		}

	}
}
