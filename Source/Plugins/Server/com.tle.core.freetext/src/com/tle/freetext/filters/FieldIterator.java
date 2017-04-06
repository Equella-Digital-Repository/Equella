package com.tle.freetext.filters;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

public class FieldIterator implements Iterator<Term>
{
	private final String field;

	private TermEnum enumerator;
	private Term current;

	public FieldIterator(IndexReader reader, String field, String start) throws IOException
	{
		this.field = field;

		enumerator = reader.terms(new Term(field, start));
		current = enumerator.term();
		checkSameField();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return current != null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Term next()
	{
		try
		{
			Term result = current;
			if( enumerator.next() )
			{
				current = enumerator.term();
			}
			else
			{
				current = null;
			}
			checkSameField();
			return result;
		}
		catch( IOException ex )
		{
			throw new RuntimeException("Error with Lucene TermEnum", ex);
		}
	}

	private void checkSameField() throws IOException
	{
		if( current != null && !current.field().equals(field) )
		{
			current = null;
		}

		if( current == null )
		{
			enumerator.close();
			enumerator = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
