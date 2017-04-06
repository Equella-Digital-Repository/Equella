package com.tle.core.item.operations;

import java.util.UUID;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.schema.SchemaService;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.core.util.ItemHelper;
import com.tle.core.util.ItemHelper.ItemHelperSettings;
import com.tle.core.workflow.operations.AbstractCloneOperation;
import com.tle.core.workflow.operations.WorkflowFactory;

/**
 * @author aholland
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SecureOnCall(priv = "CLONE_ITEM")
@SecureItemStatus(value = {ItemStatus.PERSONAL}, not = true)
@SuppressWarnings("nls")
public final class CloneOperation extends AbstractCloneOperation // NOSONAR
{
	@Inject
	private ItemDefinitionService itemDefService;
	@Inject
	private SchemaService schemaService;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private WorkflowFactory workflowFactory;

	private String oldItemdefUuid;
	private final String newItemdefUuid;
	private final boolean submit;

	/**
	 * Clone in the same item definition.
	 */
	@AssistedInject
	private CloneOperation(@Assisted("copyAttachments") boolean copyAttachments, @Assisted("submit") boolean submit)
	{
		this(null, copyAttachments, submit);
	}

	/**
	 * Clone to possibly a new item definition.
	 * 
	 * @param newItemDefUuid null indicates that it is in the same collection.
	 */
	@AssistedInject
	private CloneOperation(@Assisted String newItemDefUuid, @Assisted("copyAttachments") boolean copyAttachments,
		@Assisted("submit") boolean submit)
	{
		super(copyAttachments);
		this.newItemdefUuid = newItemDefUuid;
		this.submit = submit;
	}

	@Override
	protected Item initItemUuidAndVersion(Item newItem, Item oldItem)
	{
		newItem.setUuid(UUID.randomUUID().toString());
		newItem.setVersion(1);
		return newItem;
	}

	@Override
	protected void pushCloneData(Item item, CloningHelper forCloning)
	{
		super.pushCloneData(item, forCloning);

		if( newItemdefUuid != null )
		{
			oldItemdefUuid = item.getItemDefinition().getUuid();
			item.setItemDefinition(itemDefService.getByUuid(newItemdefUuid));
		}
	}

	@Override
	protected void finalProcessing(Item origItem, Item item)
	{
		super.finalProcessing(origItem, item);

		if( !copyAttachments )
		{
			fileSystemService.removeFile(getStaging(), "");
			// Make a blank folder
			fileSystemService.mkdir(getStaging(), "");
		}

		// now use the transform, if any
		if( !Check.isEmpty(transform) )
		{
			ItemPack<Item> pack = params.getItemPack();

			Item oldItem = pack.getOriginalItem();
			ItemPack<Item> oldPack = new ItemPack<>(oldItem, itemService.getItemXmlPropBag(oldItem), null);
			PropBagEx oldXml = itemHelper.convertToXml(oldPack, new ItemHelperSettings(true));

			try
			{
				Item newItem = pack.getItem();
				PropBagEx newXml = new PropBagEx(schemaService.transformForImport(newItem.getItemDefinition()
					.getSchema().getId(), transform, oldXml));
				pack.setXml(newXml);
			}
			catch( Exception ex )
			{
				throw new WorkflowException(
					CurrentLocale.get("com.tle.core.workflow.operations.clone.error.transforming"), ex);
			}
		}
		if( submit )
		{
			params.addOperation(workflowFactory.submit());
		}
	}

	@Override
	protected void doHistory()
	{
		// if new itemdef is not the same as old, then technically a move was
		// performed
		if( newItemdefUuid != null && !newItemdefUuid.equals(oldItemdefUuid) )
		{
			createHistory(HistoryEvent.Type.changeCollection);
		}
		else
		{
			createHistory(HistoryEvent.Type.clone);
		}
	}
}
