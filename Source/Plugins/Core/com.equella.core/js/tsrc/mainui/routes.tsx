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
import { LocationDescriptor } from "history";
import * as React from "react";
import { RouteComponentProps } from "react-router";
import { TemplateUpdate, TemplateUpdateProps } from "./Template";

const ThemePage = React.lazy(() => import("../theme/ThemePage"));
const CloudProviderListPage = React.lazy(
  () => import("../cloudprovider/CloudProviderListPage")
);
const SearchPageSettings = React.lazy(
  () => import("../settings/Search/SearchPageSettings")
);
const SettingsPage = React.lazy(() => import("../settings/SettingsPage"));
const SearchFilterPage = React.lazy(
  () => import("../settings/Search/searchfilter/SearchFilterSettingsPage")
);
const LoginNoticeConfigPage = React.lazy(
  () => import("../loginnotice/LoginNoticeConfigPage")
);
const FacetedSearchSettingsPage = React.lazy(
  () => import("../settings/Search/facetedsearch/FacetedSearchSettingsPage")
);
const ContentIndexSettings = React.lazy(
  () => import("../settings/Search/ContentIndexSettings")
);

export interface OEQRouteComponentProps<T = TemplateUpdateProps>
  extends RouteComponentProps<T> {
  updateTemplate(edit: TemplateUpdate): void;
  redirect(to: LocationDescriptor): void;
  setPreventNavigation(b: boolean): void;
  refreshUser(): void;
  isReloadNeeded: boolean;
}

type To = (uuid: string) => string;
type ToVersion = (uuid: string, version: number) => string;

export interface OEQRoute<T> {
  component?:
    | React.ComponentType<OEQRouteComponentProps<T>>
    | React.ComponentType<T>;
  render?: (props: OEQRouteComponentProps<T>) => React.ReactNode;
  path?: string;
  exact?: boolean;
  sensitive?: boolean;
  strict?: boolean;
  to?: string | To | ToVersion;
}

export const routes = {
  Settings: {
    path: "(/access/settings.do|/page/settings)",
    to: "/page/settings",
    component: SettingsPage,
  },
  AdvancedSearch: {
    to: (uuid: string) => `/advanced/searching.do?in=P${uuid}&editquery=true`,
  },
  RemoteSearch: {
    // `uc` parameter comes from sections code (AbstractRootSearchSection.Model.java). Setting it to
    // true clears out the Session State for Remote Repository pages. This replicates the behaviour
    // for links inside the 'Within' dropdown in the legacy UI.
    // See com.tle.web.searching.section.SearchQuerySection.forwardToRemote
    to: (uuid: string) => `/access/z3950.do?.repository=${uuid}&uc=true`,
  },
  SearchSettings: {
    path: "/page/searchsettings",
    render: (p: OEQRouteComponentProps) => <SearchPageSettings {...p} />,
  },
  SearchFilterSettings: {
    path: "/page/searchfiltersettings",
    render: (p: OEQRouteComponentProps) => <SearchFilterPage {...p} />,
  },
  ContentIndexSettings: {
    path: "/page/contentindexsettings",
    render: (p: OEQRouteComponentProps) => <ContentIndexSettings {...p} />,
  },
  FacetedSearchSetting: {
    path: "/page/facetedsearchsettings",
    render: (p: OEQRouteComponentProps) => <FacetedSearchSettingsPage {...p} />,
  },
  ViewItem: {
    to: (uuid: string, version: number) => `/items/${uuid}/${version}/`,
  },
  ThemeConfig: { path: "/page/themeconfiguration", component: ThemePage },
  LoginNoticeConfig: {
    path: "/page/loginconfiguration",
    component: LoginNoticeConfigPage,
  },
  CloudProviders: {
    path: "/page/cloudprovider",
    component: CloudProviderListPage,
  },
  Notifications: {
    to: "/access/notifications.do",
  },
  TaskList: {
    to: "/access/tasklist.do",
  },
  Logout: {
    // lack of '/' is significant
    to: "logon.do?logout=true",
  },
  UserPreferences: {
    to: "/access/user.do",
  },
};
