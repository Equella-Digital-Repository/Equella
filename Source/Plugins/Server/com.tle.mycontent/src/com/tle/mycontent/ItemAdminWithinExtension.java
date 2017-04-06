package com.tle.mycontent;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.ItemStatus;
import com.tle.common.search.PresetSearch;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.mycontent.service.MyContentService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemadmin.WithinEntry;
import com.tle.web.itemadmin.WithinExtension;
import com.tle.web.itemadmin.section.ItemAdminQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;

@Bind
@TreeIndexed
public class ItemAdminWithinExtension extends AbstractPrototypeSection<Object> implements WithinExtension
{

	@TreeLookup
	private ItemAdminQuerySection querySection;
	@ViewFactory
	protected FreemarkerFactory viewFactory;

	@Inject
	private ItemDefinitionService itemDefService;
	@Inject
	private MyContentService myContentService;

	@PlugKey("itemadmin.label")
	private static Label LABEL_MY_CONTENT;

	@Override
	public void register(String parentId, SectionTree tree)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public void populateModel(SectionInfo info, List<WithinEntry> list)
	{
		String defUuid = myContentService.getMyContentItemDef().getUuid();

		for( BaseEntityLabel bel : itemDefService.listAllIncludingSystem() )
		{
			if( bel.getUuid().equals(defUuid) )
			{
				WithinEntry entry = new WithinEntry(bel, querySection.getCollectionsLabel(), null, false, 1);
				entry.setOverrideLabel(LABEL_MY_CONTENT);
				entry.setSimpleOpsOnly(true);
				list.add(entry);
				return;
			}
		}
	}

	@Override
	public PresetSearch createDefaultSearch(SectionInfo info, WithinEntry selected)
	{
		PresetSearch search = new PresetSearch(null, null, false);
		search.setItemStatuses(ItemStatus.PERSONAL);
		return search;
	}

	@Override
	public SectionRenderable render(RenderEventContext context)
	{
		return null;
	}
}
