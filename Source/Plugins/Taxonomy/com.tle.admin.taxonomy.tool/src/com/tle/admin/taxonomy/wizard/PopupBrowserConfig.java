package com.tle.admin.taxonomy.wizard;

import static com.tle.common.taxonomy.wizard.PopupBrowserConstants.POPUP_ALLOW_BROWSING;
import static com.tle.common.taxonomy.wizard.PopupBrowserConstants.POPUP_ALLOW_SEARCHING;

import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.wizard.TermSelectorControl;

@SuppressWarnings("nls")
public class PopupBrowserConfig extends DynamicChoicePanel<TermSelectorControl>
{
	private JCheckBox browseMode;
	private JCheckBox searchMode;

	public PopupBrowserConfig()
	{
		super(new MigLayout("wrap 1, insets 0"));

		browseMode = new JCheckBox(s("browseMode"), true);
		searchMode = new JCheckBox(s("searchMode"), true);

		add(new JLabel("<html>" + getDescription()));
		add(new JLabel(s("mode")));
		add(browseMode, "gapleft 40px");
		add(searchMode, "gapleft 40px");

		changeDetector.watch(browseMode);
		changeDetector.watch(searchMode);
	}

	protected String getDescription()
	{
		return s("description");
	}

	@Override
	public void load(TermSelectorControl state)
	{
		browseMode.setSelected(state.getBooleanAttribute(POPUP_ALLOW_BROWSING, true));
		searchMode.setSelected(state.getBooleanAttribute(POPUP_ALLOW_SEARCHING, true));
	}

	@Override
	public void save(TermSelectorControl state)
	{
		Map<Object, Object> as = state.getAttributes();
		as.put(POPUP_ALLOW_BROWSING, browseMode.isSelected());
		as.put(POPUP_ALLOW_SEARCHING, searchMode.isSelected());
	}

	@Override
	public void removeSavedState(TermSelectorControl state)
	{
		Map<Object, Object> as = state.getAttributes();
		as.remove(POPUP_ALLOW_BROWSING);
		as.remove(POPUP_ALLOW_SEARCHING);
	}

	private String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.taxonomy.tool.wizard.popupBrowser." + keyPart);
	}
}
