package com.tle.core.item.edit.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.java.plugin.registry.Extension;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.LanguageBundle.DeleteHandler;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemEditingException;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemLock;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ItemXml;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Check;
import com.tle.core.dao.ItemDao;
import com.tle.core.events.IndexItemBackgroundEvent;
import com.tle.core.events.IndexItemNowEvent;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.edit.DRMEditor;
import com.tle.core.item.edit.ItemAttachmentListener;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.edit.ItemEditorChangeTracker;
import com.tle.core.item.edit.ItemMetadataListener;
import com.tle.core.item.edit.NavigationEditor;
import com.tle.core.item.edit.attachment.AbstractAttachmentEditor;
import com.tle.core.item.edit.attachment.AttachmentEditor;
import com.tle.core.item.security.ItemSecurityConstants;
import com.tle.core.item.serializer.ItemDeserializerEditor;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.EventService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.QuotaService;
import com.tle.core.services.StagingService;
import com.tle.core.services.WorkflowOperationService;
import com.tle.core.services.item.ItemLockingService;
import com.tle.core.user.CurrentUser;
import com.tle.core.util.ItemHelper;
import com.tle.exceptions.PrivilegeRequiredException;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

@SuppressWarnings("nls")
public final class ItemEditorImpl implements ItemEditor, DeleteHandler, ItemEditorChangeTracker
{
	private static final String SAVE_SCRIPT_NAME = "saveOperation"; //$NON-NLS-1$

	@Inject
	private PluginTracker<AbstractAttachmentEditor> attachEditorTracker;
	@Inject
	private PluginTracker<ItemMetadataListener> metadataListenerTracker;
	@Inject
	private PluginTracker<ItemAttachmentListener> attachmentListenerTracker;
	@Inject
	private WorkflowOperationService workflowOpService;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private ItemDao itemDao;
	@Inject
	private EventService eventService;
	@Inject
	private StagingService stagingService;
	@Inject
	private QuotaService quotaService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemLockingService itemLockingService;

	private final Item item;
	private final boolean newItem;
	private final boolean canEdit;
	private final Set<String> privileges;
	private final ItemLock itemLock;
	private final List<ItemDeserializerEditor> deserializerEditors;

	private final Date now = new Date();
	private final Set<String> indexingChanges = Sets.newHashSet();
	private final boolean importing;

	private FileHandle fileHandle;
	private String stagingUuid;

	private PropBagEx itemxml;
	private Map<String, Attachment> attachmentMap;
	private Map<String, Attachment> linkedAttachmentMap;
	private List<String> attachmentOrder;
	private boolean attachmentsEdited;
	private boolean metadataEdited;
	private boolean updateDateModified;
	private boolean preventSaveScript;

	private NavigationEditorImpl navigationEditor;
	private DRMEditorImpl drmEditor;
	private boolean unlock;

	@AssistedInject
	private ItemEditorImpl(TLEAclManager aclManager, @Assisted Item item, @Assisted @Nullable ItemLock lock,
		@Assisted List<ItemDeserializerEditor> deserializerEditors)
	{
		this.newItem = false;
		this.item = item;
		this.deserializerEditors = deserializerEditors;
		privileges = aclManager.filterNonGrantedPrivileges(item,
			ImmutableSet.of(ItemSecurityConstants.EDIT_ITEM, ItemSecurityConstants.REASSIGN_OWNERSHIP_ITEM));
		canEdit = privileges.contains(ItemSecurityConstants.EDIT_ITEM);
		this.itemLock = lock;
		// TODO do we want to do this?
		updateDateModified = true;
		importing = false;
	}

	@AssistedInject
	private ItemEditorImpl(@Assisted Item item, @Assisted List<ItemDeserializerEditor> deserializerEditors)
	{
		this.item = item;
		this.newItem = true;
		this.itemLock = null;
		this.deserializerEditors = deserializerEditors;
		updateDateModified = true;
		metadataEdited = true;
		canEdit = true;
		privileges = null;
		importing = false;
	}

	@AssistedInject
	private ItemEditorImpl(@Assisted Item item, @Assisted List<ItemDeserializerEditor> deserializerEditors,
		@Assisted boolean importing)
	{
		this.item = item;
		this.newItem = true;
		this.itemLock = null;
		this.deserializerEditors = deserializerEditors;
		this.importing = true;
		updateDateModified = false;
		metadataEdited = true;
		canEdit = true;
		privileges = null;
		preventSaveScript = true;
	}

	@Override
	public void doEdits(EquellaItemBean itemBean)
	{
		for( ItemDeserializerEditor deserializerEditor : deserializerEditors )
		{
			deserializerEditor.edit(itemBean, this, importing);
		}
	}

