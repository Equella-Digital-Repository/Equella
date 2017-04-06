package com.tle.common.recipientselector;

import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class TabbedFinder extends JTabbedPane implements UserGroupRoleFinder, FinderListener
{
	private static final long serialVersionUID = 1L;

	private EventListenerList eventListenerList;
	private int currentTab;

	@SuppressWarnings("nls")
	public TabbedFinder(RemoteUserService userService, RecipientFilter... filters)
	{
		List<RecipientFilter> fs = Arrays.asList(filters);

		if( fs.contains(RecipientFilter.ROLES) || fs.contains(RecipientFilter.GROUPS)
			|| fs.contains(RecipientFilter.USERS) )
		{
			addFinder(CurrentLocale.get("com.tle.admin.recipients.tabbedfinder.search"), new SearchFinder(userService,
				filters));
		}

		if( fs.contains(RecipientFilter.GROUPS) || fs.contains(RecipientFilter.USERS) )
		{
			addFinder(CurrentLocale.get("com.tle.admin.recipients.tabbedfinder.browse"), new BrowseFinder(userService,
				filters));
		}

		if( fs.contains(RecipientFilter.IP_ADDRESS) || fs.contains(RecipientFilter.HOST_REFERRER) )
		{
			addFinder(CurrentLocale.get("com.tle.admin.recipients.tabbedfinder.network"), new IpAddressFinder());
		}

		if( fs.contains(RecipientFilter.EXPRESSION) )
		{
			addFinder(CurrentLocale.get("com.tle.admin.recipients.tabbedfinder.other"), new SpecialUsersFinder(
				userService, !fs.contains(RecipientFilter.NO_OWNER)));
		}

		addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				int newTab = getSelectedIndex();
				if( newTab != currentTab )
				{
					currentTab = newTab;
					UserGroupRoleFinder finder = getSelectedFinder();
					finder.clearAll();
				}
			}
		});
	}

	private void addFinder(String name, UserGroupRoleFinder tab)
	{
		JComponent comp = (JComponent) tab;
		comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tab.addFinderListener(this);
		addTab(name, comp);
	}

	@Override
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);

		final int count = getTabCount();
		for( int i = 0; i < count; i++ )
		{
			UserGroupRoleFinder tab = (UserGroupRoleFinder) getComponentAt(i);
			tab.setEnabled(b);
		}
	}

	@Override
	public RecipientFilter getSelectedFilter()
	{
		return getSelectedFinder().getSelectedFilter();
	}

	@Override
	public void clearAll()
	{
		currentTab = 0;
		setSelectedIndex(currentTab);
		getSelectedFinder().clearAll();
	}

	@Override
	public void setSingleSelectionOnly(boolean b)
	{
		final int count = getTabCount();
		for( int i = 0; i < count; i++ )
		{
			UserGroupRoleFinder tab = (UserGroupRoleFinder) getComponentAt(i);
			tab.setSingleSelectionOnly(b);
		}
	}

	@Override
	public void addFinderListener(FinderListener listener)
	{
		if( eventListenerList == null )
		{
			eventListenerList = new EventListenerList();
		}
		eventListenerList.add(FinderListener.class, listener);
	}

	@Override
	public List<Object> getSelectedResults()
	{
		return getSelectedFinder().getSelectedResults();
	}

	@Override
	public void valueChanged(FinderEvent e)
	{
		if( eventListenerList != null )
		{
			e.setSource(this);
			for( FinderListener l : eventListenerList.getListeners(FinderListener.class) )
			{
				l.valueChanged(e);
			}
		}
	}

	private UserGroupRoleFinder getSelectedFinder()
	{
		return (UserGroupRoleFinder) getSelectedComponent();
	}
}
