package com.tle.web.htmleditor.tinymce.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.web.htmleditor.HtmlEditorButtonDefinition;
import com.tle.web.htmleditor.HtmlEditorFactoryInterface;
import com.tle.web.htmleditor.tinymce.TinyMceAddOn;
import com.tle.web.htmleditor.tinymce.TinyMceModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.AbstractRenderedComponent;

/**
 * @author aholland
 */
public interface TinyMceService extends HtmlEditorFactoryInterface
{
	/**
	 * E.g. textAreaComponent.addReadyStatements(context,
	 * tinyMceService.setupEditorStatements(textAreaComponent, model));
	 * 
	 * @param textAreaComponent
	 * @param model
	 */
	void preRender(PreRenderContext context, AbstractRenderedComponent<?> textAreaComponent, TinyMceModel model);

	/**
	 * Bind this to the JSHandler.EVENT_PRESUBMIT so that AJAX posts will read
	 * the up-to-date html E.g.
	 * textAreaComponent.setEventHandler(JSHandler.EVENT_PRESUBMIT,
	 * tinyMceService.getPreSubmitHandler(textAreaComponent));
	 * 
	 * @param textAreaComponent
	 * @return
	 */
	JSHandler getPreSubmitHandler(ElementId textAreaComponent);

	/**
	 * @param textAreaComponent
	 * @return
	 */
	JSHandler getToggleFullscreeenHandler(ElementId textAreaComponent, ElementId link);

	List<TinyMceAddOn> getAddOns();

	/**
	 * Used internally
	 * 
	 * @param info
	 * @param model
	 * @param properties
	 * @param formId Control form ID, or null
	 */
	void populateModel(SectionInfo info, TinyMceModel model, Map<String, String> properties,
		boolean restrictedCollections, boolean restrictedDynacolls, boolean restrictedSearches,
		boolean restrictedContributables, Map<Class<?>, Set<String>> searchableUuids, Set<String> contributableUuids);

	JSCallable getDisableFunction(ElementId element, ElementId fullScreenLinkElement);

	LinkedHashMap<String, HtmlEditorButtonDefinition> getButtons(SectionInfo info);

	List<List<String>> getDefaultButtonConfiguration();
}
