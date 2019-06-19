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

package com.tle.core.reporting.web;

import com.tle.beans.entity.report.Report;
import com.tle.core.guice.Bind;
import com.tle.core.reporting.ReportingService;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.section.AbstractUpdatableMenuContributor;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ReportingMenuContributor extends AbstractUpdatableMenuContributor {
  private static final Label LABEL_KEY =
      new KeyLabel(
          ResourcesService.getResourceHelper(ReportingMenuContributor.class).key("menu.reporting"));
  private static final String ICON_PATH =
      ResourcesService.getResourceHelper(ReportingMenuContributor.class)
          .url("images/reports-menu-icon.png");
  private static final String SESSION_KEY = "REPORTING-MENU";

  @Inject private ReportingService reportingService;

  @Override
  public String getSessionKey() {
    return SESSION_KEY;
  }

  @Override
  public List<MenuContribution> getMenuContributions(SectionInfo info) {
    Boolean show = userSessionService.getAttribute(SESSION_KEY);

    if (show == null) {
      show = false;
      List<Report> reports = reportingService.enumerateExecutable();
      for (Report report : reports) {
        if (!report.isHideReport()) {
          show = true;
          break;
        }
      }
      userSessionService.setAttribute(SESSION_KEY, show);
    }

    if (show) {
      // TODO: We should be generating a bookmark to the section rather
      // than hard-coding the URL

      HtmlLinkState hls = new HtmlLinkState(new SimpleBookmark("access/reports.do"));
      hls.setLabel(LABEL_KEY);
      MenuContribution mc = new MenuContribution(hls, ICON_PATH, 30, 20, "insert_chart");
      return Collections.singletonList(mc);
    } else {
      return Collections.emptyList();
    }
  }
}
