package com.tle.web.wizard.command;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.section.PreviewSection;
import com.tle.web.wizard.section.WizardSectionInfo;

public class Preview extends WizardCommand
{
	@PlugKey("command.preview")
	private static String KEY_PREVIEW;

	static
	{
		PluginResourceHandler.init(Preview.class);
	}

	public Preview()
	{
		super(KEY_PREVIEW, "preview"); //$NON-NLS-1$
	}

	@Override
	public boolean isEnabled(SectionInfo info, WizardSectionInfo winfo)
	{
		WizardState state = winfo.getWizardState();
		return (state.isLockedForEditing() || state.isNewItem() || (!state.isLockedForEditing() && state
			.isRedraftAfterSave()));
	}

	@Override
	public JSHandler getJavascript(SectionInfo info, WizardSectionInfo winfo, JSCallable submitFunction)
	{
		PreviewSection preview = info.lookupSection(PreviewSection.class);
		return new OverrideHandler(preview.getOpenFunction());
	}

	@Override
	public void execute(SectionInfo info, WizardSectionInfo winfo, String data) throws Exception
	{
		// Nothing to do
	}

	@Override
	public boolean addToMoreActionList()
	{
		return true;
	}
}
