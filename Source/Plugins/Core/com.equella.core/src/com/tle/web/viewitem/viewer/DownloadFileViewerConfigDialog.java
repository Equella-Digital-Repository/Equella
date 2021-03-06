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

package com.tle.web.viewitem.viewer;

import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.viewers.AbstractResourceViewerConfigDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogControl;

public class DownloadFileViewerConfigDialog extends AbstractResourceViewerConfigDialog {
  @PlugKey("appendtoken")
  private static Label APPEND_TOKEN_LABEL;

  @PlugKey("downloadfileviewer")
  private static Label TITLE_LABEL;

  @Component private Checkbox appendToken;

  @Override
  @SuppressWarnings("nls")
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    controls.add(new DialogControl(APPEND_TOKEN_LABEL, appendToken));
    mappings.addMapMapping("attr", "appendToken", appendToken);
  }

  @Override
  protected Label getTitleLabel(RenderContext context) {
    return TITLE_LABEL;
  }
}
