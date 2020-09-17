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
import { ThemeProvider } from "@material-ui/core";
import { oeqTheme } from "../theme";
import * as React from "react";
import { getCurrentUser, UserData } from "../api/currentuser";
import { ErrorResponse } from "../api/errors";
import {
  LegacyContent,
  LegacyContentProps,
  PageContent,
} from "../legacycontent/LegacyContent";
import { Template, TemplateProps, TemplateUpdate } from "./Template";
import { defaultNavMessage, NavAwayDialog } from "./PreventNavigation";
import { shallowEqual } from "shallow-equal-object";
import { OEQRoute, OEQRouteComponentProps, routes } from "./routes";
import {
  Prompt,
  Redirect,
  Route,
  RouteComponentProps,
  Switch,
} from "react-router";
import ErrorPage from "./ErrorPage";
import { LegacyPage, templatePropsForLegacy } from "./LegacyPage";
import { BrowserRouter } from "react-router-dom";
import { LegacyForm } from "../legacycontent/LegacyForm";
import HtmlParser from "react-html-parser";
import SettingsPage from "../settings/SettingsPage";
import { startHeartbeat } from "../util/heartbeat";

const beforeunload = function (e: BeforeUnloadEvent) {
  e.returnValue = "Are you sure?";
  return "Are you sure?";
};

const baseFullPath = new URL(document.head.getElementsByTagName("base")[0].href)
  .pathname;
export const basePath = baseFullPath.substr(0, baseFullPath.length - 1);

function IndexPage() {
  const [currentUser, setCurrentUser] = React.useState<UserData>();
  const [fullPageError, setFullPageError] = React.useState<ErrorResponse>();
  const errorShowing = React.useRef(false);

  const refreshUser = React.useCallback(() => {
    getCurrentUser().then(setCurrentUser);
  }, []);

  React.useEffect(() => refreshUser(), []);

  const [navAwayCallback, setNavAwayCallback] = React.useState<{
    message: string;
    cb: (confirm: boolean) => void;
  }>();

  const [preventNavMessage, setPreventNavMessage] = React.useState<string>();
  const [legacyContentProps, setLegacyContentProps] = React.useState<
    LegacyContentProps
  >({
    enabled: false,
    pathname: "",
    search: "",
    locationKey: "",
    userUpdated: refreshUser,
    redirected: () => {},
    onError: () => {},
    render: () => <div />,
  });

  const [templateProps, setTemplateProps] = React.useState<TemplateProps>({
    title: "",
    fullscreenMode: "YES",
    children: [],
  });

  const setPreventNavigation = React.useCallback(
    (prevent) => {
      const message = prevent ? defaultNavMessage() : undefined;
      if (message) {
        window.addEventListener("beforeunload", beforeunload, false);
      } else {
        window.removeEventListener("beforeunload", beforeunload, false);
      }
      setPreventNavMessage(message);
    },
    [setPreventNavMessage]
  );

  const nonBlankNavMessage = preventNavMessage ? preventNavMessage : "";

  const updateTemplate = React.useCallback((edit: TemplateUpdate) => {
    setTemplateProps((tp) => {
      const edited = edit(tp);
      return shallowEqual(edited, tp) ? tp : edited;
    });
  }, []);
  const oeqRoutes: { [key: string]: OEQRoute } = routes;

  function mkRouteProps(p: RouteComponentProps<any>): OEQRouteComponentProps {
    return {
      ...p,
      updateTemplate,
      refreshUser,
      redirect: p.history.push,
      setPreventNavigation,
    };
  }

  const newUIRoutes = React.useMemo(() => {
    return Object.keys(oeqRoutes).map((key, ind) => {
      const oeqRoute = oeqRoutes[key];
      return (
        (oeqRoute.component || oeqRoute.render) && (
          <Route
            key={ind}
            exact={oeqRoute.exact}
            path={oeqRoute.path}
            render={(p) => {
              const oeqProps = mkRouteProps(p);
              if (oeqRoute.component) {
                return <oeqRoute.component {...oeqProps} />;
              }
              return oeqRoute.render?.(oeqProps);
            }}
          />
        )
      );
    });
  }, [refreshUser]);

  const errorCallback = React.useCallback((err) => {
    errorShowing.current = true;
    setTemplateProps((p) => ({ ...p, fullscreenMode: undefined }));
    setFullPageError(err);
  }, []);

  function routeSwitch(content?: PageContent) {
    return (
      <Switch>
        {fullPageError && (
          <Route>
            <ErrorPage error={fullPageError} />
          </Route>
        )}
        <Route path="/" exact>
          <Redirect to="/home.do" />
        </Route>
        {newUIRoutes}
        <Route
          render={(p) => (
            <LegacyPage
              {...mkRouteProps(p)}
              errorCallback={errorCallback}
              legacyContent={{ content, setLegacyContentProps }}
            />
          )}
        />
      </Switch>
    );
  }

  return (
    <BrowserRouter
      basename={basePath}
      getUserConfirmation={(message, cb) => {
        if (errorShowing.current) {
          errorShowing.current = false;
          setFullPageError(undefined);
          cb(true);
        } else {
          setNavAwayCallback({ message, cb });
        }
      }}
    >
      <Prompt
        when={Boolean(preventNavMessage) || errorShowing.current}
        message={nonBlankNavMessage}
      />
      <NavAwayDialog
        open={Boolean(navAwayCallback)}
        message={nonBlankNavMessage}
        navigateConfirm={(confirm) => {
          if (navAwayCallback) navAwayCallback.cb(confirm);
          if (confirm) setPreventNavMessage(undefined);
          setNavAwayCallback(undefined);
        }}
      />
      <LegacyContent
        {...legacyContentProps}
        render={(content) => {
          const tp = content
            ? templatePropsForLegacy(content)
            : {
                ...templateProps,
                fullscreenMode: legacyContentProps.enabled
                  ? templateProps.fullscreenMode
                  : undefined,
              };
          const withErr = fullPageError
            ? { ...tp, title: fullPageError.error, fullscreenMode: undefined }
            : tp;
          const template = (
            <Template {...withErr} currentUser={currentUser}>
              {routeSwitch(content)}
            </Template>
          );
          const render = () => {
            if (!content || content.noForm) {
              return template;
            } else {
              const { form } = content.html;
              return (
                <>
                  <LegacyForm state={content.state}>{template}</LegacyForm>
                  {form && HtmlParser(form)}
                </>
              );
            }
          };
          return render();
        }}
      />
    </BrowserRouter>
  );
}

interface AppProps {
  legacySettingsMode: boolean;
}

export const App = ({ legacySettingsMode }: AppProps) => {
  if (legacySettingsMode) {
    return (
      <BrowserRouter basename={basePath} forceRefresh>
        <ThemeProvider theme={oeqTheme}>
          <SettingsPage refreshUser={() => {}} updateTemplate={() => {}} />
        </ThemeProvider>
      </BrowserRouter>
    );
  } else {
    startHeartbeat();
    return (
      <ThemeProvider theme={oeqTheme}>
        <IndexPage />
      </ThemeProvider>
    );
  }
};

export default App;
