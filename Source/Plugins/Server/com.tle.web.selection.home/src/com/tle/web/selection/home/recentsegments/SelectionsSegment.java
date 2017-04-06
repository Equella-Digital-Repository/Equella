package com.tle.web.selection.home.recentsegments;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.ItemId;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.SelectionHistory;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.home.model.RecentSelectionSegmentModel.RecentSelection;
import com.tle.web.viewurl.ViewItemUrlFactory;

public class SelectionsSegment extends AbstractRecentSegment
{
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private SelectionService selectionService;

	@Override
	protected List<RecentSelection> getSelections(SectionInfo info, SelectionSession session, int maximum)
	{
		List<SelectionHistory> list = selectionService.getRecentSelections(info, maximum);
		List<RecentSelection> selections = new ArrayList<RecentSelection>();
		for( SelectionHistory resource : list )
		{
			HtmlLinkState state = new HtmlLinkState(urlFactory.createItemUrl(info, new ItemId(resource.getUuid(),
				resource.getVersion())));
			selections.add(new RecentSelection(resource, state));
		}
		return selections;
	}

	@Override
	public String getTitle(SectionInfo info, SelectionSession session)
	{
		return CurrentLocale.get("com.tle.web.selection.home.recently.selected"); //$NON-NLS-1$
	}

}
