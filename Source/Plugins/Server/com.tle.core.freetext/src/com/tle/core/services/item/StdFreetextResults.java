package com.tle.core.services.item;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemSelect;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;

public class StdFreetextResults<T extends FreetextResult> implements FreetextSearchResults<T>
{
	private static final long serialVersionUID = 1L;

	private final ItemService itemService;
	private final SearchResults<T> results;
	private List<Item> items;
	private final ItemSelect itemSelect;

	public StdFreetextResults(ItemService itemService, SearchResults<T> results, Search search)
	{
		this.itemService = itemService;
		this.results = results;
		this.itemSelect = search.getSelect();
	}

	@Override
	public List<Item> getResults()
	{
		if( items == null )
		{
			items = itemService.queryItems(convertToKeys(results.getResults()), itemSelect);
		}
		return items;
	}

	public static <T extends FreetextResult> List<ItemIdKey> convertToKeys(List<T> results)
	{
		List<ItemIdKey> keys = new ArrayList<ItemIdKey>();
		for( T result : results )
		{
			keys.add(result.getItemIdKey());
		}
		return keys;
	}

	@Override
	public int getAvailable()
	{
		return results.getAvailable();
	}

	@Override
	public int getCount()
	{
		return results.getCount();
	}

	@Override
	public int getOffset()
	{
		return results.getOffset();
	}

	@Override
	public T getResultData(int index)
	{
		return results.getResults().get(index);
	}

	@Override
	public Item getItem(int index)
	{
		return getResults().get(index);
	}

	@Override
	public String getErrorMessage()
	{
		return null;
	}

	@Override
	public void setErrorMessage(String errorMessage)
	{
		// do nothing
	}

	/**
	 * Standard search unafflicted with keyResources
	 * 
	 * @return
	 */
	@Override
	public int getKeyResourcesSize()
	{
		return 0;
	}
}
