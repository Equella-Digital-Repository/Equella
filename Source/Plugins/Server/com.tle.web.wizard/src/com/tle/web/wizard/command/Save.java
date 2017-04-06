package com.tle.web.wizard.command;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.section.WizardSectionInfo;

@SuppressWarnings("nls")
public class Save extends WizardCommand
{
	static
	{
		PluginResourceHandler.init(Save.class);
	}

	@PlugKey("command.save.save")
	private static String KEY_NAME;

	public Save()
	{
		super(KEY_NAME, "save");
	}

	@Override
	public JSHandler getJavascript(SectionInfo info, WizardSectionInfo winfo, JSCallable submitFunc)
	{
		WizardState state = winfo.getWizardState();
		if( !state.isInDraft() )
		{
			return null;
		}
		SaveDialog saveDialog = info.lookupSection(SaveDialog.class);
		return new OverrideHandler(saveDialog.getOpenFunction());
	}

	@Override
	public boolean isEnabled(SectionInfo info, WizardSectionInfo winfo)
	{
		WizardState state = winfo.getWizardState();
		return (state.isLockedForEditing() || state.isNewItem() || (!state.isLockedForEditing() && state
			.isRedraftAfterSave()));
	}

	@Override
	public void execute(SectionInfo info, WizardSectionInfo winfo, String type) throws Exception
	{
		SaveDialog saveDialog = info.lookupSection(SaveDialog.class);
		saveDialog.save(info, "save", null);
	}

	@Override
	public boolean isMajorAction()
	{
		return true;
	}

	@Override
	public String getStyleClass()
	{
		return "save";
	}
}
