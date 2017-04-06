package com.tle.core.migration;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import com.google.common.base.Throwables;
import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.HibernateFactory;
import com.tle.core.hibernate.HibernateFactoryService;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;

public abstract class AbstractHibernateMigration extends AbstractMigration
{
	@Inject
	private HibernateFactoryService hibernateService;

	protected abstract Class<?>[] getDomainClasses();

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	protected HibernateMigrationHelper createMigrationHelper()
	{
		HibernateFactory configuration = hibernateService.createConfiguration(CurrentDataSource.get(),
			getDomainClasses());
		return new HibernateMigrationHelper(configuration);
	}

	protected void runSqlStatements(final List<String> sqlStatements, SessionFactory factory,
		final MigrationResult result, String statusKey) throws Exception
	{
		result.setupSubTaskStatus(statusKey, sqlStatements.size());
		runInTransaction(factory, new HibernateCall()
		{
			@Override
			public void run(Session session) throws Exception
			{
				executeSqlStatements(result, session, sqlStatements);
			}
		});
	}

	protected void executeSqlStatements(MigrationResult result, Session session, List<String> sqlStatements)
	{
		for( String statement : sqlStatements )
		{
			try
			{
				session.createSQLQuery(statement).executeUpdate();
				result.addLogEntry(new MigrationStatusLog(statement, false));
				result.incrementStatus();
			}
			catch( Exception e )
			{
				result.setMessage("Error running SQL: '" + statement + "' "); //$NON-NLS-1$ //$NON-NLS-2$
				result.addLogEntry(new MigrationStatusLog(statement, true));
				throw e;
			}
		}
	}

	protected void runInTransaction(SessionFactory factory, HibernateCall call) throws Exception
	{
		Transaction trans = null;
		Session session = null;
		try
		{
			session = factory.openSession();
			trans = session.beginTransaction();
			call.run(session);
			trans.commit();
		}
		catch( Exception t )
		{
			if( trans != null )
			{
				trans.rollback();
			}
			Throwables.propagate(t);
		}
		finally
		{
			if( session != null )
			{
				session.close();
			}
		}
	}

	public interface HibernateCall
	{
		void run(Session session) throws Exception;
	}
}
