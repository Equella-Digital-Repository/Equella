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

package com.tle.web.selection.home.model;

import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.selection.SelectionHistory;
import java.util.List;

public class RecentSelectionSegmentModel {
  private final String title;
  private final List<RecentSelection> selections;

  public RecentSelectionSegmentModel(String title, List<RecentSelection> selections) {
    this.title = title;
    this.selections = selections;
  }

  public String getTitle() {
    return title;
  }

  public List<RecentSelection> getSelections() {
    return selections;
  }

  public static class RecentSelection {
    private String title;
    private SelectionHistory resource;
    private final HtmlComponentState link;

    public RecentSelection(SelectionHistory resource, HtmlComponentState link) {
      this.resource = resource;
      this.link = link;
    }

    public RecentSelection(String title, HtmlComponentState link) {
      this.title = title;
      this.link = link;
    }

    public String getTitle() {
      if (resource != null) {
        return resource.getTitle();
      }
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public HtmlComponentState getLink() {
      return link;
    }
  }
}
