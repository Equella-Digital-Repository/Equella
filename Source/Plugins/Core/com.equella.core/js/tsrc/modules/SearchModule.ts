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

import * as OEQ from "@openequella/rest-api-client";
import { Location } from "history";
import { pick } from "lodash";
import {
  Array as RuntypeArray,
  Boolean,
  Guard,
  Literal,
  match,
  Number,
  Record,
  Static,
  String,
  Union,
  Unknown,
  Partial,
} from "runtypes";
import { API_BASE_URL } from "../AppConfig";
import { getISODateString } from "../util/Date";
import { Collection, collectionListSummary } from "./CollectionsModule";
import { SelectedCategories } from "./SearchFacetsModule";
import { SortOrder } from "./SearchSettingsModule";
import { resolveUsers } from "./UserModule";

/**
 * Type of all search options on Search page
 */
export interface SearchOptions {
  /**
   * The query string of the current search. Can be left blank for a default search.
   */
  query?: string;
  /**
   * The number of items displayed in one page.
   */
  rowsPerPage: number;
  /**
   * Selected page.
   */
  currentPage: number;
  /**
   * Selected search result sorting order.
   */
  sortOrder: Static<typeof SortOrder> | undefined;
  /**
   * A list of collections.
   */
  collections?: Collection[];
  /**
   * Whether to send the `query` as is (true) or to apply some processing (such as appending
   * a wildcard operator).
   */
  rawMode: boolean;
  /**
   * A date range for searching items by last modified date.
   */
  lastModifiedDateRange?: DateRange;
  /**
   * A user for which to filter the search by based on ownership of items.
   */
  owner?: OEQ.UserQuery.UserDetails;
  /**
   * Filter search results to only include items with the specified statuses.
   */
  status?: OEQ.Common.ItemStatus[];
  /**
   * A list of categories selected in the Category Selector and grouped by Classification ID.
   */
  selectedCategories?: SelectedCategories[];
  /**
   * Whether to search attachments or not.
   */
  searchAttachments?: boolean;
}

/**
 * Represent a date range which has an optional start and end.
 */
export interface DateRange {
  /**
   * The start date of a date range.
   */
  start?: Date;
  /**
   * The end date of a date range.
   */
  end?: Date;
}

/**
 * Legacy searching.do parameters currently supported by SearchPage component.
 */
const LegacySearchParams = Union(
  Literal("dp"),
  Literal("ds"),
  Literal("dr"),
  Literal("q"),
  Literal("sort"),
  Literal("owner"),
  Literal("in")
);

type LegacyParams = Static<typeof LegacySearchParams>;

const isDate = (value: unknown): value is Date => value instanceof Date;

/**
 * Represents the shape of data returned from generateQueryStringFromSearchOptions
 */
const DehydratedSearchOptionsRunTypes = Partial({
  query: String,
  rowsPerPage: Number,
  currentPage: Number,
  sortOrder: SortOrder,
  collections: RuntypeArray(Record({ uuid: String })),
  rawMode: Boolean,
  lastModifiedDateRange: Record({ start: Guard(isDate), end: Guard(isDate) }),
  owner: Record({ id: String }),
  // Runtypes guard function would not work when defining the type as Array(OEQ.Common.ItemStatuses) or Guard(OEQ.Common.ItemStatuses.guard),
  // So the Union of Literals has been copied from the OEQ.Common module.
  status: RuntypeArray(
    Union(
      Literal("ARCHIVED"),
      Literal("DELETED"),
      Literal("DRAFT"),
      Literal("LIVE"),
      Literal("MODERATING"),
      Literal("PERSONAL"),
      Literal("REJECTED"),
      Literal("REVIEW"),
      Literal("SUSPENDED")
    )
  ),
  selectedCategories: RuntypeArray(
    Record({
      id: Number,
      categories: RuntypeArray(String),
    })
  ),
  searchAttachments: Boolean,
});

type DehydratedSearchOptions = Static<typeof DehydratedSearchOptionsRunTypes>;

/**
 * List of status which are considered 'live'.
 */
export const liveStatuses: OEQ.Common.ItemStatus[] = ["LIVE", "REVIEW"];

/**
 * Predicate for checking if a provided status is not one of `liveStatuses`.
 * @param status a status to check for liveliness
 */
export const nonLiveStatus = (status: OEQ.Common.ItemStatus): boolean =>
  !liveStatuses.find((liveStatus) => status === liveStatus);

/**
 * List of statuses which are considered non-live.
 */
export const nonLiveStatuses: OEQ.Common.ItemStatus[] = OEQ.Common.ItemStatuses.alternatives
  .map((status) => status.value)
  .filter(nonLiveStatus);

export const defaultSearchOptions: SearchOptions = {
  rowsPerPage: 10,
  currentPage: 0,
  sortOrder: undefined,
  rawMode: false,
  status: liveStatuses,
  searchAttachments: true,
};

