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

package com.tle.common.workflow.node;

import com.tle.beans.entity.LanguageBundle;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import org.hibernate.annotations.AccessType;

@Entity(name = "WorkflowSerial")
@AccessType("field")
@DiscriminatorValue("s")
public class SerialNode extends WorkflowTreeNode {
  private static final long serialVersionUID = 1;

  public SerialNode(LanguageBundle name) {
    super(name);
  }

  public SerialNode() {
    super();
  }

  @Override
  public char getType() {
    return 's';
  }

  @Override
  public boolean canHaveSiblingRejectPoints() {
    return true;
  }
}
