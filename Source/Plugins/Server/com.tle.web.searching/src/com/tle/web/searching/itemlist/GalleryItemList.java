package com.tle.web.searching.itemlist;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.item.StandardItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.SectionRenderable;

@Bind
@SuppressWarnings("nls")
public class GalleryItemList extends StandardItemList
{
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@PlugURL("scripts/gallerypreview.js")
	private static String SCRIPT_URL;

	private final IncludeFile previewHandler = new IncludeFile(SCRIPT_URL);

	public static final String GALLERY_FLAG = "gallery.result";

	@Override
	protected SectionRenderable getRenderable(RenderEventContext context)
	{
		JSCallAndReference setupPreviews = new ExternallyDefinedFunction("setupPreviews", previewHandler);
		getTag(context).addReadyStatements(setupPreviews);
		return viewFactory.createResult("gallerylist.ftl", this);
	}

	@SuppressWarnings("nls")
	@Override
	protected void customiseListEntries(RenderContext context, List<StandardItemListEntry> entries)
	{
		getListSettings(context).setAttribute(GALLERY_FLAG, true);
		super.customiseListEntries(context, entries);
	}

	@Override
	protected Set<String> getExtensionTypes()
	{
		return Collections.singleton("gallery");
	}

}
