package com.tle.core.activation.tasks;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.workflow.ActivationStatusOperation;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.filters.BaseFilter;
import com.tle.core.workflow.operations.WorkflowOperation;

@Bind
@Singleton
public class UpdateActivationStatusTask implements ScheduledTask
{
	@Inject
	private ItemService itemService;
	@Inject
	private Provider<ActivationStatusFilter> filterFactory;

	@Override
	public void execute()
	{
		itemService.operateAll(filterFactory.get());
	}

	@Bind
	public static class ActivationStatusFilter extends BaseFilter
	{
		@Inject
		private ActivationService activationService;
		@Inject
		private Provider<ActivationStatusOperation> opFactory;

		@Override
		public WorkflowOperation[] createOperations()
		{
			return new WorkflowOperation[]{opFactory.get(), workflowFactory.reindexOnly(false)};
		}

		@Override
		public FilterResults getItemIds()
		{
			return new FilterResults(activationService.getAllActivatedItemsForInstitution());
		}
	}
}
