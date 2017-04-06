package com.tle.web.api.activation.impl;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.VersionSelection;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.BindFactory;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.api.activation.CourseBean;
import com.tle.web.api.activation.CourseEditor;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class CourseEditorImpl extends AbstractBaseEntityEditor<CourseInfo, CourseBean> implements CourseEditor
{
	@Inject
	private CourseInfoService courseService;

	@AssistedInject
	public CourseEditorImpl(@Assisted CourseInfo course, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("lockId") @Nullable String lockId, @Assisted("editing") boolean editing,
		@Assisted("importing") boolean importing)
	{
		super(course, stagingUuid, lockId, editing, importing);
	}

	@AssistedInject
	public CourseEditorImpl(@Assisted CourseInfo course, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("importing") boolean importing)
	{
		this(course, stagingUuid, null, false, importing);
	}

	@Override
	public void copyCustomFields(CourseBean bean)
	{
		super.copyCustomFields(bean);

		Boolean archived = bean.isArchived();
		if( archived != null )
		{
			entity.setDisabled(archived);
		}
		entity.setDisabled(editing);
		entity.setCode(bean.getCode());
		entity.setCitation(bean.getCitation());
		if( !Check.isEmpty(bean.getType()) )
		{
			entity.setCourseType(bean.getType().toLowerCase().charAt(0));
		}
		entity.setDepartmentName(bean.getDepartmentName());
		entity.setFrom(bean.getFrom());
		entity.setUntil(bean.getUntil());
		Integer students = bean.getStudents();
		if( students != null )
		{
			entity.setStudents(students);
		}
		entity.setCitation(bean.getCitation());
		String versionSelectionAsString = bean.getVersionSelection();
		if( !Check.isEmpty(versionSelectionAsString) )
		{
			VersionSelection versionSelection = VersionSelection.valueOf(versionSelectionAsString);
			entity.setVersionSelection(versionSelection);
		}
	}

	@Override
	protected AbstractEntityService<?, CourseInfo> getEntityService()
	{
		return courseService;
	}

	@BindFactory
	public interface CourseEditorFactory
	{
		CourseEditorImpl createExistingEditor(@Assisted CourseInfo course,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted("editing") boolean editing, @Assisted("importing") boolean importing);

		CourseEditorImpl createNewEditor(CourseInfo course, @Assisted("stagingUuid") @Nullable String stagingUuid,
			@Assisted("importing") boolean importing);
	}
}
