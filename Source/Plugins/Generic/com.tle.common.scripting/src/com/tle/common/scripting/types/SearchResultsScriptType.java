package com.tle.common.scripting.types;

import java.util.List;

/**
 * A search results type for use in scripts
 */
public interface SearchResultsScriptType
{
	/**
	 * @return The number of results available if an unlimited search were
	 *         performed.
	 */
	int available();

	/**
	 * @return The results of the search as a list of items.
	 */
	List<ItemScriptType> getResults();
}
