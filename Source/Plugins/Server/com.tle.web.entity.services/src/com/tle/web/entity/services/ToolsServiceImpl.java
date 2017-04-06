package com.tle.web.entity.services;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.guice.Bind;
import com.tle.core.remoting.ToolsService;
import com.tle.core.services.RemoteCachingService;
import com.tle.core.services.item.ItemService;
import com.tle.core.user.CurrentUser;
import com.tle.core.util.ItemHelper;
import com.tle.core.util.ItemHelper.ItemHelperSettings;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.core.workflow.operations.WorkflowOperation;

@Bind
@Singleton
public class ToolsServiceImpl extends AbstractSoapService implements ToolsService
{
	@Inject
	private RemoteCachingService remoteCachingService;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private WorkflowFactory workflowFactory;

	@Override
	public String[] getCacheList(String ssid, String lastUpdate)
	{
		try
		{
			authenticate(ssid);
			final List<String> l = remoteCachingService.getCacheList(lastUpdate);
			return l.toArray(new String[l.size()]);
		}
		catch( final Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getCacheSchedule(String ssid)
	{
		try
		{
			authenticate(ssid);
			// Right away!
			return new UtcDate().format(Dates.ISO_WITH_TIMEZONE);
		}
		catch( final Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void archive(String session, String uuid, int version, String itemdef)
	{
		try
		{
			authenticate(session);
			ItemId key = new ItemId(uuid, version);
			itemService.operation(key, new WorkflowOperation[]{workflowFactory.archive()});
		}
		catch( final Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String newVersion(String session, String uuid, int version, boolean copyAttachments)
	{
		try
		{
			authenticate(session);

			ItemId key = new ItemId(uuid, version);
			ItemPack pack = itemService.operation(key, workflowFactory.newVersion(copyAttachments));

			return itemHelper.convertToXml(pack, new ItemHelperSettings(false)).toString();
		}
		catch( final Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getUserId(String session)
	{
		return CurrentUser.getUserID();
	}
}