	public void setStagingUuid(String stagingUuid)
	{
		this.stagingUuid = stagingUuid;
		if( stagingUuid != null )
		{
			if( !stagingService.stagingExists(stagingUuid) )
			{
				throw new ItemEditingException("Staging id '" + stagingUuid + "' doesn't exist");
			}
			fileHandle = new StagingFile(stagingUuid);
			editDetected();
		}
		else
		{
			if( !newItem )
			{
				fileHandle = new ItemFile(item);
			}
			else
			{
				fileHandle = null;
			}
		}
	}

	@Override
	public FileHandle getFileHandle()
	{
		return fileHandle;
	}

	@Override
	public void editDates(Date dateCreated, Date dateModified)
	{
		if( importing )
		{
			item.setDateCreated(dateCreated);
			item.setDateModified(dateModified);
		}
	}

	@Override
	public void editItemStatus(String status)
	{
		if( importing )
		{
			final ItemStatus s;
			if( Strings.isNullOrEmpty(status) )
			{
				s = ItemStatus.DRAFT;
			}
			else
			{
				s = ItemStatus.valueOf(status.toUpperCase());
			}
			item.setStatus(s);
		}
	}

	@Override
	public void editOwner(String owner)
	{
		if( !Objects.equals(item.getOwner(), owner) )
		{
			if( !newItem && !privileges.contains(ItemSecurityConstants.REASSIGN_OWNERSHIP_ITEM) )
			{
				throw new PrivilegeRequiredException(ItemSecurityConstants.REASSIGN_OWNERSHIP_ITEM);
			}
			item.setOwner(owner);
			indexingChanges.add("owner");
		}
	}

	@Override
	public void editCollaborators(Set<String> collaborators)
	{
		final Set<String> collabs = item.getCollaborators();
		if( !collabs.equals(collaborators) )
		{
			if( !newItem && !privileges.contains(ItemSecurityConstants.REASSIGN_OWNERSHIP_ITEM) )
			{
				throw new PrivilegeRequiredException(ItemSecurityConstants.REASSIGN_OWNERSHIP_ITEM);
			}
			collabs.clear();
			collabs.addAll(collaborators);
			indexingChanges.add("owner-collabs");
		}
	}

	@Override
	public void editThumbnail(String thumbnail)
	{
		if( hasBeenEdited(item.getThumb(), thumbnail) )
		{
			if( thumbnail == null )
			{
				item.setThumb("default");
			}
			else
			{
				item.setThumb(thumbnail);
			}
		}
	}

	@Override
	public void editMetadata(String xml)
	{
		ItemXml itemXml = item.getItemXml();
		if( !itemXml.getXml().equals(xml) )
		{
			metadataEdited = true;
			editDetected();
			addIndexingEdit("metadata");
			itemXml.setXml(xml);
		}
		itemxml = null;
	}

	@Override
	public void editMetadata(PropBagEx xml)
	{
		editMetadata(xml.toString());
		this.itemxml = xml;
	}

	@Override
	public <T extends AttachmentEditor> T getAttachmentEditor(String uuid, Class<T> type)
	{
		Attachment existingAttachment = null;
		Map<String, Attachment> attachMap = getAttachmentMap();
		if( Check.isEmpty(uuid) )
		{
			uuid = UUID.randomUUID().toString();
		}
		else
		{
			existingAttachment = attachMap.get(uuid);
			if( existingAttachment == null )
			{
				checkValidUuid(uuid);
			}
		}
		Map<String, Extension> extMap = attachEditorTracker.getExtensionMap();
		Extension extension = extMap.get(type.getName());
		if( extension == null )
		{
			throw new ItemEditingException("No extension for '" + type.getName() + "'");
		}
		AbstractAttachmentEditor attachEditor = attachEditorTracker.getNewBeanByExtension(extension);
		attachEditor.setItemEditorChangeTracker(this);
		attachEditor.setItem(item);
		attachEditor.setFileHandle(fileHandle);
		if( existingAttachment == null || !attachEditor.canEdit(existingAttachment) )
		{
			Attachment attachment = attachEditor.newAttachment();
			attachment.setUuid(uuid);
			attachMap.put(uuid, attachment);
			attachEditor.setAttachment(attachment);
			if( linkedAttachmentMap.containsKey(uuid) )
			{
				attachmentEditDetected();
				linkedAttachmentMap.put(uuid, attachment);
			}
		}
		else
		{
			attachEditor.setAttachment(existingAttachment);
		}
		return type.cast(attachEditor);
	}

