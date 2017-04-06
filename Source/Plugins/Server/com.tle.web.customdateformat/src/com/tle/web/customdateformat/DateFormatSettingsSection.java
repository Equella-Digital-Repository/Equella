package com.tle.web.customdateformat;

import javax.inject.Inject;

import com.tle.beans.system.DateFormatSettings;
import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.userdetails.EditUserSection;

@Bind
@SuppressWarnings("nls")
public class DateFormatSettingsSection extends OneColumnLayout<DateFormatSettingsSection.DateFormatSettingsModel> 
{
	@PlugKey("settings.page.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@PlugKey("settings.dateformat.exact")
	private static String USE_EXACT;
	@PlugKey("settings.dateformat.relative")
	private static String USE_APPROX;
	
	@EventFactory
	private EventGenerator events;

	@Component
	private SingleSelectionList<NameValue> dateFormats;
	@Component
	@PlugKey("settings.save.button")
	private Button saveButton;

	@Inject
	private DateFormatSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;


	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		SimpleHtmlListModel<NameValue> dateFormatOptions = new SimpleHtmlListModel<NameValue>(new BundleNameValue(
			USE_APPROX, EditUserSection.DATE_FORMAT_APPROX), new BundleNameValue(USE_EXACT,
			EditUserSection.DATE_FORMAT_EXACT));
		dateFormats.setListModel(dateFormatOptions);
		dateFormats.setAlwaysSelect(true);

		saveButton.setClickHandler(events.getNamedHandler("save"));
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		securityProvider.checkAuthorised();
		final DateFormatSettings settings = getDateFormatSettings();
		if( settings.getDateFormat() != null )
		{
			dateFormats.setSelectedStringValue(info, settings.getDateFormat());
		}

		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "dateformatsettings.ftl", this));
	}

	private DateFormatSettings getDateFormatSettings()
	{
		return configService.getProperties(new DateFormatSettings());
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		final DateFormatSettings settings = getDateFormatSettings();
		settings.setDateFormat(dateFormats.getSelectedValueAsString(info));
		configService.setProperties(settings);
		receiptService.setReceipt(SAVE_RECEIPT_LABEL);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs) 
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());	
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public SingleSelectionList<NameValue> getDateFormats()
	{
		return dateFormats;
	}

	@Override
	public Class<DateFormatSettingsModel> getModelClass()
	{
		return DateFormatSettingsModel.class;
	}
	
	public static class DateFormatSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		// nothing
	}
}
