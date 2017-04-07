package com.dytech.common.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Nicholas Read
 */
public class CombinedCollection<T> implements Collection<T>
{
	private final Collection<T> first;
	private final Collection<T> second;

	public CombinedCollection(Collection<T> first, Collection<T> second)
	{
		if( first != null )
		{
			this.first = first;
		}
		else
		{
			this.first = Collections.emptyList();
		}

		if( second != null )
		{
			this.second = second;
		}
		else
		{
			this.second = Collections.emptyList();
		}
	}

	@Override
	public int size()
	{
		return first.size() + second.size();
	}

	@Override
	public boolean isEmpty()
	{
		return first.isEmpty() && second.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return first.contains(o) || second.contains(o);
	}

	@Override
	public Iterator<T> iterator()
	{
		return new Iterator<T>()
		{
			boolean isFirst = true;
			Iterator<? extends T> i = first.iterator();

			@Override
			public boolean hasNext()
			{
				ensureIter();
				return i.hasNext();
			}

			private void ensureIter()
			{
				if( isFirst && !i.hasNext() )
				{
					isFirst = false;
					i = second.iterator();
				}
			}

			@Override
			public T next()
			{
				ensureIter();
				return i.next();
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public boolean containsAll(Collection<?> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E> E[] toArray(E[] a)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(T o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends T> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}
}
