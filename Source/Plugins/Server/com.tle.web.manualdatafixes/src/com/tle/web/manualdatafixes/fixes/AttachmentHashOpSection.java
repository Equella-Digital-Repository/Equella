package com.tle.web.manualdatafixes.fixes;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.core.services.impl.Task;
import com.tle.core.services.item.ItemService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.workflow.filters.BaseFilter;
import com.tle.core.workflow.filters.FilterResultListener;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.manualdatafixes.ManualDataFixModel;
import com.tle.web.manualdatafixes.UpdateTaskStatus;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class AttachmentHashOpSection extends AbstractPrototypeSection<AttachmentHashOpSection.AttachmentHashOpModel>
	implements
		HtmlRenderer,
		UpdateTaskStatus
{
	private static final String TASK_ID = "Attachment-MD5-Summing";

	@PlugKey("fix.attachmenthash.task.key")
	private static String TASK_KEY;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Inject
	private TaskService taskService;
	@Inject
	private ItemService itemService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private Provider<AttachmentMD5HashFilter> filterProvider;

	@Component
	@PlugKey("fix.attachmenthash.execute")
	private Button execute;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		execute.setClickHandler(events.getNamedHandler("startMd5ing"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		AttachmentHashOpModel model = getModel(context);
		TaskStatus status = model.getTaskStatus();
		if( status != null && !status.isFinished() )
		{
			model.setTaskLabel(new KeyLabel(TASK_KEY, status.getDoneWork(), status.getMaxWork()));
			model.setInProgress(true);
		}

		return viewFactory.createResult("attachmenthash.ftl", context);
	}

	@EventHandlerMethod
	public void startMd5ing(SectionInfo info)
	{
		long instId = CurrentInstitution.get().getUniqueId();
		taskService.getGlobalTask(new BeanClusteredTask(TASK_ID + instId, AttachmentHashOpSection.class, "createTask",
			instId), TimeUnit.SECONDS.toMillis(20));
	}

	public Task createTask(final long currentInstitution)
	{
		return new SingleShotTask()
		{
			@Override
			public void runTask() throws Exception
			{
				Institution inst = institutionService.getInstitution(currentInstitution);
				runAs.executeAsSystem(inst, new Runnable()
				{
					@Override
					public void run()
					{
						itemService.operateAll(filterProvider.get(), new FilterResultListener()
						{
							@Override
							public void succeeded(ItemKey itemId, ItemPack pack)
							{
								incrementWork();
							}

							@Override
							public void failed(ItemKey itemId, Item item, Throwable e)
							{
								incrementWork();
							}

							@Override
							public void total(int total)
							{
								setupStatus(TASK_KEY, total);
							}
						});
					}
				});
			}

			@Override
			protected String getTitleKey()
			{
				return "fix.attachmenthash.task";
			}
		};
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new AttachmentHashOpModel();
	}

	@NonNullByDefault(false)
	public class AttachmentHashOpModel extends ManualDataFixModel
	{
		@Override
		public TaskStatus getTaskStatus()
		{
			if( checkedStatus )
			{
				return taskStatus;
			}

			String taskId = taskService.getRunningGlobalTask(TASK_ID + CurrentInstitution.get().getUniqueId());
			taskService.askTaskChanges(Collections.singleton(taskId));

			if( taskId != null )
			{
				taskStatus = taskService.waitForTaskStatus(taskId, 2000);
			}
			else
			{
				taskStatus = null;
			}

			checkedStatus = true;

			return taskStatus;
		}
	}

	@Bind
	public static class AttachmentMD5HashFilter extends BaseFilter
	{
		@Inject
		private Provider<AttachmentHashOperation> attOpFactory;

		@Override
		protected WorkflowOperation[] createOperations()
		{
			return new AbstractWorkflowOperation[]{attOpFactory.get()};
		}

		@Override
		public String getJoinClause()
		{
			return "JOIN i.attachments a";
		}

		@Override
		public String getWhereClause()
		{
			return "(a.md5sum IS NULL OR a.md5sum = '') AND (a.class = FileAttachment OR a.class = CustomAttachment )";
		}
	}

	public Button getExecute()
	{
		return execute;
	}

	@Override
	public String getAjaxId()
	{
		return "hash_status";
	}

	@Override
	public boolean isFinished(SectionInfo info)
	{
		TaskStatus taskStatus = getModel(info).getTaskStatus();
		return taskStatus != null ? taskStatus.isFinished() : true;
	}
}
