/**
 *
 */
package com.tle.web.manualdatafixes.fixes;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreeTextService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author larry
 */
@Bind
@SuppressWarnings("nls")
public class ReindexOpSection extends AbstractPrototypeSection<ReindexOpSection.ReindexModel> implements HtmlRenderer
{
	@Component
	@PlugKey("reindex.button")
	private Button execute;

	@AjaxFactory
	protected AjaxGenerator ajax;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Inject
	private FreeTextService freeTextService;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		execute.setClickHandler(events.getNamedHandler("reindex"));
		ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("reindex"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "reindex");
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("reindex.ftl", context);
	}

	@EventHandlerMethod
	public void reindex(SectionInfo info)
	{
		freeTextService.indexAll();
		getModel(info).setFired(true);
	}

	public Button getExecute()
	{
		return execute;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ReindexModel();
	}

	public static class ReindexModel
	{
		@Bookmarked
		private boolean fired;

		public boolean isFired()
		{
			return fired;
		}

		public void setFired(boolean fired)
		{
			this.fired = fired;
		}
	}
}
