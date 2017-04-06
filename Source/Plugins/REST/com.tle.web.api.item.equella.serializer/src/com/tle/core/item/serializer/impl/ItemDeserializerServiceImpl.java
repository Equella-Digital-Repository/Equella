package com.tle.core.item.serializer.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.item.ItemEditingException;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.edit.ItemEditorService;
import com.tle.core.item.serializer.ItemDeserializerEditor;
import com.tle.core.item.serializer.ItemDeserializerService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

@Bind(ItemDeserializerService.class)
@Singleton
@SuppressWarnings("nls")
public class ItemDeserializerServiceImpl implements ItemDeserializerService
{
	@Inject
	private ItemEditorService itemEditorService;
	@Inject
	private PluginTracker<ItemDeserializerEditor> editorsTracker;
	@Inject
	private ItemService itemService;
	@Inject
	private WorkflowFactory workflowFactory;

	@Override
	@Transactional
	public ItemIdKey edit(EquellaItemBean itemBean, String stagingUuid, String lockId, boolean unlock,
		boolean ensureOnIndexList)
	{
		ItemId itemId = new ItemId(itemBean.getUuid(), itemBean.getVersion());
		ItemEditor editor = itemEditorService.getItemEditor(itemId, stagingUuid, lockId, editorsTracker.getBeanList());
		editor.doEdits(itemBean);
		if( unlock && lockId != null )
		{
			editor.unlock();
		}
		return editor.finishedEditing(ensureOnIndexList);
	}

	@Override
	@Transactional
	public ItemIdKey newItem(EquellaItemBean itemBean, String stagingUuid, boolean dontSubmit,
		boolean ensureOnIndexList, boolean noAutoArchive)
	{
		ItemId itemId = new ItemId(itemBean.getUuid(), itemBean.getVersion());
		BaseEntityReference collectionRef = itemBean.getCollection();
		if( collectionRef == null )
		{
			throw new ItemEditingException("No collection specified");
		}
		String collectionUuid = collectionRef.getUuid();
		ItemEditor editor = itemEditorService.newItemEditor(collectionUuid, itemId, stagingUuid,
			editorsTracker.getBeanList());
		editor.doEdits(itemBean);
		if( !dontSubmit )
		{
			editor.preventSaveScript();
		}
		ItemIdKey itemKey = editor.finishedEditing(ensureOnIndexList);
		if( !dontSubmit )
		{
			itemService.operation(itemKey, workflowFactory.submit(),
				workflowFactory.saveNoIndexing(noAutoArchive, stagingUuid));
		}
		return itemKey;
	}

	@SecureOnCallSystem
	@Transactional
	@Override
	public ItemIdKey importItem(EquellaItemBean itemBean, String stagingUuid, boolean ensureOnIndexList)
	{
		ItemId itemId = new ItemId(itemBean.getUuid(), itemBean.getVersion());
		BaseEntityReference collectionRef = itemBean.getCollection();
		if( collectionRef == null )
		{
			throw new ItemEditingException("No collection specified");
		}
		String collectionUuid = collectionRef.getUuid();
		ItemEditor editor = itemEditorService.importItemEditor(collectionUuid, itemId, stagingUuid,
			editorsTracker.getBeanList());
		editor.doEdits(itemBean);

		ItemIdKey itemKey = editor.finishedEditing(ensureOnIndexList);

		return itemKey;
	}
}