export const defaultPagedSearchResult: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> = {
  start: 0,
  length: 10,
  available: 0,
  results: [],
  highlight: [],
};

/**
 * Helper function, to support formatting of query in raw mode. When _not_ raw mode
 * we append a wildcard to support the idea of a simple (typeahead) search.
 *
 * @param query the intended search query to be sent to the API
 * @param addWildcard whether a wildcard should be appended
 */
export const formatQuery = (query: string, addWildcard: boolean): string => {
  const trimmedQuery = query ? query.trim() : "";
  const appendWildcard = addWildcard && trimmedQuery.length > 0;
  return trimmedQuery + (appendWildcard ? "*" : "");
};

/**
 * Generates a Where clause through Classifications.
 * Each Classification that has categories selected is joined by AND.
 * Each selected category of one Classification is joined by OR.
 *
 * @param selectedCategories A list of selected Categories grouped by Classification ID.
 */
export const generateCategoryWhereQuery = (
  selectedCategories?: SelectedCategories[]
): string | undefined => {
  if (!selectedCategories || selectedCategories.length === 0) {
    return undefined;
  }

  const and = " AND ";
  const or = " OR ";
  const processNodeTerms = (
    categories: string[],
    schemaNode?: string
  ): string => categories.map((c) => `/xml${schemaNode}='${c}'`).join(or);

  return selectedCategories
    .filter((c) => c.categories.length > 0)
    .map(
      ({ schemaNode, categories }: SelectedCategories) =>
        `(${processNodeTerms(categories, schemaNode)})`
    )
    .join(and);
};

/**
 * A function that takes and parses a saved search query string from a shared legacy searching.do or /page/search URL, and converts it into a SearchOptions object
 * @param location representing a Location which includes search query params - such as from the <SearchPage>
 * @return SearchOptions containing the options encoded in the query string params, or undefined if there were none.
 */
export const queryStringParamsToSearchOptions = async (
  location: Location
): Promise<SearchOptions | undefined> => {
  if (!location.search) return undefined;
  const params = new URLSearchParams(location.search);

  // If no query strings is of type LegacySearchParams, return undefined.
  if (!Array.from(params.keys()).some((key) => LegacySearchParams.guard(key))) {
    return undefined;
  }

  if (location.pathname.endsWith("searching.do")) {
    return await legacyQueryStringToSearchOptions(params);
  }
  return await newSearchQueryToSearchOptions(params.get("searchOptions") ?? "");
};

/**
 * A function that takes search options and converts it to to a JSON representation.
 * Collections and owner properties are both reduced down to their uuid and id properties respectively.
 * Undefined properties are excluded.
 * Intended to be used in conjunction with SearchModule.newSearchQueryToSearchOptions
 * @param searchOptions Search options selected on Search page.
 * @return url encoded key/value pair of JSON searchOptions
 */
export const generateQueryStringFromSearchOptions = (
  searchOptions: SearchOptions
): string => {
  const params = new URLSearchParams();
  params.set(
    "searchOptions",
    JSON.stringify(
      searchOptions,
      (key: string, value: object[] | undefined) => {
        return match(
          [
            Literal("collections"),
            () => value?.map((collection) => pick(collection, ["uuid"])),
          ],
          [Literal("owner"), () => (value ? pick(value, ["id"]) : undefined)],
          [Unknown, () => value ?? undefined]
        )(key);
      }
    )
  );
  return params.toString();
};

const rehydrateCollections = async (
  options: DehydratedSearchOptions
): Promise<Collection[] | undefined> =>
  options.collections
    ? await findCollectionsByUuid(options.collections.map((c) => c.uuid))
    : undefined;

const rehydrateOwner = async (
  options: DehydratedSearchOptions
): Promise<OEQ.UserQuery.UserDetails | undefined> =>
  options.owner ? await findUser(options.owner.id) : undefined;

/**
 * A function that takes a JSON representation of a SearchOptions object, and converts it into an actual SearchOptions object.
 * Note: currently, due to lack of support from runtypes extraneous properties will end up in the resultant object (which then could be stored in state).
 * @see {@link https://github.com/pelotom/runtypes/issues/169 }
 * @param searchOptionsJSON a JSON representation of a SearchOptions object.
 * @return searchOptions A deserialized representation of that provided by `searchOptionsJSON`
 */
export const newSearchQueryToSearchOptions = async (
  searchOptionsJSON: string
): Promise<SearchOptions> => {
  const parsedOptions: unknown = JSON.parse(searchOptionsJSON, (key, value) => {
    if (key === "lastModifiedDateRange") {
      let { start, end } = value;
      start = start ? new Date(start) : undefined;
      end = end ? new Date(end) : undefined;
      return { start, end };
    }
    return value;
  });

  if (!DehydratedSearchOptionsRunTypes.guard(parsedOptions)) {
    console.warn("Invalid search query params received. Using defaults.");
    return defaultSearchOptions;
  }
  const result: SearchOptions = {
    ...defaultSearchOptions,
    ...parsedOptions,
    collections: await rehydrateCollections(parsedOptions),
    owner: await rehydrateOwner(parsedOptions),
  };
  return result;
};

