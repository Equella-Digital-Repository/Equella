/*
 * Created on Jun 17, 2005
 */
package com.dytech.common.xml.mapping;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * 
 */
public class DataMapMapping extends MapMapping
{
	private final Class keyType;
	private final Class valueType;

	public DataMapMapping(String name, String node, String keyPath, String valuePath, Class keyType, Class valueType)
	{
		this(name, node, null, keyPath, valuePath, keyType, valueType);
	}

	public DataMapMapping(String name, String node, String keyPath, String valuePath, Class valueType)
	{
		this(name, node, null, keyPath, valuePath, valueType);
	}

	public DataMapMapping(String name, String node, Class type, String keyPath, String valuePath, Class valueType)
	{
		this(name, node, type, keyPath, valuePath, null, valueType);
	}

	public DataMapMapping(String name, String node, Class type, String keyPath, String valuePath, Class keyType,
		Class valueType)
	{
		super(name, node, type, keyPath, valuePath);
		this.keyType = keyType;
		this.valueType = valueType;
	}

	@Override
	protected AbstractMapping createMapNodeMapping(String path, MapHolder holder, boolean isKey)
	{
		Class clazz;
		if( isKey )
		{
			clazz = keyType;
		}
		else
		{
			clazz = valueType;
		}
		AbstractMapping mapping = null;
		if( clazz == null )
		{
			mapping = super.createMapNodeMapping(path, holder, isKey);
		}
		else
		{
			mapping = new MapDataNodeMapping(path, clazz, holder, isKey);
		}
		return mapping;
	}

	private static class MapDataNodeMapping extends DataMapping
	{
		private final boolean key;
		private final MapHolder holder;

		public MapDataNodeMapping(String node, Class clazz, MapHolder holder, boolean key)
		{
			super("", node, clazz);
			this.holder = holder;
			this.key = key;
		}

		@Override
		public void unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context, Object object)
		{
			Object value = getUnmarshalledValue(object, reader, context);
			holder.add(value, key);
		}

		@Override
		public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
		{
			object = ((MapKeyValue) object).getValue(key);
			super.marshal(writer, context, object);
		}

		@Override
		public boolean hasValue(Object object)
		{
			return true;
		}
	}
}
