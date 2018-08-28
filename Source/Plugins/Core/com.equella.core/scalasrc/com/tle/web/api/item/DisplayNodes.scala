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

package com.tle.web.api.item

import com.dytech.devlib.PropBagEx
import com.tle.beans.entity.itemdef.DisplayNode
import com.tle.common.i18n.LangUtils
import com.tle.web.api.item.interfaces.beans.MetaDisplay

import scala.collection.JavaConverters._


object DisplayNodes {

  def create(itemxml: PropBagEx)(dn: DisplayNode): Option[MetaDisplay] = {
    val valueText = itemxml.iterateAll(dn.getNode).iterator().asScala.map { x =>
      LangUtils.getString(LangUtils.getBundleFromXml(x), "")
    }.mkString(dn.getSplitter)
    if (valueText.nonEmpty) Some {
      MetaDisplay(LangUtils.getString(dn.getTitle), valueText, dn.isDoubleMode, dn.getType)
    }
    else None
  }
}