	public static void checkValidUuid(String uuid)
	{
		boolean invalid = false;
		try
		{
			invalid = !uuid.equals(UUID.fromString(uuid).toString());
		}
		catch( Exception e )
		{
			invalid = true;
		}
		if( invalid )
		{
			throw new ItemEditingException("Invalid UUID '" + uuid + "'");
		}
	}

	@Override
	public void editAttachmentOrder(List<String> attachmentUuids)
	{
		Map<String, Attachment> locAttachmentMap = getAttachmentMap();
		if( !attachmentOrder.equals(attachmentUuids) )
		{
			editDetected();
			linkedAttachmentMap.clear();
			attachmentOrder.clear();
			attachmentOrder.addAll(attachmentUuids);

			for( String attachmentUuid : attachmentUuids )
			{
				Attachment newAttach = locAttachmentMap.get(attachmentUuid);
				if( newAttach == null )
				{
					throw new ItemEditingException("Attachment with uuid '" + attachmentUuid + "' does not exist");
				}
				linkedAttachmentMap.put(attachmentUuid, newAttach);
			}
		}
	}

	@Override
	public void editRating(Float rating)
	{
		if( rating == -1 || (rating >= 0 && rating <= 5.0) )
		{
			item.setRating(rating);
			indexingChanges.add("rating");
		}
		else
		{
			throw new ItemEditingException("Rating must be a float between 0 and 5 or -1 for unrated");
		}
	}

	public Map<String, Attachment> getAttachmentMap()
	{
		if( attachmentMap == null )
		{
			attachmentMap = Maps.newHashMap();
			linkedAttachmentMap = Maps.newHashMap();
			attachmentOrder = Lists.newArrayList();
			for( Attachment attachment : item.getAttachments() )
			{
				String attachUuid = attachment.getUuid();
				attachmentMap.put(attachUuid, attachment);
				linkedAttachmentMap.put(attachUuid, attachment);
				attachmentOrder.add(attachUuid);
			}
		}
		return attachmentMap;
	}

	@Override
	public void preventSaveScript()
	{
		preventSaveScript = true;
	}

	@Override
	public ItemIdKey finishedEditing(boolean ensureOnIndexList)
	{
		finishAttachments();
		ItemDefinition itemDefinition = item.getItemDefinition();
		Wizard wizard = itemDefinition.getWizard();
		if( !importing && !preventSaveScript && wizard != null )
		{
			final String saveScript = wizard.getSaveScript();
			if( !Check.isEmpty(saveScript) )
			{
				ItemPack<Item> itemPack = createItemPack();
				ScriptContext scriptContext = workflowOpService.createScriptContext(itemPack, null, null, null);
				workflowOpService.executeScript(saveScript, SAVE_SCRIPT_NAME, scriptContext, true);
				editMetadata(itemPack.getXml());
				indexingChanges.add("savescript");
			}
		}
		if( !importing && updateDateModified )
		{
			indexingChanges.add("datemodified");
			createHistory(Type.edit);
			item.setDateModified(now);
		}
		if( newItem || !indexingChanges.isEmpty() )
		{
			item.setDateForIndex(now);
		}
		if( metadataEdited )
		{
			itemHelper.updateItemFromXml(createItemPack(), this, false);
		}
		itemDao.save(item);
		if( metadataEdited )
		{
			workflowOpService.updateMetadataBasedSecurity(getMetadata(), item);
			for( ItemMetadataListener metadataListener : metadataListenerTracker.getBeanList() )
			{
				metadataListener.metadataChanged(item, getMetadata());
			}
		}
		if( attachmentsEdited )
		{
			for( ItemAttachmentListener attachmentListener : attachmentListenerTracker.getBeanList() )
			{
				attachmentListener.attachmentsChanged(this, item, getFileHandle());
			}
		}

		if( stagingUuid != null )
		{
			processFiles();
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSync(ensureOnIndexList));
		if( unlock && itemLock != null )
		{
			itemLockingService.unlock(itemLock);
		}
		return new ItemIdKey(item);
	}

	private void finishAttachments()
	{
		if( linkedAttachmentMap != null )
		{
			List<Attachment> attachments = item.getAttachments();
			int i = 0;
			for( String attachmentUuid : attachmentOrder )
			{
				Attachment attach = linkedAttachmentMap.get(attachmentUuid);
				if( attachments.size() <= i )
				{
					attachments.add(attach);
				}
				else
				{
					attachments.set(i, attach);
				}
				i++;
			}
			while( attachments.size() > i )
			{
				attachments.remove(i);
			}
		}
		if( navigationEditor != null || attachmentMap != null )
		{
			getNavigationEditor();
			navigationEditor.finishedEditing(linkedAttachmentMap);
		}
	}

