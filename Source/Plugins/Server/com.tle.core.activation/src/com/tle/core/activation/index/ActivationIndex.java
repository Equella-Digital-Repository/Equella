/*
 * Created on Aug 31, 2005
 */
package com.tle.core.activation.index;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.lucene.document.Document;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.collect.ImmutableMap;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.activation.ActivationResult;
import com.tle.core.guice.Bind;
import com.tle.freetext.index.MultipleIndex;

@Bind
@Singleton
public class ActivationIndex extends MultipleIndex<ActivationResult>
{
	private static final Map<String, String> privMap = ImmutableMap.of(ActivationConstants.VIEW_ACTIVATION_ITEM,
		ActivationConstants.VIEW_ACTIVATION_ITEM_PFX, ActivationConstants.DELETE_ACTIVATION_ITEM,
		ActivationConstants.DELETE_ACTIVATION_ITEM_PFX);

	@Override
	protected Set<String> getKeyFields()
	{
		return new HashSet<String>(Arrays.asList(FreeTextQuery.FIELD_UNIQUE, FreeTextQuery.FIELD_ID,
			FreeTextQuery.FIELD_ACTIVATION_ID));
	}

	@Override
	public String getIndexId()
	{
		return ActivationConstants.ACTIVATION_INDEX_ID;
	}

	@Override
	protected String getPrefixForPrivilege(String priv)
	{
		if( privMap.containsKey(priv) )
		{
			return privMap.get(priv);
		}
		return super.getPrefixForPrivilege(priv);
	}

	@Override
	protected ActivationResult createResult(ItemIdKey key, Document doc, float relevance, boolean sortByRelevance)
	{
		return new ActivationResult(key, doc.get(FreeTextQuery.FIELD_ACTIVATION_ID), relevance, sortByRelevance);
	}
}
