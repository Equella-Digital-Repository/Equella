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

package com.tle.core.settings.loginnotice.impl;

import java.time.ZonedDateTime;

enum ScheduleSettings {
  OFF,
  ON,
  SCHEDULED
}

public class PreLoginNotice {
  private String notice = "";
  private ScheduleSettings scheduleSettings = ScheduleSettings.OFF;
  private ZonedDateTime startDate;
  private ZonedDateTime endDate;

  public String getNotice() {
    return notice;
  }

  public void setNotice(String notice) {
    this.notice = notice;
  }

  public ScheduleSettings getScheduleSettings() {
    return scheduleSettings;
  }

  public void setScheduleSettings(ScheduleSettings scheduleSettings) {
    this.scheduleSettings = scheduleSettings;
  }

  public void setStartDate(ZonedDateTime startDate) {
    this.startDate = startDate;
  }

  public void setEndDate(ZonedDateTime endDate) {
    this.endDate = endDate;
  }

  public ZonedDateTime getStartDate() {
    return startDate;
  }

  public ZonedDateTime getEndDate() {
    return endDate;
  }
}