/**
 * A function that takes search options and converts search options to search params,
 * and then does a search and returns a list of Items.
 * @param searchOptions Search options selected on Search page.
 */
export const searchItems = ({
  query,
  rowsPerPage,
  currentPage,
  sortOrder,
  collections,
  rawMode,
  lastModifiedDateRange,
  owner,
  status = liveStatuses,
  searchAttachments,
  selectedCategories,
}: SearchOptions): Promise<
  OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>
> => {
  const processedQuery = query ? formatQuery(query, !rawMode) : undefined;
  const searchParams: OEQ.Search.SearchParams = {
    query: processedQuery,
    start: currentPage * rowsPerPage,
    length: rowsPerPage,
    status: status,
    order: sortOrder,
    collections: collections?.map((collection) => collection.uuid),
    modifiedAfter: getISODateString(lastModifiedDateRange?.start),
    modifiedBefore: getISODateString(lastModifiedDateRange?.end),
    owner: owner?.id,
    searchAttachments: searchAttachments,
    whereClause: generateCategoryWhereQuery(selectedCategories),
  };
  return OEQ.Search.search(API_BASE_URL, searchParams);
};

const findCollectionsByUuid = async (
  collectionUuids: string[]
): Promise<Collection[] | undefined> => {
  const collectionList = await collectionListSummary([
    OEQ.Acl.ACL_SEARCH_COLLECTION,
  ]);
  const filteredCollectionList = collectionList.filter((c) =>
    collectionUuids.includes(c.uuid)
  );
  return filteredCollectionList.length > 0 ? filteredCollectionList : undefined;
};

const findUser = async (
  userId: string
): Promise<OEQ.UserQuery.UserDetails | undefined> => {
  const userDetails = await resolveUsers([userId]);
  if (userDetails.length > 1)
    throw new Error(`More than one user was resolved for id: ${userId}`);
  return userDetails[0];
};

/**
 * A function that takes query string params from a shared searching.do URL and converts all applicable params to Search options
 * @param params URLSearchParams from a shared `searching.do` URL
 * @return SearchOptions object.
 */
export const legacyQueryStringToSearchOptions = async (
  params: URLSearchParams
): Promise<SearchOptions> => {
  const getQueryParam = (paramName: LegacyParams) => {
    return params.get(paramName) ?? undefined;
  };

  const collectionId = getQueryParam("in")?.substring(1);
  const ownerId = getQueryParam("owner");
  const dateRange = getQueryParam("dr");
  const datePrimary = getQueryParam("dp");
  const dateSecondary = getQueryParam("ds");

  const RangeTypeLiterals = Union(
    Literal("between"),
    Literal("after"),
    Literal("before"),
    Literal("on")
  );

  type RangeType = Static<typeof RangeTypeLiterals>;

  const getLastModifiedDateRange = (
    rangeType: RangeType,
    primaryDate?: Date,
    secondaryDate?: Date
  ): DateRange | undefined => {
    if (!primaryDate && !secondaryDate) {
      return undefined;
    }
    return match(
      [
        Literal("between"),
        (): DateRange => ({ start: primaryDate, end: secondaryDate }),
      ],
      [Literal("after"), (): DateRange => ({ start: primaryDate })],
      [Literal("before"), (): DateRange => ({ end: primaryDate })],
      [
        Literal("on"),
        (): DateRange => ({
          start: primaryDate,
          end: primaryDate,
        }),
      ]
    )(rangeType.toLowerCase() as RangeType);
  };

  const parseCollectionUuid = async (
    collectionUuid: string | undefined
  ): Promise<Collection[] | undefined> => {
    if (!collectionUuid) return defaultSearchOptions.collections;
    const collectionDetails:
      | Collection[]
      | undefined = await findCollectionsByUuid([collectionUuid]);

    return typeof collectionDetails !== "undefined" &&
      collectionDetails.length > 0
      ? collectionDetails
      : defaultSearchOptions.collections;
  };

  const sortOrderParam = getQueryParam("sort")?.toUpperCase();
  const searchOptions: SearchOptions = {
    ...defaultSearchOptions,
    collections: await parseCollectionUuid(collectionId),
    query: getQueryParam("q") ?? defaultSearchOptions.query,
    owner: ownerId ? await findUser(ownerId) : defaultSearchOptions.owner,
    lastModifiedDateRange:
      getLastModifiedDateRange(
        dateRange as RangeType,
        datePrimary ? new Date(parseInt(datePrimary)) : undefined,
        dateSecondary ? new Date(parseInt(dateSecondary)) : undefined
      ) ?? defaultSearchOptions.lastModifiedDateRange,
    sortOrder: SortOrder.guard(sortOrderParam)
      ? sortOrderParam
      : defaultSearchOptions.sortOrder,
  };
  return searchOptions;
};
