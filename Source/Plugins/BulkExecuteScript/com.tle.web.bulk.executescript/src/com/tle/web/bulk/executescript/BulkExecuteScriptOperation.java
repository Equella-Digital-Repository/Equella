package com.tle.web.bulk.executescript;

import java.util.Collection;
import java.util.List;

import com.dytech.edge.common.ScriptContext;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.scripting.service.StandardScriptContextParams;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.web.bulk.executescript.operations.ExecuteScriptFactory;
import com.tle.web.bulk.executescript.section.BulkExecuteScriptSection;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.model.Option;

@Bind
public class BulkExecuteScriptOperation extends BulkExecuteScriptSection implements BulkOperationExtension
{
	private static final String BULK_EXEC_VAL = "executescript"; //$NON-NLS-1$

	@PlugKey("bulkop.executescript")
	private static String LABEL_EXECUTE;
	@PlugKey("bulkop.executescript.title")
	private static Label LABEL_EXECUTE_TITLE;

	@Inject
	private ItemService itemService;
	@Inject
	private ScriptingService scriptService;

	@BindFactory
	public interface ExecuteSriptExecutorFactory
	{
		ExecuteScriptExecutor create(@Assisted("script") String script);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{
		options.add(new KeyOption<OperationInfo>(LABEL_EXECUTE, BULK_EXEC_VAL, new OperationInfo(this, BULK_EXEC_VAL)));
	}

	@Override
	public BeanLocator<ExecuteScriptExecutor> getExecutor(SectionInfo info, String operationId)
	{
		return new FactoryMethodLocator<ExecuteScriptExecutor>(ExecuteSriptExecutorFactory.class, "create",
			getScript(info));
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{
		// Nothing
	}

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		return renderSection(context, this);
	}

	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return LABEL_EXECUTE_TITLE;
	}

	@Override
	public boolean areOptionsFinished(SectionInfo info, String operationId)
	{
		return !(Check.isEmpty(getScript(info))) && !(getModel(info).isValidationErrors());
	}

	@Override
	public boolean hasExtraOptions(SectionInfo info, String operationId)
	{
		return true;
	}

	// stupid name??
	public static class ExecuteScriptExecutor implements BulkOperationExecutor
	{
		private final String script;

		@Inject
		private ExecuteScriptFactory exScriptFactory;
		@Inject
		private WorkflowFactory workflowFactory;

		@Inject
		public ExecuteScriptExecutor(@Assisted("script") String script)
		{
			this.script = script;

		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			return new WorkflowOperation[]{workflowFactory.startEdit(true), exScriptFactory.executeScript(script),
					workflowFactory.save()};
		}

		@Override
		public String getTitleKey()
		{
			return "com.tle.web.bulk.executescript.bulk.execscript.title";
		}
	}

	@Override
	public boolean hasExtraNavigation(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public Collection<Button> getExtraNavigation(SectionInfo info, String operationId)
	{
		return null;
	}

	@Override
	public boolean hasPreview(SectionInfo info, String operationId)
	{
		return true;
	}

	@Override
	public boolean showPreviousButton(SectionInfo info, String opererationId)

	{
		return true;
	}

	@Override
	public ItemPack runPreview(SectionInfo info, String operationId, long itemId) throws Exception
	{
		ItemKey itemKey = itemService.getItemIdKey(itemId);
		ItemPack itemPack = itemService.getItemPack(itemKey);
		StandardScriptContextParams params = new StandardScriptContextParams(itemPack, new ItemFile(itemKey), false,
			null);
		ScriptContext scriptContext = scriptService.createScriptContext(params);
		scriptService.executeScript(getScript(info), "bulkExecute", scriptContext, false);

		return itemPack;
	}

}
