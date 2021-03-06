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

package com.tle.web.itemlist.item;

import com.google.inject.Provider;
import com.tle.beans.item.Item;
import com.tle.common.filters.AndFilter;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ContainsParamFilter;
import com.tle.core.plugins.PluginTracker.NotContainsParamFilter;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.searching.SearchIndexModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.java.plugin.registry.Extension;

@Bind
@Singleton
public class StandardItemListEntryFactory {
  @Inject private PluginTracker<ItemlikeListEntryExtension> tracker;
  @Inject private PluginTracker<ItemListEntryFactoryExtension> factoryTracker;

  @Inject private Provider<StandardItemListEntry> entryFactory;

  public StandardItemListEntry createItemListItem(
      SectionInfo info, Item item, FreetextResult result, int index, int available) {
    StandardItemListEntry itemListItem = null;
    List<ItemListEntryFactoryExtension> extensions = factoryTracker.getBeanList();
    for (ItemListEntryFactoryExtension extension : extensions) {
      StandardItemListEntry entry = extension.createItemListEntry(item);
      if (entry != null) {
        itemListItem = entry;
        break;
      }
    }
    if (itemListItem == null) {
      itemListItem = entryFactory.get();
    }
    String searchPageUri = info.getTreeAttribute(ContextableSearchSection.SEARCHPAGE_ATTR);
    if (searchPageUri != null) {
      itemListItem.addModifier(new SearchIndexModifier(searchPageUri, index, available));
    }
    itemListItem.setInfo(info);
    itemListItem.setItem(item);
    itemListItem.setFreetextData(result);
    return itemListItem;
  }

  @SuppressWarnings({"nls", "unchecked"})
  public <LE extends ItemListEntry> List<ItemlikeListEntryExtension<Item, LE>> register(
      Set<String> types, SectionTree tree, String parentId) {
    List<ItemlikeListEntryExtension<Item, LE>> extensions = new ArrayList<>();
    AndFilter<Extension> filter =
        new AndFilter<Extension>(
            new ContainsParamFilter("applies", types),
            new NotContainsParamFilter("not-applies", types));
    for (Extension extension : tracker.getExtensions(filter)) {
      ItemlikeListEntryExtension<Item, LE> ext = tracker.getNewBeanByExtension(extension);
      ext.register(tree, parentId);
      extensions.add(ext);
    }
    return extensions;
  }
}
