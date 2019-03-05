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

package com.tle.core.db

import java.util.Locale

import com.tle.beans.Institution
import com.tle.common.i18n.CurrentLocale
import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.{CurrentUser, UserState}
import com.tle.core.hibernate.CurrentDataSource
import javax.sql.DataSource

case class UserContext(inst: Institution, user: UserState, ds: DataSource, locale: Locale)

object UserContext {

  def fromThreadLocals(): UserContext = {
    UserContext(
      CurrentInstitution.get(),
      CurrentUser.getUserState,
      CurrentDataSource.get().getDataSource,
      CurrentLocale.getLocale
    )
  }

}
