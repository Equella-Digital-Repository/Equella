/*
 * Created on May 24, 2005
 */
package com.dytech.common.xml;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.google.common.base.Throwables;

/**
 * 
 */
public class ReflectionProvider
{
	private static Class<?>[] BLANK_CLASSES = new Class[0];
	private static Object[] BLANK_OBJECTS = new Object[0];

	public ReflectionProvider()
	{
		super();
	}

	public Object newInstance(Class<?> type)
	{
		Object instance = null;
		try
		{
			Constructor<?> constructor = type.getDeclaredConstructor(BLANK_CLASSES);
			constructor.setAccessible(true);
			instance = constructor.newInstance(BLANK_OBJECTS);
		}
		catch( Exception e )
		{
			throw new Error("Unable to find constructor for class: " + type, e); //$NON-NLS-1$
		}
		return instance;
	}

	public void writeField(Object object, String fieldName, Object value)
	{
		if( object == null )
		{
			return;
		}

		Class<?> definedIn = object.getClass();
		Class<?> type = getField(definedIn, fieldName).getType();
		boolean write = true;
		try
		{
			if( type == Integer.TYPE )
			{
				value = Integer.valueOf(value.toString());
			}
			else if( type == Boolean.TYPE )
			{
				value = Boolean.valueOf(value.toString());
			}
			else if( type == Long.TYPE )
			{
				value = Long.valueOf(value.toString());
			}
			else if( type == Float.TYPE )
			{
				value = Float.valueOf(value.toString());
			}
			else if( type == Short.TYPE )
			{
				value = Short.valueOf(value.toString());
			}
			else if( type == Double.TYPE )
			{
				value = Double.valueOf(value.toString());
			}

		}
		catch( Exception e )
		{
			write = false;
		}

		if( write )
		{
			try
			{
				getField(definedIn, fieldName).set(object, value);
			}
			catch( IllegalArgumentException e )
			{
				Throwables.propagate(e);
			}
			catch( IllegalAccessException e )
			{
				Throwables.propagate(e);
			}
		}
	}

	public Object getField(Object object, String field)
	{
		Object value = null;
		if( object != null )
		{
			Field ffield = getField(object.getClass(), field);
			if( ffield != null )
			{
				try
				{
					value = ffield.get(object);
				}
				catch( IllegalArgumentException e )
				{
					Throwables.propagate(e);
				}
				catch( IllegalAccessException e )
				{
					Throwables.propagate(e);
				}
			}
		}
		return value;

	}

	protected Field getField(Class<?> c, String name)
	{
		Field field = null;

		while( field == null && c != null )
		{
			try
			{
				field = c.getDeclaredField(name);
				field.setAccessible(true);
			}
			catch( Exception ex )
			{
				c = c.getSuperclass();
			}
		}
		if( field == null )
		{
			throw new RuntimeException(name + " doesn't exist in class " + c); //$NON-NLS-1$
		}
		return field;
	}
}
