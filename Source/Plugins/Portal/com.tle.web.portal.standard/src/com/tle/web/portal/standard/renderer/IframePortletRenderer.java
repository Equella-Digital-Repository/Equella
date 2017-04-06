package com.tle.web.portal.standard.renderer;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class IframePortletRenderer extends PortletContentRenderer<IframePortletRenderer.IframePortletRendererModel>
{
	private static final String IFRAME_HEIGHT = "300px";

	protected static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(IframePortletRenderer.class);

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionRenderable renderHtml(RenderEventContext context) throws Exception
	{
		final IframePortletRendererModel model = getModel(context);
		model.setUrl(portlet.getConfig());
		model.setHeight(IFRAME_HEIGHT);

		return view.createResult("iframeportlet.ftl", context);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "pif";
	}

	@Override
	public Class<IframePortletRendererModel> getModelClass()
	{
		return IframePortletRendererModel.class;
	}

	public static class IframePortletRendererModel
	{
		private String url;
		private String height;

		public String getUrl()
		{
			return url;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}

		public String getHeight()
		{
			return height;
		}

		public void setHeight(String height)
		{
			this.height = height;
		}
	}
}
