package com.tle.core.services.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.tle.core.services.TaskStatusChange;

public class MapTaskStatusChange implements TaskStatusChange<MapTaskStatusChange>
{
	private static final long serialVersionUID = 1L;
	private SetMultimap<String, Object> removals = HashMultimap.create();
	private Map<String, Map<Object, Object>> puts = Maps.newHashMap();
	// Only used during setup, so this property doesn't need to be serialised.
	private final transient String mapKey; // NOSONAR

	public MapTaskStatusChange(String mapKey)
	{
		this.mapKey = mapKey;
	}

	@Override
	public void merge(MapTaskStatusChange newChanges)
	{
		for( Entry<String, Map<Object, Object>> entry : newChanges.puts.entrySet() )
		{
			for( Entry<Object, Object> oneEntry : entry.getValue().entrySet() )
			{
				putInternal(entry.getKey(), oneEntry.getKey(), oneEntry.getValue());
			}
		}
		for( Entry<String, Object> entry : newChanges.removals.entries() )
		{
			removeInteral(entry.getKey(), entry.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void modifyStatus(TaskStatusImpl taskStatus)
	{
		Map<String, Serializable> stateMap = taskStatus.getStateMap();
		for( String key : removals.keySet() )
		{
			Map<Object, Object> map = (Map<Object, Object>) stateMap.get(key);
			if( map == null )
			{
				map = Maps.newHashMap();
				stateMap.put(key, (Serializable) map);
			}
			Collection<Object> removeKeys = removals.get(key);
			for( Object removeKey : removeKeys )
			{
				map.remove(removeKey);
			}
		}

		for( Entry<String, Map<Object, Object>> entry : puts.entrySet() )
		{
			String key = entry.getKey();
			Map<Object, Object> map = (Map<Object, Object>) stateMap.get(key);
			if( map == null )
			{
				map = Maps.newHashMap();
				stateMap.put(key, (Serializable) map);
			}
			map.putAll(entry.getValue());
		}
	}

	public void remove(String key)
	{
		removeInteral(mapKey, key);
	}

	public void put(Object key, Object value)
	{
		putInternal(mapKey, key, value);
	}

	private void removeInteral(String mapKey, Object key)
	{
		removals.put(mapKey, key);
		Map<Object, Object> map = puts.get(mapKey);
		if( map != null )
		{
			map.remove(key);
		}
	}

	private void putInternal(String mapKey, Object key, Object value)
	{
		removals.remove(mapKey, key);
		Map<Object, Object> map = puts.get(mapKey);
		if( map == null )
		{
			map = Maps.newHashMap();
			puts.put(mapKey, map);
		}
		map.put(key, value);
	}

}
