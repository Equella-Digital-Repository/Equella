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

package com.tle.admin.gui.common.actions;

import com.tle.common.i18n.CurrentLocale;
import javax.swing.Action;

/** @author Nicholas Read */
public abstract class AddAttributeAction extends TLEAction {
  @SuppressWarnings("nls")
  public AddAttributeAction() {
    setIcon("/icons/add.gif");
    putValue(
        Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.addattributeaction.name"));
    putValue(
        Action.SHORT_DESCRIPTION,
        CurrentLocale.get("com.tle.admin.gui.common.actions.addattributeaction.desc"));
  }
}
