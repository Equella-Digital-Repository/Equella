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

package com.tle.web.wizard;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.filesystem.handle.FileHandle;
import java.io.Serializable;

public interface WizardStateInterface extends Serializable, Cloneable {
  String getWizid();

  ModifiableAttachments getAttachments();

  void setItemPack(ItemPack<Item> pack);

  ItemPack<Item> getItemPack();

  int getStateVersion();

  FileHandle getFileHandle();

  Item getItem();

  ItemKey getItemId();

  PropBagEx getItemxml();

  WorkflowStatus getWorkflowStatus();

  String getStagingId();

  void onSessionSave();
}
