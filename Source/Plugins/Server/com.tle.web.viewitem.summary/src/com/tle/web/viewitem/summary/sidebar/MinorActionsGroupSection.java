package com.tle.web.viewitem.summary.sidebar;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ExtensionParamComparator;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.render.HideableFromDRMModel;
import com.tle.web.sections.equella.render.HideableFromDRMSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionSection;

@SuppressWarnings("nls")
public class MinorActionsGroupSection
	extends
		AbstractParentViewItemSection<MinorActionsGroupSection.MinorActionsGroupModel>
	implements
		HideableFromDRMSection

{
	private PluginTracker<GenericMinorActionSection> minorActionsTracker;
	private List<GenericMinorActionSection> minorActionSections;

	@Override
	public SectionResult renderHtml(final RenderEventContext context)
	{
		if( !canView(context) || !getItemInfo(context).getViewableItem().isItemForReal() )
		{
			return null;
		}

		final MinorActionsGroupModel model = getModel(context);
		final List<SectionRenderable> renderables = Lists.newArrayList();

		List<GenericMinorActionSection> sortedSections = Lists.newArrayList(minorActionSections);
		sortedSections.sort(new Comparator<GenericMinorActionSection>()
		{
			@Override
			public int compare(GenericMinorActionSection mas1, GenericMinorActionSection mas2)
			{
				return mas1.getLinkText().compareTo(mas2.getLinkText());
			}
		});

		sortedSections.stream().forEachOrdered(section -> {
			final SectionRenderable renderable = renderSection(context, section);
			if( renderable != null )
			{
				renderables.add(renderable);
			}
		});
		model.setSections(renderables);

		return viewFactory.createResult("viewitem/summary/sidebar/basiclistgroup.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		minorActionSections = Lists.newArrayList(minorActionsTracker.getBeanList());
		minorActionSections.stream().forEach(section -> tree.registerInnerSection(section, id));
	}

	// Called by Freemarker
	public String getGroupTitleKey()
	{
		return "summary.sidebar.actionsgroup.title";
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return getModel(info).isHide() ? false : Lists.newArrayList(minorActionSections).stream()
			.filter(derp -> derp.canView(info)).findFirst().isPresent();
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	@Override
	public void hideSection(SectionInfo info)
	{
		getModel(info).setHide(true);
	}

	@Override
	public void unhideSection(SectionInfo info)
	{
		getModel(info).setHide(false);
	}

	@Override
	public Class<MinorActionsGroupModel> getModelClass()
	{
		return MinorActionsGroupModel.class;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		minorActionsTracker = new PluginTracker<GenericMinorActionSection>(pluginService,
			MinorActionsGroupSection.class, "minorAction", "id", new ExtensionParamComparator());
		minorActionsTracker.setBeanKey("class");
	}

	public static class MinorActionsGroupModel implements HideableFromDRMModel
	{
		private List<SectionRenderable> sections;

		private boolean hide;

		public List<SectionRenderable> getSections()
		{
			return sections;
		}

		public void setSections(List<SectionRenderable> sections)
		{
			this.sections = sections;
		}

		@Override
		public boolean isHide()
		{
			return hide;
		}

		@Override
		public void setHide(boolean hide)
		{
			this.hide = hide;
		}
	}
}
