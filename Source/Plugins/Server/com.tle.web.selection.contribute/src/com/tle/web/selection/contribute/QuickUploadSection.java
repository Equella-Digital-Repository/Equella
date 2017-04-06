package com.tle.web.selection.contribute;

import java.io.IOException;

import javax.inject.Inject;

import com.google.common.base.Throwables;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.core.services.QuickUploadService;
import com.tle.core.services.item.ItemResolver;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.FileUpload;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.SelectAttachmentHandler;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.contribute.QuickUploadSection.QuickUploadModel;
import com.tle.web.selection.event.AttachmentSelectorEvent;
import com.tle.web.selection.event.AttachmentSelectorEventListener;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;

/**
 * Used for quick upload on the contribution page when in a selection session
 */
@Bind
@SuppressWarnings("nls")
public class QuickUploadSection extends AbstractPrototypeSection<QuickUploadModel>
	implements
		HtmlRenderer,
		ViewableChildInterface,
		AttachmentSelectorEventListener
{
	@PlugKey("uploadbutton")
	@Component
	private Button uploadButton;
	@Component
	private FileUpload fileUploader;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;

	@Inject
	private QuickUploadService quickUploadService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private ItemResolver itemResolver;
	@Inject
	private ViewableItemResolver viewableItemResolver;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( canView(context) )
		{
			final ItemDefinition collection = quickUploadService.getOneClickItemDef();
			if( collection != null )
			{
				getModel(context).setCollectionName(
					new BundleLabel(collection.getName(), collection.getUuid(), bundleCache));
			}

			return viewFactory.createResult("quickupload.ftl", context);
		}

		return null;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.addListener(null, AttachmentSelectorEventListener.class, this);
		uploadButton.setClickHandler(events.getNamedHandler("upload"));
	}

	@EventHandlerMethod
	public void upload(SectionInfo info) throws Exception
	{
		final String filename = fileUploader.getFilename(info);
		if( fileUploader.getFileSize(info) > 0 && !Check.isEmpty(filename) )
		{
			try
			{
				Pair<ItemId, Attachment> attInfo = quickUploadService.createOrSelectExisting(
					fileUploader.getInputStream(info), filename);
				ViewableItem<?> vitem = viewableItemResolver.createViewableItem(
					itemResolver.getItem(attInfo.getFirst(), null), null);
				SelectAttachmentHandler selectAttachmentHandler = selectionService.getSelectAttachmentHandler(info,
					vitem, null);

				if( selectAttachmentHandler != null )
				{
					selectAttachmentHandler.handleAttachmentSelection(info, attInfo.getFirst(), attInfo.getSecond(),
						null);
					selectionService.returnFromSession(info);
				}
			}
			catch( IOException e )
			{
				Throwables.propagate(e);
			}
		}
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		final ItemDefinition itemdef = quickUploadService.getOneClickItemDef();
		final SelectionSession ss = selectionService.getCurrentSession(info);
		if( ss == null )
		{
			return false;
		}

		boolean quick = false;
		boolean cont = false;

		if( itemdef != null
			&& (ss.isAllContributionCollections() || ss.getContributionCollectionIds().contains(itemdef.getUuid())) )
		{
			quick = true;
		}
		if( ss.isAllContributionCollections() || !ss.getContributionCollectionIds().isEmpty() )
		{
			cont = true;
		}

		return quick && cont;
	}

	@Override
	public void supplyFunction(SectionInfo info, AttachmentSelectorEvent event)
	{
		if( event.getHandler() == null )
		{
			event.setHandler(this);
		}
	}

	@Override
	public void handleAttachmentSelection(SectionInfo info, ItemId itemId, IAttachment attachment, String extensionType)
	{
		selectionService.addSelectedResource(info,
			selectionService.createAttachmentSelection(info, itemId, attachment, null, null), false);
		selectionService.returnFromSession(info);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new QuickUploadModel();
	}

	public Button getUploadButton()
	{
		return uploadButton;
	}

	public FileUpload getFileUploader()
	{
		return fileUploader;
	}

	public static class QuickUploadModel
	{
		private Label collectionName;

		public Label getCollectionName()
		{
			return collectionName;
		}

		public void setCollectionName(Label collectionName)
		{
			this.collectionName = collectionName;
		}
	}
}