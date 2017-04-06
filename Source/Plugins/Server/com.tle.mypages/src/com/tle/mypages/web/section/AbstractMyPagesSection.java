package com.tle.mypages.web.section;

import com.tle.mypages.web.event.LoadItemEventListener;
import com.tle.mypages.web.event.SaveItemEventListener;
import com.tle.mypages.web.event.SavePageEventListener;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;

/**
 * @author aholland
 */
public abstract class AbstractMyPagesSection<M> extends AbstractPrototypeSection<M>
{
	protected static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(AbstractMyPagesSection.class);

	@EventFactory
	protected EventGenerator events;

	@ViewFactory(fixed = false)
	protected FreemarkerFactory viewFactory;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		if( this instanceof SavePageEventListener )
		{
			tree.addListener(null, SavePageEventListener.class, id);
		}

		if( this instanceof LoadItemEventListener )
		{
			tree.addListener(null, LoadItemEventListener.class, id);
		}

		if( this instanceof SaveItemEventListener )
		{
			tree.addListener(null, SaveItemEventListener.class, id);
		}
	}
}
