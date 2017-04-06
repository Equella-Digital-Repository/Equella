package com.tle.web.connectors.manage;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.connectors.service.ConnectorItemKey;
import com.tle.core.guice.Bind;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.bulk.operation.BulkOperationExtension.OperationInfo;
import com.tle.web.bulk.section.AbstractBulkResultsDialog;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.Option;

@Bind
@NonNullByDefault
public class ConnectorBulkResultsDialog extends AbstractBulkResultsDialog<ConnectorItemKey>
{
	@PlugKey("opresults.count")
	private static String OPRESULTS_COUNT_KEY;

	@Inject
	private BulkRemoveContentOperation removeOp;
	@Inject
	private BulkMoveContentOperation moveOp;

	@TreeLookup
	private AbstractBulkSelectionSection<ConnectorItemKey> selectionSection;

	@Override
	protected DynamicHtmlListModel<OperationInfo> getBulkOperationList(SectionTree tree, String parentId)
	{
		return new ConnectorBulkOperationList(tree, parentId);
	}

	public class ConnectorBulkOperationList extends DynamicHtmlListModel<OperationInfo>
	{
		private final List<BulkOperationExtension> bulkOps = new ArrayList<BulkOperationExtension>();

		public ConnectorBulkOperationList(SectionTree tree, String parentId)
		{
			bulkOps.add(removeOp);
			bulkOps.add(moveOp);
			for( BulkOperationExtension operation : bulkOps )
			{
				operation.register(tree, parentId);
			}
		}

		@Override
		protected Iterable<Option<OperationInfo>> populateOptions(SectionInfo info)
		{
			List<Option<OperationInfo>> ops = new ArrayList<Option<OperationInfo>>();
			for( BulkOperationExtension operation : bulkOps )
			{
				operation.addOptions(info, ops);
			}
			return ops;
		}

		@Nullable
		@Override
		protected Iterable<OperationInfo> populateModel(SectionInfo info)
		{
			return null;
		}
	}

	@Override
	protected Label getOpResultCountLabel(int totalSelections)
	{
		return new PluralKeyLabel(OPRESULTS_COUNT_KEY, totalSelections);
	}

	@Override
	protected List<com.tle.web.bulk.section.AbstractBulkResultsDialog.SelectionRow> getRows(
		List<ConnectorItemKey> pageOfIds)
	{
		List<SelectionRow> rows = new ArrayList<SelectionRow>();

		for( ConnectorItemKey itemId : pageOfIds )
		{
			rows.add(new SelectionRow(new TextLabel(itemId.getTitle()), new HtmlComponentState(RendererConstants.LINK,
				events.getNamedHandler("removeSelection", itemId)))); //$NON-NLS-1$
		}
		return rows;
	}

	@EventHandlerMethod
	public void removeSelection(SectionInfo info, String itemId)
	{
		selectionSection.removeSelection(info, new ConnectorItemKey(itemId));
	}
}
