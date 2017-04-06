package com.tle.web.viewitem.section;

import java.util.Collection;

import javax.inject.Inject;

import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.viewitem.AbstractAttachmentViewItemResource;
import com.tle.web.viewurl.ViewAttachmentUrl.ViewAttachmentInterface;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemFilter;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

public class ViewAttachmentSection extends AbstractPrototypeSection<ViewAttachmentSection.ViewAttachmentModel>
	implements
		ViewAttachmentInterface,
		ViewItemViewer,
		ViewItemFilter
{
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	@TreeLookup
	private RootItemFileSection rootSection;

	@Override
	public int getOrder()
	{
		return 0;
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		rootSection.addFilterViewer(this);
		rootSection.setFallbackMapping(new FilenameToAttachmentFilter());
	}

	@SuppressWarnings("nls")
	@Override
	public ViewItemResource filter(SectionInfo info, ViewItemResource resource)
	{
		final ViewAttachmentModel model = getModel(info);
		resource.setAttribute("isStream", model.isStream());
		final IAttachment attachment = getAttachment(info, resource);
		if( attachment != null )
		{
			ViewableResource viewableResource = attachmentResourceService.getViewableResource(info,
				resource.getViewableItem(), attachment);
			return new ViewAttachmentViewItemResource(resource, viewableResource, model.isStream());
		}
		return resource;
	}

	@Nullable
	@Override
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		final ViewAttachmentModel model = getModel(info);
		final String attachUuid = model.getUuid();
		if( attachUuid != null )
		{
			final IAttachment attachment = resource.getViewableItem().getAttachmentByUuid(attachUuid);
			if( attachment == null )
			{
				SectionUtils.throwRuntime(new IllegalArgumentException("Attachment " + attachUuid
					+ " could not be found."));
			}
			return attachment;
		}
		return null;
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return resource.getViewAuditEntry();
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return ViewItemViewer.VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		info.setRendered();
		contentStreamWriter.outputStream(info.getRequest(), info.getResponse(), resource.getContentStream());
		return null;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "attachment"; //$NON-NLS-1$s
	}

	@Override
	public Class<ViewAttachmentModel> getModelClass()
	{
		return ViewAttachmentModel.class;
	}

	public static class ViewAttachmentModel
	{
		@Bookmarked(parameter = "attachment.uuid", supported = true)
		private String uuid;
		@Bookmarked
		private boolean stream;

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public boolean isStream()
		{
			return stream;
		}

		public void setStream(boolean stream)
		{
			this.stream = stream;
		}
	}

	@Override
	public void setAttachmentToView(SectionInfo info, String uuid)
	{
		if( uuid != null )
		{
			getModel(info).setUuid(uuid);
		}
	}

	public String getUuid(SectionInfo info)
	{
		ViewAttachmentModel model = getModel(info);
		return model.getUuid();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		// tree.setAttribute(ViewAttachmentInterface.class, this);
	}

	public class FilenameToAttachmentFilter implements ViewItemFilter
	{
		@Override
		public int getOrder()
		{
			return 0;
		}

		@Override
		public ViewItemResource filter(SectionInfo info, ViewItemResource resource)
		{
			String viewerId = resource.getDefaultViewerId();
			if( Check.isEmpty(viewerId) )
			{
				IAttachment attachment = resource.getViewableItem().getAttachmentByFilepath(resource.getFilepath());
				if( attachment != null )
				{
					ViewableResource viewableResource = attachmentResourceService.getViewableResource(info,
						resource.getViewableItem(), attachment);
					return new ViewAttachmentViewItemResource(resource, viewableResource, false);
				}
			}
			return resource;
		}
	}

	public class ViewAttachmentViewItemResource extends AbstractAttachmentViewItemResource
	{
		public ViewAttachmentViewItemResource(ViewItemResource inner, ViewableResource viewableResource,
			boolean forcedStream)
		{
			super(inner, viewableResource, forcedStream);
		}

		@Override
		public ViewItemViewer getViewer()
		{
			return forcedStream ? ViewAttachmentSection.this : null;
		}
	}
}
