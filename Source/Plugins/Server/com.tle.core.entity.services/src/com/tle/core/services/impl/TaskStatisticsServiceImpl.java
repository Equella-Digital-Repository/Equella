package com.tle.core.services.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.tle.beans.TaskHistory;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.Item;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.dao.TaskHistoryDao;
import com.tle.core.guice.Bind;
import com.tle.core.services.TaskStatisticsService;
import com.tle.core.services.TaskTrend;
import com.tle.core.services.entity.WorkflowService;

@Bind(TaskStatisticsService.class)
@Singleton
@SuppressWarnings("nls")
public class TaskStatisticsServiceImpl implements TaskStatisticsService
{
	@Inject
	private TaskHistoryDao taskHistoryDao;
	@Inject
	private WorkflowService workflowService;

	@Override
	public void enterTask(Item item, WorkflowItem task, Date entry)
	{
		TaskHistory old = taskHistoryDao.findByCriteria(Restrictions.eq("task.id", task.getId()),
			Restrictions.eq("item.id", item.getId()), Restrictions.isNull("exitDate"));
		if( old != null )
		{
			throw new Error("Task History never exited for this task: " + CurrentLocale.get(task.getName()) + "   ID="
				+ task.getId());
		}
		taskHistoryDao.save(new TaskHistory(item, task, entry, null));
	}

	@Override
	@Transactional
	public void exitTask(Item item, WorkflowItem task, Date exit)
	{
		TaskHistory th = taskHistoryDao.findByCriteria(Restrictions.eq("task.id", task.getId()),
			Restrictions.eq("item.id", item.getId()), Restrictions.isNull("exitDate"));
		th.setExitDate(exit);
		taskHistoryDao.update(th);
	}

	@Override
	@Transactional
	public void exitAllTasksForItem(Item item, Date end)
	{
		if( item.isModerating() )
		{
			taskHistoryDao.exitAllTasksForItem(item, end);
		}
	}

	@Override
	@Transactional
	public void restoreTasksForItem(Item item)
	{
		taskHistoryDao.restoreTasksForItem(item);
	}

	@Override
	@Transactional
	public List<TaskTrend> getWaitingTasks(Trend trend)
	{
		// Get all tasks
		Collection<BaseEntityLabel> listManagable = workflowService.listManagable();
		Collection<String> manageableUuids = Collections2.transform(listManagable,
			new Function<BaseEntityLabel, String>()
			{
				@Override
				public String apply(BaseEntityLabel input)
				{
					return input.getUuid();
				}

			});
		return taskHistoryDao.getTaskTrendsForWorkflows(manageableUuids, getTrendDate(trend));

	}

	@Override
	@Transactional
	public List<TaskTrend> getWaitingTasksForWorkflow(String uuid, Trend trend)
	{
		return taskHistoryDao.getTaskTrendsForWorkflows(Collections.singleton(uuid), getTrendDate(trend));
	}

	private Date getTrendDate(Trend trend)
	{
		final Date now = new Date();
		return new Date(now.getTime() - TimeUnit.DAYS.toMillis(trend.getDays()));
	}
}
