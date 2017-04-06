package com.tle.web.mimetypes.section;

import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.common.Constants;
import com.dytech.edge.common.ValidationException;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.mimetypes.MimeEditExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.MutableList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.StringListModel;

@Bind
public class MimeDetailsSection extends AbstractPrototypeSection<MimeDetailsSection.MimeDetailsModel>
	implements
		MimeEditExtension,
		HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	private TextField description;
	@Component
	private TextField type;
	@Component
	private TextField newExtension;
	@Component
	private MutableList<String> extensions;
	@Component
	private Button addExtensionButton;
	@Component
	private Button removeExtensionButton;
	@AjaxFactory
	private AjaxGenerator ajax;

	private List<String> ajaxIds;

	public static class MimeDetailsModel
	{
		// nothing
	}

	@Override
	public Class<MimeDetailsModel> getModelClass()
	{
		return MimeDetailsModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "det"; //$NON-NLS-1$
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		extensions.setListModel(new StringListModel());
		addExtensionButton.addClickStatements(
			new FunctionCallStatement(extensions.createAddFunction(), newExtension.createGetExpression(), newExtension
				.createGetExpression()),
			new ScriptStatement(JQuerySelector.valueSetExpression(newExtension, Constants.BLANK)));
		removeExtensionButton.addClickStatements(new FunctionCallStatement(extensions.createRemoveFunction()));
		ajaxIds = new ArrayList<String>();
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		String[] ids = ajaxIds.toArray(new String[ajaxIds.size()]);
		type.setEventHandler(JSHandler.EVENT_BLUR,
			new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, null, null, ids)));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("mimedetails.ftl", context); //$NON-NLS-1$
	}

	@Override
	public void loadEntry(SectionInfo info, MimeEntry entry)
	{
		if( entry != null )
		{
			description.setValue(info, entry.getDescription());
			type.setValue(info, entry.getType());
			newExtension.setValue(info, ""); //$NON-NLS-1$
			extensions.getListModel().setValues(info, new ArrayList<String>(entry.getExtensions()));
		}
	}

	@Override
	public void saveEntry(SectionInfo info, MimeEntry entry)
	{
		if( Check.isEmpty(type.getValue(info)) )
		{
			throw new ValidationException("mimetype.empty"); //$NON-NLS-1$
		}
		entry.setDescription(description.getValue(info));
		entry.setType(type.getValue(info));

		List<String> extensionsList = extensions.getValues(info);
		for( String ext : extensionsList )
		{
			if( ext.length() >= 20 )
			{
				throw new ValidationException("extensions.length"); //$NON-NLS-1$
			}
		}
		entry.setExtensions(extensions.getValues(info));
	}

	@Override
	public NameValue getTabToAppearOn()
	{
		return MimeTypesEditSection.TAB_DETAILS;
	}

	@Override
	public boolean isVisible(SectionInfo info)
	{
		return true;
	}

	public TextField getDescription()
	{
		return description;
	}

	public TextField getType()
	{
		return type;
	}

	public TextField getNewExtension()
	{
		return newExtension;
	}

	public MutableList<String> getExtensions()
	{
		return extensions;
	}

	public Button getAddExtensionButton()
	{
		return addExtensionButton;
	}

	public Button getRemoveExtensionButton()
	{
		return removeExtensionButton;
	}

	public void addAjaxId(String ajaxId)
	{
		ajaxIds.add(ajaxId);
	}
}
