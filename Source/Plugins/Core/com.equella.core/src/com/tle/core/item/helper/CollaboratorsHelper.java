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

package com.tle.core.item.helper;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;

@SuppressWarnings("nls")
@Bind
@Singleton
public class CollaboratorsHelper extends AbstractHelper {
  @Override
  public void load(PropBagEx itemxml, Item bean) {
    for (String collab : bean.getCollaborators()) {
      itemxml.createNode("collaborativeowners/collaborator", collab);
    }
  }

  @Override
  public void save(PropBagEx xml, Item item, Set<String> handled) {
    final PropBagEx collabs = xml.getSubtree("collaborativeowners");
    if (collabs != null) {
      item.setCollaborators(iterate(collabs, "collaborator", new HashSet<String>()));
    }
    handled.add("collaborativeowners");
  }
}
