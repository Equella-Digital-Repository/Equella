package com.tle.common.searching;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.ItemSelect;
import com.tle.common.Check;

/**
 * @author Nicholas Read
 */
public class VeryBasicSearch implements Search, Serializable
{
	private static final long serialVersionUID = 1L;

	protected String query;
	protected Collection<String> queryTokens;
	protected FreeTextQuery freeTextQuery;

	public VeryBasicSearch()
	{
	}

	public VeryBasicSearch(String query)
	{
		this.query = query;
	}

	@Override
	public FreeTextQuery getFreeTextQuery()
	{
		return freeTextQuery;
	}

	public void setFreeTextQuery(FreeTextQuery freeTextQuery)
	{
		this.freeTextQuery = freeTextQuery;
	}

	@Override
	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	@Override
	public Collection<String> getTokenisedQuery()
	{
		if( queryTokens == null )
		{
			queryTokens = new HashSet<String>();
			if( !Check.isEmpty(query) )
			{
				final String[] toks = query.split("\\s"); //$NON-NLS-1$
				queryTokens.addAll(Arrays.asList(toks));
			}
		}
		return queryTokens;
	}

	@Override
	public String getPrivilegeToCollect()
	{
		return null;
	}

	@Override
	public SortField[] getSortFields()
	{
		return new SortField[]{SortType.NAME.getSortField()};
	}

	@Override
	public String getPrivilege()
	{
		return null;
	}

	@Override
	public Date[] getDateRange()
	{
		return null;
	}

	@Override
	public String getSearchType()
	{
		return "item"; //$NON-NLS-1$
	}

	@Override
	public List<List<Field>> getMust()
	{
		return null;
	}

	@Override
	public List<List<Field>> getMustNot()
	{
		return null;
	}

	public boolean processIfItemsDeleteable()
	{
		return false;
	}

	@Override
	public ItemSelect getSelect()
	{
		return null;
	}

	@Override
	public List<Field> getMatrixFields()
	{
		return null;
	}

	@Override
	public List<String> getExtraQueries()
	{
		return null;
	}

	@Override
	public Collection<DateFilter> getDateFilters()
	{
		return null;
	}

	@Override
	public String getPrivilegePrefix()
	{
		return null;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(query, queryTokens, freeTextQuery);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null || !(obj instanceof VeryBasicSearch) )
		{
			return false;
		}
		else if( this == obj )
		{
			return true;
		}
		else
		{
			VeryBasicSearch rhs = (VeryBasicSearch) obj;
			return Objects.equals(query, rhs.query) && Objects.equals(queryTokens, rhs.queryTokens)
				&& Objects.equals(freeTextQuery, rhs.freeTextQuery);
		}
	}
}
