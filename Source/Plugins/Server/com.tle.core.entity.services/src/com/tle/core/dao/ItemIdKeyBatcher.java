package com.tle.core.dao;

import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNull;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.dao.helpers.BatchingIterator;
import com.tle.core.user.CurrentInstitution;

/**
 * Iterates over possibly enormous numbers of ItemIdKey results by doing
 * batching behind the scenes.
 * 
 * @author Nick Read
 */
public abstract class ItemIdKeyBatcher extends BatchingIterator<ItemIdKey>
{
	private static final int BATCH_SIZE = 100;

	private final ItemDao dao;

	public ItemIdKeyBatcher(@NonNull ItemDao dao)
	{
		this.dao = dao;
	}

	public long getTotalCount()
	{
		return dao.getCount(joinClause(), wrapWhereClause(), getParams());
	}

	@Override
	protected Iterator<ItemIdKey> getMore(Optional<ItemIdKey> lastObj)
	{
		long startId = lastObj.isPresent() ? lastObj.get().getKey() + 1 : 1;
		return dao.getItemKeyBatch(joinClause(), wrapWhereClause(), getParams(), startId, BATCH_SIZE)
			.iterator();
	}

	@SuppressWarnings("nls")
	private String wrapWhereClause()
	{
		return "(" + whereClause() + ") AND i.institution = :institution";
	}

	@SuppressWarnings("nls")
	private Map<String, Object> getParams()
	{
		Map<String, Object> params = Maps.newHashMap();
		params.put("institution", CurrentInstitution.get());
		addParameters(params);
		return params;
	}

	protected abstract String joinClause();

	protected abstract String whereClause();

	protected abstract void addParameters(Map<String, Object> params);
}
