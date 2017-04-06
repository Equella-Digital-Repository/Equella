package com.tle.web.remoterepo.srw;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.FederatedSearch;
import com.tle.core.remoterepo.srw.service.SrwService;
import com.tle.core.remoterepo.srw.service.impl.SrwSearchResult;
import com.tle.core.remoterepo.srw.service.impl.SrwSearchResults;
import com.tle.web.remoterepo.RemoteRepoListEntryFactory;
import com.tle.web.remoterepo.event.RemoteRepoSearchResultEvent;
import com.tle.web.remoterepo.section.RemoteRepoResultsSection;
import com.tle.web.remoterepo.section.RemoteRepoResultsSection.RemoteRepoResultsModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;

/**
 * @author aholland
 */
public class SrwResultsSection
	extends
		RemoteRepoResultsSection<SrwSearchEvent, SrwSearchResult, RemoteRepoResultsModel>
{
	@Inject
	private SrwService srwService;
	@Inject
	private SrwListEntryFactory srwFac;

	/**
	 * @see com.tle.web.search.base.AbstractSearchResultsSection#getErrorMessage(com.tle.web.sections.SectionInfo,
	 *      com.tle.web.sections.equella.search.event.AbstractSearchEvent,
	 *      com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent)
	 */
	@Override
	protected List<Label> getErrorMessageLabels(SectionInfo info, SrwSearchEvent searchEvent,
		RemoteRepoSearchResultEvent<SrwSearchResult> resultsEvent)
	{
		// TODO Auto-generated method stub
		return super.getErrorMessageLabels(info, searchEvent, resultsEvent);
	}

	@Override
	protected SrwSearchResults doSearch(SectionInfo info, SrwSearchEvent search)
	{
		return srwService.search(search.getSearch(), search.getQuery(), search.getOffset(), search.getCount());
	}

	@Override
	protected RemoteRepoListEntryFactory<SrwSearchResult> getEntryFactory()
	{
		return srwFac;
	}

	@Override
	protected SrwSearchEvent makeSearchEvent(SectionInfo info, FederatedSearch fedSearch)
	{
		return new SrwSearchEvent(getRootRemoteRepoSection(), fedSearch);
	}
}
