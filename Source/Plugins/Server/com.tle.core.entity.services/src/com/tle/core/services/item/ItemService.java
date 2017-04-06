package com.tle.core.services.item;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.AttachmentNotFoundException;
import com.google.common.collect.Multimap;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemSelect;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Triple;
import com.tle.core.remoting.RemoteItemService;
import com.tle.core.workflow.filters.FilterResultListener;
import com.tle.core.workflow.filters.WorkflowFilter;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.core.workflow.operations.WorkflowParams;

/**
 * @author Nicholas Read
 */
public interface ItemService extends RemoteItemService
{
	Item getUnsecure(ItemKey itemId);

	Item getUnsecureIfExists(ItemKey itemId);

	List<ItemId> getItemsWithUrl(String url, ItemDefinition itemDefinition, String excludedUuid);

	List<Item> getVersionDetails(String uuid);

	List<Item> getNextLiveItems(List<ItemId> items);

	/**
	 * @param itemId
	 * @param uuid
	 * @return Never returns null.
	 * @throws AttachmentNotFoundException
	 */
	Attachment getAttachmentForUuid(ItemKey itemId, String uuid);

	Multimap<Item, Attachment> getAttachmentsForItems(Collection<Item> items);

	int getLatestVersion(String uuid);

	Item getLatestVersionOfItem(String uuid);

	int getLiveItemVersion(String uuid);

	ItemIdKey getLiveItemVersionId(String uuid);

	ItemPack<Item> getItemPack(ItemKey key);

	PropBagEx getItemXmlPropBag(Item item);

	/**
	 * Use the other version if possible
	 * 
	 * @param item
	 * @return
	 */
	PropBagEx getItemXmlPropBag(ItemKey key);

	List<Item> queryItems(List<Long> itemkeys);

	List<Item> queryItems(List<ItemIdKey> itemkeys, ItemSelect select);

	List<Item> queryItemsByUuids(List<String> uuids);

	<A> List<A> queryItems(DetachedCriteria criteria, Integer firstResult, Integer maxResults);

	void updateIndexTimes(String whereClause, String[] names, Object[] values);

	long getUserFileSize(String whereClause, String[] names, Object[] values);

	List<ItemId> enumerateItems(String whereClause, String[] names, Object[] values);

	List<ItemIdKey> getItemIdKeys(List<Long> ids);

	ItemIdKey getItemIdKey(Long id);

	/**
	 * Returns uuid, version, name
	 */
	List<Triple<String, Integer, String>> enumerateItemNames(String whereClause, String[] names, Object[] values);

	ItemPack<Item> operation(ItemKey key, WorkflowOperation... operations);

	void operateAll(WorkflowFilter filter);

	void operateAllInTransaction(WorkflowFilter filter);

	void operateAll(WorkflowFilter filter, FilterResultListener listener);

	/* LEGACY or not.. */

	void forceUnlock(Item item);

	Map<ItemId, LanguageBundle> getItemNames(Collection<? extends ItemKey> keys);

	Map<ItemId, Long> getItemNameIds(Collection<? extends ItemKey> keys);

	Set<String> getCachedPrivileges(ItemKey itemKey);

	Set<String> getReferencedUsers();

	void delete(Item item);

	void executeOperationsNow(WorkflowParams params, Collection<WorkflowOperation> operations);

	List<WorkflowOperation> executeExtensionOperationsNow(WorkflowParams params, String type);

	List<WorkflowOperation> executeExtensionOperationsLater(WorkflowParams params, String type);

	/**
	 * Filters out item UUIDs from the given list for items that are not in any
	 * of the given collections. Does not modify the passed in item UUID list.
	 * The returned set is not guaranteed to have the contents the same
	 * ordering.
	 */
	Set<String> unionItemUuidsWithCollectionUuids(Collection<String> itemUuids, Set<String> collectionUuids);

	Map<String, Object> getItemInfo(ItemId id);

	Map<ItemId, Item> queryItemsByItemIds(List<? extends ItemKey> itemIds);

	/**
	 * Used only by REST endpoint
	 * 
	 * @param itemId
	 * @param purge
	 * @param waitForIndex
	 */
	void delete(ItemId itemId, boolean purge, boolean waitForIndex);

	Item getForEdit(ItemKey itemId);

	Item getForNewVersion(ItemKey itemId);

	Attachment getAttachmentForFilepath(ItemKey itemId, String filepath);

	List<String> getNavReferencedAttachmentUuids(List<Item> items);

	Item getItemWithViewAttachmentPriv(ItemKey key);
}
