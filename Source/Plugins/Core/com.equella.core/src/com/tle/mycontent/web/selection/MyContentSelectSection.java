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

package com.tle.mycontent.web.selection;

import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class MyContentSelectSection
    extends ContextableSearchSection<ContextableSearchSection.Model> {
  @PlugKey("select.title")
  private static Label LABEL_TITLE;

  @SuppressWarnings("nls")
  @Override
  protected String getSessionKey() {
    return "myContentSelect";
  }

  @Override
  public Label getTitle(SectionInfo info) {
    return LABEL_TITLE;
  }

  @Override
  protected String getPageName() {
    return null;
  }
}
