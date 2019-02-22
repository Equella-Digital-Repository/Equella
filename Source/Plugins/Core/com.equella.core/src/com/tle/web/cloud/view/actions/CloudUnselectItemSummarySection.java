/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.cloud.view.actions;

import com.tle.core.cloud.CloudConstants;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.web.cloud.view.section.CloudItemSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.summary.sidebar.actions.AbstractUnselectItemSummarySection;

/** @author Aaron */
public class CloudUnselectItemSummarySection
    extends AbstractUnselectItemSummarySection<CloudItem, Object> {
  @Override
  protected ViewableItem<CloudItem> getViewableItem(SectionInfo info) {
    return CloudItemSectionInfo.getItemInfo(info).getViewableItem();
  }

  @Override
  protected String getItemExtensionType() {
    return CloudConstants.ITEM_EXTENSION;
  }
}