	private void processFiles()
	{
		final StagingFile staging = (StagingFile) fileHandle;
		for( ItemDeserializerEditor deserializerEditor : deserializerEditors )
		{
			deserializerEditor.processFiles(item, this, importing);
		}

		item.setTotalFileSize(quotaService.checkQuotaAndReturnNewItemSize(item, staging));
		if( unlock )
		{
			stagingService.removeStagingArea(staging, false);
		}
	}

	private ItemPack<Item> createItemPack()
	{
		ItemPack<Item> itemPack = new ItemPack<Item>();
		itemPack.setItem(item);
		itemPack.setXml(getMetadata());
		if( stagingUuid != null )
		{
			itemPack.setStagingID(stagingUuid);
		}
		return itemPack;
	}

	@Override
	public PropBagEx getMetadata()
	{
		if( itemxml == null )
		{
			itemxml = new PropBagEx(item.getItemXml().getXml());
		}
		return itemxml;
	}

	@Override
	public NavigationEditor getNavigationEditor()
	{
		if( navigationEditor == null )
		{
			navigationEditor = new NavigationEditorImpl(this, getAttachmentMap(), item);
		}
		return navigationEditor;
	}

	@Override
	public void deleteBundleObject(Object obj)
	{
		itemDao.deleteAny(obj);
	}

	protected void createHistory(Type type)
	{
		if( !importing )
		{
			HistoryEvent historyEvent = new HistoryEvent(type, item);
			historyEvent.setUser(CurrentUser.getUserID());
			historyEvent.setDate(now);
			List<HistoryEvent> history = item.getHistory();
			history.add(historyEvent);
		}
	}

	@Override
	public DRMEditor getDRMEditor()
	{
		if( drmEditor == null )
		{
			drmEditor = new DRMEditorImpl(item, this);
		}
		return drmEditor;
	}

	@Override
	public boolean hasBeenEdited(Object orig, Object edit)
	{
		if( Objects.equals(orig, edit) )
		{
			return false;
		}
		editDetected();
		return true;
	}

	@Override
	public void editDetected()
	{
		if( !canEdit )
		{
			throw new PrivilegeRequiredException(ItemSecurityConstants.EDIT_ITEM);
		}
		updateDateModified = true;
	}

	@Override
	public void attachmentEditDetected()
	{
		attachmentsEdited = true;
	}

	@Override
	public void addIndexingEdit(String editType)
	{
		indexingChanges.add(editType);
	}

	@Override
	public void editWithPrivilege(String priv)
	{
		if( !privileges.contains(priv) )
		{
			throw new PrivilegeRequiredException(priv);
		}
	}

	@Override
	public boolean isForceFileCheck()
	{
		return stagingUuid != null;
	}

	@Override
	public void unlock()
	{
		if( itemLock == null )
		{
			throw new ItemEditingException("Item is not locked");
		}
		unlock = true;
	}

	public class TransactionSync extends TransactionSynchronizationAdapter
	{
		private final boolean ensureOnIndexList;

		public TransactionSync(boolean ensureOnIndexList)
		{
			this.ensureOnIndexList = ensureOnIndexList;
		}

		@Override
		public void afterCommit()
		{
			try
			{
				if( stagingUuid != null )
				{
					StagingFile stagingFile = (StagingFile) fileHandle;
					ItemFile destFiles = new ItemFile(item);
					if( itemLock == null || unlock )
					{
						fileSystemService.commitFiles(stagingFile, destFiles);
					}
					else
					{
						fileSystemService.saveFiles(stagingFile, destFiles);
					}
				}
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
			if( !indexingChanges.isEmpty() )
			{
				ItemIdKey itemId = new ItemIdKey(item);
				eventService.publishApplicationEvent(new IndexItemBackgroundEvent(itemId, !ensureOnIndexList));
				if( ensureOnIndexList )
				{
					eventService.publishApplicationEvent(new IndexItemNowEvent(itemId));
				}
			}
		}
	}

	@BindFactory
	public interface ItemEditorFactory
	{
		ItemEditorImpl createExistingEditor(Item item, ItemLock lock, List<ItemDeserializerEditor> deserializerEditors);

		ItemEditorImpl createNewEditor(Item item, List<ItemDeserializerEditor> deserializerEditors);

		ItemEditorImpl createImportEditor(Item item, List<ItemDeserializerEditor> deserializerEditors, boolean importing);
	}

	@Override
	public boolean isNewItem()
	{
		return newItem;
	}
}
