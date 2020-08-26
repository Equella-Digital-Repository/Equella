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
import { isEqual, memoize } from "lodash";
import { API_BASE_URL } from "../config";
import { SearchPageClassification } from "../search/components/FacetSelector";
import { getISODateString } from "../util/Date";
import { getFacetsFromServer } from "./FacetedSearchSettingsModule";
import { SearchOptions } from "./SearchModule";

/**
 * Represents a Classification and its generated categories ready for display.
 */
export interface Classification {
  /**
   * The unique ID of a Classification.
   */
  id: number;
  /**
   * The name for this group of categories - typically one which has been configured on the system.
   */
  name: string;
  /**
   * The maximum number of categories which should be displayed - based on what was configured for
   * this classification. If `undefined` then the system default number of categories should be
   * displayed.
   */
  maxDisplay?: number;
  /**
   * The actual list of categories for this classification. This will be the full list returned
   * from the server - as no paging is currently provided.
   */
  categories: OEQ.SearchFacets.Facet[];
  /**
   * The configured order in which this classification should be displayed.
   */
  orderIndex: number;
}

/**
 * Helper function to convert the commonly used `SearchOptions` into the params we need to
 * list facets. This is a memoized function, so that it can be used in an `Array.map()`
 * with reasonable performance. (Important seeing it's also doing some data conversion.)
 */
const convertSearchOptions: (
  options: SearchOptions
) => OEQ.SearchFacets.SearchFacetsParams = memoize(
  (options: SearchOptions): OEQ.SearchFacets.SearchFacetsParams => ({
    nodes: [],
    q: options.query,
    collections: options.collections?.map((c) => c.uuid),
    modifiedAfter: getISODateString(options.lastModifiedDateRange?.start),
    modifiedBefore: getISODateString(options.lastModifiedDateRange?.end),
    owner: options.owner?.id,
    showall: isEqual(
      options.status?.sort(),
      OEQ.Common.ItemStatuses.alternatives.map((i) => i.value).sort()
    ),
  })
);

/**
 * Provides a list of categories as defined and filtered by the `options`.
 *
 * @param options The control parameters for the generation of the categories
 */
export const listCategories = async (
  options: OEQ.SearchFacets.SearchFacetsParams
): Promise<OEQ.SearchFacets.Facet[]> =>
  (await OEQ.SearchFacets.searchFacets(API_BASE_URL, options)).results;

/**
 * Uses the system's configured facets/classifications to generate a set of categories for
 * each. Thereby, generating All the classifications and categories for the system based on
 * configured facets.
 *
 * It is intended that this can be run alongside other search filters, and thereby provide
 * matching categories.
 *
 * @param options The standard options used for searching, as these also filter the generated categories
 */
export const listClassifications = async (
  options: SearchOptions
): Promise<Classification[]> =>
  Promise.all(
    (await getFacetsFromServer()).map<Promise<Classification>>(
      async (settings, index) => ({
        // We know IDs won't be undefined here, but due to its type being number | undefined,
        // we have to do a nullish coalescing
        id: settings.id ?? index,
        name: settings.name,
        maxDisplay: settings.maxResults,
        orderIndex: settings.orderIndex,
        categories: await listCategories({
          ...convertSearchOptions(options),
          nodes: [settings.schemaNode],
        }),
      })
    )
  );

/**
 * Convert a list of standard Classifications to a list of SearchPage Classifications.
 * @param classifications The standard Classifications to be processed.
 */
export const classificationTransformer = (
  classifications: Classification[]
): SearchPageClassification[] =>
  classifications.map((c) => {
    // If 'maxDisplay' is undefined, it will be 10 by default.
    const maxDisplay = c.maxDisplay ?? 10;
    const showMore = c.categories.length > maxDisplay;
    return { ...c, showMore: showMore, maxDisplay: maxDisplay };
  });