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

package com.tle.core.userscripts.service;

import com.tle.common.userscripts.UserScriptsConstants.ScriptTypes;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.userscripts.service.session.UserScriptEditingBean;
import java.util.List;

public interface UserScriptsService
    extends AbstractEntityService<UserScriptEditingBean, UserScript> {
  @SuppressWarnings("nls")
  public static final String ENTITY_TYPE = "USER_SCRIPTS";

  List<UserScript> enumerateForType(ScriptTypes type);

  boolean executableScriptsAvailable();

  boolean displayScriptsAvailable();

  boolean isModuleNameExist(String moduleName, long id);
}
