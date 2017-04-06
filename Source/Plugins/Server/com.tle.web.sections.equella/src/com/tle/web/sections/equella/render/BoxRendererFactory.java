package com.tle.web.sections.equella.render;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.tle.core.accessibility.AccessibilityModeService;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.component.model.BoxState;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.FreemarkerComponentRendererFactory;

/**
 * @author aholland
 */
@Bind
@Singleton
public class BoxRendererFactory extends FreemarkerComponentRendererFactory
{
	@Inject
	private AccessibilityModeService accessibilityService;

	@Override
	public SectionRenderable getRenderer(RendererFactory rendererFactory, SectionInfo info, String renderer,
		HtmlComponentState state)
	{
		return new BoxRenderer(factory, (BoxState) state, accessibilityService.isAccessibilityMode()); // NOSONAR
	}
}
