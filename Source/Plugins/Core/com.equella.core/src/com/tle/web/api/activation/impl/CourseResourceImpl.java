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

package com.tle.web.api.activation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;

import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.entity.EnumerateOptions;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.api.activation.CourseBean;
import com.tle.web.api.activation.CourseBeanSerializer;
import com.tle.web.api.activation.CourseResource;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.entity.PagedResults;
import com.tle.web.api.entity.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.PagingBean;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.remoting.rest.service.RestImportExportHelper;
import scala.Option;

/**
 * @author larry
 */
@SuppressWarnings("nls")
@Bind(CourseResource.class)
@Singleton
public class CourseResourceImpl extends AbstractBaseEntityResource<CourseInfo, BaseEntitySecurityBean, CourseBean>
	implements
		CourseResource
{
	@Inject
	private CourseInfoService courseService;
	@Inject
	private CourseBeanSerializer courseSerializer;
	@Inject
	private TLEAclManager aclService;

	private static String KEY_PFX = AbstractPluginService.getMyPluginId(CourseResource.class)+".";


	/**
	 * Provide the full course data in the results
	 */
	@Override
	public CourseBean serialize(CourseInfo entity, Object data, boolean heavy)
	{
		return super.serialize(entity, data, true);
	}

//	@Override
//	public PagingBean<CourseBean> list(UriInfo uriInfo, String code, String q)
//	{
//		final boolean isExport = RestImportExportHelper.isExport(uriInfo);
//		final EnumerateOptions opts = new EnumerateOptions(q, null, 0, 100000, isExport, null);
//		if (!Check.isEmpty(code))
//		{
//			opts.addParameter("code", code);
//		}
//		return list(opts, isExport);
//	}

	@Override
	protected void validate(String uuid, CourseBean bean, boolean isNew) throws InvalidDataException
	{
		super.validate(uuid, bean, isNew);
		final String courseCode = bean.getCode();
		if( courseCode != null )
		{
			final CourseInfo courseSameCode = courseService.getByCode(courseCode);
			if( courseSameCode != null && (isNew || !uuid.equals(courseSameCode.getUuid())) )
			{
				throw new InvalidDataException(new ValidationError("code",
					CurrentLocale.get(KEY_PFX + "course.edit.validation.codeinuse", courseCode)));
			}
		}
	}

	@Override
	public PagingBean<CourseBean> list(UriInfo uriInfo, String code, String q, boolean archived, List<String> privilege,
									   String resumption, int length, boolean full)
	{
		final boolean isExport = RestImportExportHelper.isExport(uriInfo);
		return PagedResults.pagedResults(this, q, privilege, resumption, length, full | isExport, isExport, archived);
	}

	public List<String> citation(UriInfo uriInfo, String uuid)
	{
		return courseService.getAllCitations();
	}

	@Override
	public CourseInfoService getEntityService()
	{
		return courseService;
	}

	@Override
	protected BaseEntitySerializer<CourseInfo, CourseBean> getSerializer()
	{
		return courseSerializer;
	}

	@Override
	protected Class<?> getResourceClass()
	{
		return CourseResource.class;
	}

	@Override
	protected Node[] getAllNodes()
	{
		return new Node[]{Node.ALL_COURSE_INFO};
	}

	@Override
	protected BaseEntitySecurityBean createAllSecurityBean()
	{
		return new BaseEntitySecurityBean();
	}
}
