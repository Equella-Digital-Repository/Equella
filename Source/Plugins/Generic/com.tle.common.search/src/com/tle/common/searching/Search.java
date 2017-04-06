package com.tle.common.searching;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.ItemSelect;

public interface Search
{
	String INDEX_TASK = "task"; //$NON-NLS-1$
	String INDEX_ITEM = "item"; //$NON-NLS-1$

	public enum SortType
	{
		RANK(null, false), DATEMODIFIED(FreeTextQuery.FIELD_REALLASTMODIFIED, true), DATECREATED(
			FreeTextQuery.FIELD_REALCREATED, true), NAME(FreeTextQuery.FIELD_NAME, false), FORCOUNT(null, false),
		RATING(FreeTextQuery.FIELD_RATING, true);

		private final String field;
		private final boolean reverse;

		private SortType(String field, boolean reverse)
		{
			this.field = field;
			this.reverse = reverse;
		}

		public SortField getSortField()
		{
			return getSortField(false);
		}

		public SortField getSortField(boolean reverseTheDefault)
		{
			boolean r = reverseTheDefault ? !reverse : reverse;
			return new SortField(field, r, field == null ? SortField.Type.SCORE : SortField.Type.STRING);
		}
	}

	ItemSelect getSelect();

	FreeTextQuery getFreeTextQuery();

	String getQuery();

	List<String> getExtraQueries();

	Collection<String> getTokenisedQuery();

	SortField[] getSortFields();

	Date[] getDateRange();

	String getSearchType();

	List<Field> getMatrixFields();

	List<List<Field>> getMust();

	List<List<Field>> getMustNot();

	String getPrivilege();

	String getPrivilegePrefix();

	String getPrivilegeToCollect();

	Collection<DateFilter> getDateFilters();

}
