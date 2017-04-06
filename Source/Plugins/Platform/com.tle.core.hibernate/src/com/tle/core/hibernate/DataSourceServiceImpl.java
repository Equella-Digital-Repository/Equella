package com.tle.core.hibernate;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tle.common.Check;
import com.tle.core.config.guice.PropertiesModule;
import com.tle.core.guice.Bind;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Bind(DataSourceService.class)
@Singleton
@SuppressWarnings("nls")
public class DataSourceServiceImpl implements DataSourceService
{
	@Inject
	@Named("hibernate.connection.driver_class")
	private String driverClass;
	@Inject
	@Named("hibernate.dialect")
	private ExtendedDialect dialect;
	@Inject
	@Named("hibernate.connection.username")
	private String systemUsername;
	@Inject
	@Named("hibernate.connection.password")
	private String systemPassword;
	@Inject
	@Named("hibernate.connection.url")
	private String systemUrl;

	private final LoadingCache<SourceKey, DataSourceHolder> dsCache = CacheBuilder.newBuilder().weakValues()
		.build(CacheLoader.from(new CreateDataSourceFunction()));

	private DataSourceHolder systemDataSource;
	private final Properties baseConfig;

	public DataSourceServiceImpl() throws Exception
	{
		baseConfig = PropertiesModule.getPropertiesCache().get("/hikari.properties");
	}

	@Override
	public synchronized DataSourceHolder getSystemDataSource()
	{
		if( systemDataSource == null )
		{
			systemDataSource = getDataSource(systemUrl, systemUsername, systemPassword);
		}
		return systemDataSource;
	}

	@Override
	public String getSystemPassword()
	{
		return systemPassword;
	}

	@Override
	public String getSystemUrl()
	{
		return systemUrl;
	}

	@Override
	public String getSystemUsername()
	{
		return systemUsername;
	}

	@Override
	public DataSourceHolder getDataSource(String url, String username, String password)
	{
		return dsCache.getUnchecked(new SourceKey(url, username, password));
	}

	@Override
	public void removeDataSource(String url, String username, String password)
	{
		dsCache.invalidate(new SourceKey(url, username, password));
	}

	public class CreateDataSourceFunction implements Function<SourceKey, DataSourceHolder>
	{
		@Override
		public DataSourceHolder apply(SourceKey key)
		{
			final HikariConfig newConfig = new HikariConfig(baseConfig);
			newConfig.setDriverClassName(driverClass);
			newConfig.setUsername(key.getUsername());
			newConfig.setPassword(key.getPassword());
			newConfig.setJdbcUrl(key.getUrl());
			//Fixed properties: (i.e. do not allow override in hikari.properties)
			newConfig.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
			newConfig.setAutoCommit(false);
			newConfig.setInitializationFailFast(false);

			return new DataSourceHolder(new HikariDataSource(newConfig), dialect);
		}
	}

	@Override
	public ExtendedDialect getDialect()
	{
		return dialect;
	}

	private static class SourceKey
	{
		private final String url;
		private final String username;
		private final String password;

		public SourceKey(String url, String username, String password)
		{
			this.url = url;
			this.username = username;
			this.password = password;
		}

		public String getUrl()
		{
			return url;
		}

		public String getUsername()
		{
			return username;
		}

		public String getPassword()
		{
			return password;
		}

		@Override
		public int hashCode()
		{
			return Check.getHashCode(url, username, password);
		}

		@Override
		public boolean equals(Object obj)
		{
			if( this == obj )
			{
				return true;
			}
			if( !(obj instanceof SourceKey) )
			{
				return false;
			}

			SourceKey other = (SourceKey) obj;
			return url.equals(other.url) && username.equals(other.username) && password.equals(other.password);
		}
	}

	@Override
	public String getDriverClass()
	{
		return driverClass;
	}
}
