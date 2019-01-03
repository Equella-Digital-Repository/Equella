/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.core.integration.impl;

import javax.inject.Singleton;

import com.tle.core.auditlog.impl.AbstractAuditLogDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.integration.IntegrationLoggingDao;
import com.tle.core.integration.beans.AuditLogLms;

@Bind(IntegrationLoggingDao.class)
@Singleton
public class IntegrationLoggingDaoImpl extends AbstractAuditLogDaoImpl<AuditLogLms> implements IntegrationLoggingDao
{

	public IntegrationLoggingDaoImpl()
	{
		super(AuditLogLms.class);
	}

}
