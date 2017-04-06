package com.tle.core.workflow.migrate;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class AddUserPathMigration extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddUserPathMigration.class) + "."; //$NON-NLS-1$

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{WorkflowItemAdd.class};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Entity(name = "WorkflowNode")
	public static class WorkflowItemAdd
	{
		@Id
		long id;
		@Column(length = 512)
		String userPath;
		@Column(length = 40)
		String userSchemaUuid;
	}

	@Override
	@SuppressWarnings("nls")
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "migratepath.title", keyPrefix + "migratepath.description");
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// nothing
	}

	@SuppressWarnings("nls")
	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("workflow_node", "user_path", "user_schema_uuid");
	}

	@SuppressWarnings("nls")
	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getAddIndexesAndConstraintsForColumns("workflow_node", "user_path", "user_schema_uuid");
	}
}
