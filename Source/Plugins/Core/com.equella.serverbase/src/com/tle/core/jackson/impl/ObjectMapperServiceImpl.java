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

package com.tle.core.jackson.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.MapperExtension;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ParamFilter;
import java.util.List;
import org.java.plugin.registry.Extension;

@NonNullByDefault
@Bind(ObjectMapperService.class)
@Singleton
@SuppressWarnings("nls")
public class ObjectMapperServiceImpl implements ObjectMapperService {
  @Inject private PluginTracker<MapperExtension> mapperTracker;

  @Override
  public ObjectMapper createObjectMapper(String... named) {
    ObjectMapper mapper = new ObjectMapper();
    if (named.length != 0) {
      List<Extension> extensions = mapperTracker.getExtensions(new ParamFilter("mapper", named));
      for (Extension mapperExtension : extensions) {
        mapperTracker.getBeanByExtension(mapperExtension).extendMapper(mapper);
      }
    }
    return mapper;
  }
}
