package com.tle.mypages.web.section;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.Schema;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.util.ItemHelper;
import com.tle.mypages.web.event.LoadItemEvent;
import com.tle.mypages.web.event.LoadItemEventListener;
import com.tle.mypages.web.event.SaveItemEvent;
import com.tle.mypages.web.event.SaveItemEventListener;
import com.tle.mypages.web.model.MyPagesContributeModel;
import com.tle.mypages.web.model.MyPagesTitleModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author aholland
 */
public class MyPagesTitleSection extends AbstractMyPagesSection<MyPagesTitleModel>
	implements
		HtmlRenderer,
		SaveItemEventListener,
		LoadItemEventListener
{
	@Component
	// TODO: it should be stateful = false
	private TextField titleField;

	@Inject
	private ItemHelper itemHelper;

	@TreeLookup
	private MyPagesContributeSection contribSection;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		MyPagesContributeModel model = contribSection.getModel(context);

		if( !model.isModal() )
		{
			return viewFactory.createResult("mypagesitemtitle.ftl", context); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public Class<MyPagesTitleModel> getModelClass()
	{
		return MyPagesTitleModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "mypttl"; //$NON-NLS-1$
	}

	public TextField getTitleField()
	{
		return titleField;
	}

	@Override
	public void doSaveItemEvent(SectionInfo info, SaveItemEvent event)
	{
		// check the description and veto the save if empty
		String description = titleField.getValue(info);
		if( Check.isEmpty(description) )
		{
			MyPagesTitleModel model = getModel(info);
			model.setError(true);
			event.setCommit(false);
		}
		else
		{
			final ItemPack<Item> itemPack = event.getItemPack();
			final Item item = itemPack.getItem();
			final PropBagEx itemXml = itemPack.getXml();
			final Schema schema = item.getItemDefinition().getSchema();

			itemXml.setNode(schema.getItemNamePath(), description);

			itemHelper.updateItemFromXml(itemPack);
		}
	}

	@Override
	public void doLoadItemEvent(SectionInfo info, LoadItemEvent event)
	{
		titleField.setValue(info,
			CurrentLocale.get(event.getItem().getName(), CurrentLocale.get(RESOURCES.key("description.untitled")))); //$NON-NLS-1$
	}
}
