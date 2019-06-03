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

package com.tle.web.api.settings

import java.net.URI

import com.tle.common.institution.CurrentInstitution
import com.tle.core.db.{DB, RunWithDB}
import com.tle.core.security.AclChecks
import com.tle.core.settings.SettingsDB
import com.tle.web.settings.{EditableSettings, SettingsList, UISettings}
import io.swagger.annotations.Api
import javax.ws.rs.{GET, PUT, Path, Produces}

case class SettingTypeLinks(web: Option[URI], rest: Option[URI], route: Option[String])
case class SettingType(id: String,
                       name: String,
                       description: String,
                       group: String,
                       links: SettingTypeLinks)

object SettingTypeLinks {
  def apply(instUri: URI, ed: EditableSettings): SettingTypeLinks = ed.uriType match {
    case "rest"     => SettingTypeLinks(None, Option(instUri.resolve(ed.uri)), None)
    case "external" => SettingTypeLinks(Option(instUri.resolve(ed.uri)), None, None)
    case _          => SettingTypeLinks(Option(instUri.resolve(ed.uri)), None, Some("/" + ed.uri))
  }
}

@Path("settings/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class SettingsResource {

  @GET
  def settings: Iterable[SettingType] = {
    val baseUri = CurrentInstitution.get().getUrlAsUri
    SettingsList.allSettings.filter(_.isEditable).map { s =>
      SettingType(s.id, s.name, s.description, s.group, SettingTypeLinks(baseUri, s))
    }
  }

  @GET
  @Path("ui")
  def getUISettings: UISettings =
    RunWithDB.execute(UISettings.getUISettings).getOrElse(UISettings.defaultSettings)

  @PUT
  @Path("ui")
  def setUISettings(in: UISettings): Unit = RunWithDB.executeWithPostCommit(
    SettingsDB.ensureEditSystem(UISettings.setUISettings(in))
  )
}
