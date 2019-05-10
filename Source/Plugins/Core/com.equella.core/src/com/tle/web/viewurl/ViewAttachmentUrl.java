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

package com.tle.web.viewurl;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

public class ViewAttachmentUrl implements ItemUrlExtender {
  private static final long serialVersionUID = 1L;

  private final String uuid;
  private final boolean stream;

  public ViewAttachmentUrl(String uuid) {
    this.uuid = uuid;
    this.stream = false;
  }

  public ViewAttachmentUrl(String uuid, boolean stream) {
    this.uuid = uuid;
    this.stream = stream;
  }

  @Override
  public void execute(SectionInfo info) {
    ViewAttachmentInterface section = info.lookupSection(ViewAttachmentInterface.class);
    section.setAttachmentToView(info, uuid, stream);
  }

  @TreeIndexed
  public interface ViewAttachmentInterface extends SectionId {
    void setAttachmentToView(SectionInfo info, String attachmentUuid, boolean stream);
  }
}
