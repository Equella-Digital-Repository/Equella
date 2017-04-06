package com.tle.web.connectors.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.common.connectors.service.ConnectorItemKey;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.ClusteredTask;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.core.services.impl.Task;
import com.tle.core.services.item.ItemService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.core.workflow.filters.FilterResultListener;
import com.tle.core.workflow.filters.WorkflowFilter;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkResult;

@Bind(ConnectorBulkOperationService.class)
@Singleton
public class ConnectorBulkOperationServiceImpl implements ConnectorBulkOperationService
{
	@Inject
	private ItemService itemService;
	@Inject
	private RunAsInstitution runAs;

	@SuppressWarnings("nls")
	@Override
	public ClusteredTask createTask(Collection<? extends ItemKey> items,
		BeanLocator<? extends BulkOperationExecutor> executor)
	{
		BeanClusteredTask clusteredTask = new BeanClusteredTask(null, ConnectorBulkOperationService.class,
			"createNewTask", CurrentUser.getUserState(), (Serializable) items, executor);
		return clusteredTask;
	}

	public Task createNewTask(UserState userState, Collection<ItemIdKey> items,
		BeanLocator<? extends BulkOperationExecutor> executor)
	{
		return new ConnectorBulkOperationTask(userState, items, executor.get());
	}

	public class ConnectorBulkOperationTask extends SingleShotTask
	{
		private final UserState userState;
		private final Collection<? extends ItemKey> items;
		private final BulkOperationExecutor executor;

		public ConnectorBulkOperationTask(UserState userState, Collection<? extends ItemKey> items,
			BulkOperationExecutor executor)
		{
			this.userState = userState;
			this.items = items;
			this.executor = executor;
		}

		@Override
		protected String getTitleKey()
		{
			return null;
		}

		@Override
		public void runTask() throws Exception
		{
			final ConnectorBulkWorkflowFilter filter = new ConnectorBulkWorkflowFilter(this, items, executor);
			runAs.execute(userState, new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					itemService.operateAll(filter, filter);
					return null;
				}
			});
		}
	}

	public static class ConnectorBulkWorkflowFilter implements WorkflowFilter, FilterResultListener
	{
		private final Collection<? extends ItemKey> items;
		private final BulkOperationExecutor executor;
		private final Task task;

		public ConnectorBulkWorkflowFilter(Task task, Collection<? extends ItemKey> items,
			BulkOperationExecutor executor)
		{
			this.task = task;
			this.items = items;
			this.executor = executor;
		}

		@Override
		public FilterResults getItemIds()
		{
			return new FilterResults(items);
		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			return executor.getOperations();
		}

		@Override
		public boolean isReadOnly()
		{
			return false;
		}

		@Override
		public void succeeded(ItemKey itemId, ItemPack pack)
		{
			ConnectorItemKey itemKey = (ConnectorItemKey) itemId;
			task.addLogEntry(new BulkResult(true, itemKey.getTitle(), null));
		}

		@Override
		public void failed(ItemKey itemId, Item item, Throwable e)
		{
			ConnectorItemKey itemKey = (ConnectorItemKey) itemId;
			task.addLogEntry(new BulkResult(false, itemKey.getTitle(), e.getMessage()));

		}

		@Override
		public void setDateNow(Date now)
		{
			// nothing
		}

		@Override
		public void total(int total)
		{
			// Nothing
		}
	}
}
