package com.tle.web.itemadmin.section;

import it.uniroma3.mat.extendedset.wrappers.LongSet;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.search.DefaultSearch;
import com.tle.core.services.item.FreeTextService;
import com.tle.web.bulk.section.AbstractBulkResultsDialog;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.annotations.Component;

@NonNullByDefault
public class ItemAdminSelectionSection extends AbstractBulkSelectionSection<ItemIdKey>
{
	private static final String KEY_SELECTIONS = "itemadminSelections"; //$NON-NLS-1$

	@PlugKey("selectionsbox.selectall")
	private static Label LABEL_SELECTALL;
	@PlugKey("selectionsbox.unselect")
	private static Label LABEL_UNSELECTALL;
	@PlugKey("selectionsbox.viewselected")
	private static Label LABEL_VIEWSELECTED;
	@PlugKey("selectionsbox.pleaseselect")
	private static Label LABEL_PLEASE;
	@PlugKey("selectionsbox.count")
	private static String LABEL_COUNT;

	@Component
	@Inject
	private ItemAdminResultsDialog bulkDialog;
	@Inject
	private FreeTextService freeTextService;

	@TreeLookup
	private AbstractFreetextResultsSection<?, ?> resultsSection;

	@Override
	@EventHandlerMethod
	public void selectAll(SectionInfo info)
	{
		FreetextSearchEvent searchEvent = resultsSection.createSearchEvent(info);
		info.processEvent(searchEvent);
		DefaultSearch search = searchEvent.getFinalSearch();
		LongSet bitSet = freeTextService.searchIdsBitSet(search);
		Model<ItemIdKey> model = getModel(info);
		model.setBitSet(bitSet);
		model.setModifiedSelection(true);
	}

	@Override
	protected String getKeySelections()
	{
		return KEY_SELECTIONS;
	}

	@Override
	protected Label getLabelSelectAll()
	{
		return LABEL_SELECTALL;
	}

	@Override
	protected Label getLabelUnselectAll()
	{
		return LABEL_UNSELECTALL;
	}

	@Override
	protected Label getLabelViewSelected()
	{
		return LABEL_VIEWSELECTED;
	}

	@Override
	protected Label getPleaseSelectLabel()
	{
		return LABEL_PLEASE;
	}

	@Override
	protected Label getSelectionBoxCountLabel(int selectionCount)
	{
		return new PluralKeyLabel(LABEL_COUNT, selectionCount);
	}

	@Override
	protected AbstractBulkResultsDialog<ItemIdKey> getBulkDialog()
	{
		return bulkDialog;
	}

	@Override
	protected boolean useBitSet()
	{
		return true;
	}
}
