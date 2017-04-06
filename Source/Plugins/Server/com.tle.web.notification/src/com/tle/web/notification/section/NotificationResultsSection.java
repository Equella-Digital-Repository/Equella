package com.tle.web.notification.section;

import java.util.List;

import com.google.inject.Inject;
import com.tle.common.search.DefaultSearch;
import com.tle.core.notification.indexer.NotificationSearch;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.notification.NotificationItemListEntry;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class NotificationResultsSection
	extends
		AbstractFreetextResultsSection<NotificationItemListEntry, AbstractSearchResultsSection.SearchResultsModel>
{
	@PlugKey("noresults.items")
	private static Label LABEL_NOAVAILABLE;
	@PlugKey("noresults.items.filtered")
	private static Label LABEL_NORESULTS;

	@Inject
	private NotificationItemList itemList;

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		tree.registerInnerSection(itemList, id);
	}

	@Override
	public NotificationItemList getItemList(SectionInfo info)
	{
		return itemList;
	}

	@Override
	protected DefaultSearch createDefaultSearch(SectionInfo info)
	{
		return new NotificationSearch();
	}

	@Override
	protected Label getNoResultsTitle(SectionInfo info, FreetextSearchEvent searchEvent,
		FreetextSearchResultEvent resultsEvent)
	{
		if( !searchEvent.isFiltered() )
		{
			return LABEL_NOAVAILABLE;
		}
		return LABEL_NORESULTS;
	}

	@Override
	protected void addAjaxUpdateDivs(SectionTree tree, List<String> ajaxList)
	{
		super.addAjaxUpdateDivs(tree, ajaxList);
		ajaxList.add(AbstractBulkSelectionSection.DIVID_SELECTBOX);
	}

}
