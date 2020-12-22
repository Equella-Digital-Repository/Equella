/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Card, CardContent, Grid, Typography } from "@material-ui/core";
import * as OEQ from "@openequella/rest-api-client";

import { isEqual, pick } from "lodash";
import * as React from "react";
import { useEffect, useRef, useState } from "react";
import { useHistory, useLocation } from "react-router";
import { Static } from "runtypes";
import { generateFromError } from "../api/errors";
import { AppConfig } from "../AppConfig";
import { DateRangeSelector } from "../components/DateRangeSelector";
import MessageInfo from "../components/MessageInfo";
import { routes } from "../mainui/routes";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps,
} from "../mainui/Template";
import { getAdvancedSearchesFromServer } from "../modules/AdvancedSearchModule";
import type { Collection } from "../modules/CollectionsModule";
import { getRemoteSearchesFromServer } from "../modules/RemoteSearchModule";
import {
  Classification,
  listClassifications,
  SelectedCategories,
} from "../modules/SearchFacetsModule";
import {
  DateRange,
  defaultPagedSearchResult,
  defaultSearchOptions,
  generateQueryStringFromSearchOptions,
  queryStringParamsToSearchOptions,
  searchItems,
  SearchOptions,
} from "../modules/SearchModule";
import {
  getSearchSettingsFromServer,
  SearchSettings,
  SortOrder,
} from "../modules/SearchSettingsModule";
import {
  prepareDraggable,
  buildSelectionSessionAdvancedSearchLink,
  buildSelectionSessionRemoteSearchLink,
  isSelectionSessionInStructured,
} from "../modules/LegacySelectionSessionModule";
import SearchBar from "../search/components/SearchBar";
import { languageStrings } from "../util/langstrings";
import { AuxiliarySearchSelector } from "./components/AuxiliarySearchSelector";
import { CategorySelector } from "./components/CategorySelector";
import { CollectionSelector } from "./components/CollectionSelector";
import OwnerSelector from "./components/OwnerSelector";
import {
  RefinePanelControl,
  RefineSearchPanel,
} from "./components/RefineSearchPanel";
import { SearchAttachmentsSelector } from "./components/SearchAttachmentsSelector";
import {
  mapSearchResultItems,
  SearchResultList,
} from "./components/SearchResultList";
import StatusSelector from "./components/StatusSelector";

/**
 * Type of search options that are specific to Search page presentation layer.
 */
export interface SearchPageOptions extends SearchOptions {
  /**
   * Whether to enable Quick mode (true) or to use custom date pickers (false).
   */
  dateRangeQuickModeEnabled: boolean;
}

/**
 * Structure of data stored in browser history state, to capture the current state of SearchPage
 */
interface SearchPageHistoryState {
  /**
   * SearchPageOptions to store in history
   */
  searchPageOptions: SearchPageOptions;
  /**
   * Open/closed state of refine expansion panel
   */
  filterExpansion: boolean;
}

const SearchPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const searchStrings = languageStrings.searchpage;
  const {
    title: dateModifiedSelectorTitle,
    quickOptionDropdown,
  } = searchStrings.lastModifiedDateSelector;
  const { title: collectionSelectorTitle } = searchStrings.collectionSelector;

  const history = useHistory();
  const defaultSearchPageOptions: SearchPageOptions = {
    ...defaultSearchOptions,
    dateRangeQuickModeEnabled: true,
  };

  const defaultSearchPageHistory: SearchPageHistoryState = {
    searchPageOptions: defaultSearchPageOptions,
    filterExpansion: false,
  };
  const searchPageHistoryState: SearchPageHistoryState | undefined = history
    .location.state as SearchPageHistoryState;
  const [searchPageOptions, setSearchPageOptions] = useState<SearchPageOptions>(
    // If the user has gone 'back' to this page, then use their previous options. Otherwise
    // we start fresh - i.e. if a new navigation to Search Page.
    searchPageHistoryState?.searchPageOptions ??
      defaultSearchPageHistory.searchPageOptions
  );
  const [filterExpansion, setFilterExpansion] = useState(
    searchPageHistoryState?.filterExpansion ??
      defaultSearchPageHistory.filterExpansion
  );
  const [pagedSearchResult, setPagedSearchResult] = useState<
    OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>
  >(defaultPagedSearchResult);
  const [showSpinner, setShowSpinner] = useState<boolean>(false);
  const [showSnackBar, setShowSnackBar] = useState<boolean>(false);
  const [searchSettings, setSearchSettings] = useState<SearchSettings>();
  const [classifications, setClassifications] = useState<Classification[]>([]);

  const location = useLocation();

  const handleError = (error: Error) => {
    updateTemplate(templateError(generateFromError(error)));
  };

  /**
   * Update the page title and retrieve Search settings.
   */
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchStrings.title)(tp),
    }));
    // Show spinner before calling API to retrieve Search settings.
    setShowSpinner(true);

    Promise.all([
      getSearchSettingsFromServer(),
      // If the search options are available from browser history, ignore those in the query string.
      searchPageHistoryState
        ? Promise.resolve(undefined)
        : queryStringParamsToSearchOptions(location),
    ])
      .then(([searchSettings, queryStringSearchOptions]) => {
        setSearchSettings(searchSettings);
        if (queryStringSearchOptions)
          setSearchPageOptions({
            ...queryStringSearchOptions,
            dateRangeQuickModeEnabled: false,
            sortOrder:
              queryStringSearchOptions.sortOrder ??
              searchSettings.defaultSearchSort,
          });
        else
          setSearchPageOptions({
            ...searchPageOptions,
            sortOrder:
              searchPageOptions.sortOrder ?? searchSettings.defaultSearchSort,
          });
      })
      .catch((e) => {
        setShowSpinner(false);
        handleError(e);
      });
  }, []);

  const isInitialSearch = useRef(true);
  // Trigger a search when Search options get changed, but skip the initial values.
  useEffect(() => {
    if (isInitialSearch.current) {
      isInitialSearch.current = false;
    } else {
      search();
    }
  }, [searchPageOptions]);

  useEffect(() => {
    // If search page is open from a URL which has some query params, updating browser
    // history for the initial search will produce wrong search option for the second
    // render. This is because in the second render, we ignore the query param conversion
    // as the history data has higher priority. So the end result is wrong search options
    // are displayed in the page.
    if (isInitialSearch && searchPageOptions === defaultSearchPageOptions) {
      return;
    }
    history.replace({
      ...history.location,
      state: { searchPageOptions, filterExpansion },
    });
  }, [filterExpansion, pagedSearchResult]);

  // In Selection Session, once a new search result is returned, make each
  // new search result Item draggable.
  useEffect(() => {
    if (isSelectionSessionInStructured()) {
      pagedSearchResult.results.forEach(({ uuid }) => {
        prepareDraggable(uuid);
      });
    }
  }, [pagedSearchResult]);

  // When Search options get changed, also update the Classification list.
  useEffect(() => {
    if (!isInitialSearch.current) {
      listClassifications(searchPageOptions)
        .then((classifications) => setClassifications(classifications))
        .catch(handleError);
    }
  }, [searchPageOptions]);

  /**
   * Search items with specified search criteria and show a spinner when the search is in progress.
   */
  const search = (): void => {
    setShowSpinner(true);
    searchItems(searchPageOptions)
      .then((items: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>) => {
        setPagedSearchResult(items);
        // scroll back up to the top of the page
        window.scrollTo(0, 0);
      })
      .catch(handleError)
      .finally(() => setShowSpinner(false));
  };

  const handleSortOrderChanged = (order: Static<typeof SortOrder>) =>
    setSearchPageOptions({ ...searchPageOptions, sortOrder: order });

  const handleQueryChanged = (query: string) =>
    setSearchPageOptions({
      ...searchPageOptions,
      query: query,
      currentPage: 0,
      selectedCategories: undefined,
    });

  const handleCollectionSelectionChanged = (collections: Collection[]) => {
    setSearchPageOptions({
      ...searchPageOptions,
      collections: collections,
      currentPage: 0,
      selectedCategories: undefined,
    });
  };

  const handleCollapsibleFilterClick = () => {
    setFilterExpansion(!filterExpansion);
  };

  /**
   * Determines if any collapsible filters have been modified from their defaults
   */
  const areCollapsibleFiltersSet = (): boolean => {
    const getCollapsibleOptions = (options: SearchOptions) =>
      pick(options, [
        "lastModifiedDateRange",
        "owner",
        "status",
        "searchAttachments",
      ]);

    return !isEqual(
      getCollapsibleOptions(defaultSearchOptions),
      getCollapsibleOptions(searchPageOptions)
    );
  };

  const handlePageChanged = (page: number) =>
    setSearchPageOptions({ ...searchPageOptions, currentPage: page });

  const handleRowsPerPageChanged = (rowsPerPage: number) =>
    setSearchPageOptions({
      ...searchPageOptions,
      currentPage: 0,
      rowsPerPage: rowsPerPage,
    });

  const handleRawModeChanged = (rawMode: boolean) =>
    setSearchPageOptions({ ...searchPageOptions, rawMode: rawMode });

  const handleQuickDateRangeModeChange = (
    quickDateRangeMode: boolean,
    dateRange?: DateRange
  ) =>
    setSearchPageOptions({
      ...searchPageOptions,
      dateRangeQuickModeEnabled: quickDateRangeMode,
      // When the mode is changed, the date range may also need to be updated.
      // For example, if a custom date range is converted to Quick option 'All', then both start and end should be undefined.
      lastModifiedDateRange: dateRange,
      selectedCategories: undefined,
    });

  const handleLastModifiedDateRangeChange = (dateRange?: DateRange) =>
    setSearchPageOptions({
      ...searchPageOptions,
      lastModifiedDateRange: dateRange,
      selectedCategories: undefined,
    });

  const handleClearSearchOptions = () => {
    setSearchPageOptions({
      ...defaultSearchPageOptions,
      sortOrder: searchSettings?.defaultSearchSort,
    });
    setFilterExpansion(false);
  };

  const copySearchInfoSnackbar = (
    <MessageInfo
      open={showSnackBar}
      onClose={() => setShowSnackBar(false)}
      title={searchStrings.shareSearchConfirmationText}
      variant="success"
    />
  );

  const handleCopySearch = () => {
    //base institution urls have a trailing / that we need to get rid of
    const instUrl = AppConfig.baseUrl.slice(0, -1);
    const searchUrl = `${instUrl}${
      location.pathname
    }?${generateQueryStringFromSearchOptions(searchPageOptions)}`;

    navigator.clipboard
      .writeText(searchUrl)
      .then(() => {
        setShowSnackBar(true);
      })
      .catch(() => handleError);
  };

  const handleOwnerChange = (owner: OEQ.UserQuery.UserDetails) =>
    setSearchPageOptions({
      ...searchPageOptions,
      owner: { ...owner },
      selectedCategories: undefined,
    });

  const handleOwnerClear = () =>
    setSearchPageOptions({
      ...searchPageOptions,
      owner: undefined,
      selectedCategories: undefined,
    });

  const handleStatusChange = (status: OEQ.Common.ItemStatus[]) =>
    setSearchPageOptions({
      ...searchPageOptions,
      status: [...status],
      selectedCategories: undefined,
    });

  const handleSearchAttachmentsChange = (searchAttachments: boolean) => {
    setSearchPageOptions({
      ...searchPageOptions,
      searchAttachments: searchAttachments,
    });
  };

  const handleSelectedCategoriesChange = (
    selectedCategories: SelectedCategories[]
  ) => {
    const getSchemaNode = (id: number) => {
      const node = classifications.find((c) => c.id === id)?.schemaNode;
      if (!node) {
        throw new Error(`Unable to find schema node for classification ${id}.`);
      }
      return node;
    };

    setSearchPageOptions({
      ...searchPageOptions,
      selectedCategories: selectedCategories.map((c) => ({
        ...c,
        schemaNode: getSchemaNode(c.id),
      })),
    });
  };

  const refinePanelControls: RefinePanelControl[] = [
    {
      idSuffix: "CollectionSelector",
      title: collectionSelectorTitle,
      component: (
        <CollectionSelector
          onError={handleError}
          onSelectionChange={handleCollectionSelectionChanged}
          value={searchPageOptions.collections}
        />
      ),
      disabled: false,
      alwaysVisible: true,
    },
    {
      idSuffix: "AdvancedSearchSelector",
      title: searchStrings.advancedSearchSelector.title,
      component: (
        <AuxiliarySearchSelector
          auxiliarySearchesSupplier={getAdvancedSearchesFromServer}
          urlGeneratorForRouteLink={routes.AdvancedSearch.to}
          urlGeneratorForMuiLink={buildSelectionSessionAdvancedSearchLink}
        />
      ),
      disabled: false,
      alwaysVisible: true,
    },
    {
      idSuffix: "RemoteSearchSelector",
      title: searchStrings.remoteSearchSelector.title,
      component: (
        <AuxiliarySearchSelector
          auxiliarySearchesSupplier={getRemoteSearchesFromServer}
          urlGeneratorForRouteLink={routes.RemoteSearch.to}
          urlGeneratorForMuiLink={buildSelectionSessionRemoteSearchLink}
        />
      ),
      disabled: false,
    },
    {
      idSuffix: "DateRangeSelector",
      title: dateModifiedSelectorTitle,
      component: (
        <DateRangeSelector
          onDateRangeChange={handleLastModifiedDateRangeChange}
          onQuickModeChange={handleQuickDateRangeModeChange}
          quickOptionDropdownLabel={quickOptionDropdown}
          dateRange={searchPageOptions.lastModifiedDateRange}
          quickModeEnabled={searchPageOptions.dateRangeQuickModeEnabled}
        />
      ),
      // Before Search settings are retrieved, do not show.
      disabled: searchSettings?.searchingDisableDateModifiedFilter ?? true,
    },
    {
      idSuffix: "OwnerSelector",
      title: searchStrings.filterOwner.title,
      component: (
        <OwnerSelector
          onClearSelect={handleOwnerClear}
          onSelect={handleOwnerChange}
          value={searchPageOptions.owner}
        />
      ),
      disabled: searchSettings?.searchingDisableOwnerFilter ?? true,
    },
    {
      idSuffix: "StatusSelector",
      title: searchStrings.statusSelector.title,
      component: (
        <StatusSelector
          onChange={handleStatusChange}
          value={searchPageOptions.status}
        />
      ),
      disabled: !searchSettings?.searchingShowNonLiveCheckbox ?? true,
    },
    {
      idSuffix: "SearchAttachmentsSelector",
      title: searchStrings.searchAttachmentsSelector.title,
      component: (
        <SearchAttachmentsSelector
          value={searchPageOptions.searchAttachments}
          onChange={handleSearchAttachmentsChange}
        />
      ),
      disabled: false,
    },
  ];

  const {
    available: totalCount,
    highlight: highlights,
    results: searchResults,
  } = pagedSearchResult;
  return (
    <>
      <Grid container spacing={2}>
        <Grid item xs={9}>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <SearchBar
                query={searchPageOptions.query ?? ""}
                rawMode={searchPageOptions.rawMode}
                onQueryChange={handleQueryChanged}
                onRawModeChange={handleRawModeChanged}
                doSearch={search}
              />
            </Grid>
            <Grid item xs={12}>
              <SearchResultList
                showSpinner={showSpinner}
                paginationProps={{
                  count: totalCount,
                  currentPage: searchPageOptions.currentPage,
                  rowsPerPage: searchPageOptions.rowsPerPage,
                  onPageChange: handlePageChanged,
                  onRowsPerPageChange: handleRowsPerPageChanged,
                }}
                orderSelectProps={{
                  value: searchPageOptions.sortOrder,
                  onChange: handleSortOrderChanged,
                }}
                onClearSearchOptions={handleClearSearchOptions}
                onCopySearchLink={handleCopySearch}
              >
                {searchResults.length > 0 &&
                  mapSearchResultItems(searchResults, handleError, highlights)}
              </SearchResultList>
            </Grid>
          </Grid>
        </Grid>

        <Grid item xs={3}>
          <Grid container direction="column" spacing={2}>
            <Grid item>
              <RefineSearchPanel
                controls={refinePanelControls}
                onChangeExpansion={handleCollapsibleFilterClick}
                panelExpanded={filterExpansion}
                showFilterIcon={areCollapsibleFiltersSet()}
              />
            </Grid>
            {classifications.length > 0 &&
              classifications.some((c) => c.categories.length > 0) && (
                <Grid item>
                  <Card>
                    <CardContent>
                      <Typography variant="h5">
                        {languageStrings.searchpage.categorySelector.title}
                      </Typography>
                      <CategorySelector
                        classifications={classifications}
                        onSelectedCategoriesChange={
                          handleSelectedCategoriesChange
                        }
                        selectedCategories={
                          searchPageOptions.selectedCategories
                        }
                      />
                    </CardContent>
                  </Card>
                </Grid>
              )}
          </Grid>
        </Grid>
      </Grid>
      {copySearchInfoSnackbar}
    </>
  );
};

export default SearchPage;
