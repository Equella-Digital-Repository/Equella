package com.tle.core.institution.migration.v50;

import java.util.Date;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AddTimestampToBadUrl extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddTimestampToBadUrl.class) + "."; //$NON-NLS-1$

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Do Nothing
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("badurl", "lastchecked");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeBadURL.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "badurl.addtimestamp.title", keyPrefix + "badurl.addtimestamp.description");
	}

	@Entity(name = "BadURL")
	@AccessType("field")
	public static class FakeBadURL
	{
		@Id
		long id;
		Date lastchecked;
	}

}
