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

package com.tle.web.api.report;

import com.google.inject.assistedinject.Assisted;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.report.Report;
import com.tle.core.guice.BindFactory;

@BindFactory
public interface ReportEditorFactory {
  ReportEditorImpl createExistingEditor(
      @Assisted Report report,
      @Assisted("stagingUuid") @Nullable String stagingUuid,
      @Assisted("lockId") @Nullable String lockId,
      @Assisted("editing") boolean editing,
      @Assisted("importing") boolean importing);

  ReportEditorImpl createNewEditor(
      @Assisted Report report,
      @Assisted("stagingUuid") @Nullable String stagingUuid,
      @Assisted("importing") boolean importing);
}
