package com.tle.core.tasks;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.dao.AccessExpressionDao;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.security.SharePassService;
import com.tle.core.security.TLEAclManager;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
public class RemoveExpiredSecurity implements ScheduledTask
{
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private SharePassService sharePassService;
	@Inject
	private AccessExpressionDao accessExpressionDao;

	@Override
	public void execute()
	{
		sharePassService.removeExpiredPasses();
		aclManager.deleteExpiredAccessEntries();
		accessExpressionDao.deleteOrphanedExpressions();
	}
}
