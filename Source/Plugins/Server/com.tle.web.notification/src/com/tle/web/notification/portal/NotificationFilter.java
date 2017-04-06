package com.tle.web.notification.portal;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.tle.common.Check;
import com.tle.common.search.DefaultSearch;
import com.tle.core.guice.BindFactory;
import com.tle.core.notification.indexer.NotificationIndex;
import com.tle.core.notification.indexer.NotificationSearch;
import com.tle.web.notification.filters.FilterByNotificationReason;
import com.tle.web.notification.section.RootNotificationListSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.workflow.portal.TaskListSubsearch;

public class NotificationFilter implements TaskListSubsearch
{
	@PlugKey("filternames.")
	private static String KEY_FILTERNAMES;

	private final String identifier;
	private final String reason;
	private final boolean secondLevel;
	@Inject
	private SectionsController sectionsController;

	@Inject
	public NotificationFilter(@Assisted("identifier") String identifier, @Assisted("reason") String reason,
		@Assisted boolean secondLevel)
	{
		this.identifier = identifier;
		this.reason = reason;
		this.secondLevel = secondLevel;
	}

	@Override
	public String getIdentifier()
	{
		return identifier;
	}

	@Override
	public DefaultSearch getSearch()
	{
		NotificationSearch search = new NotificationSearch();
		if( !Check.isEmpty(reason) )
		{
			search.addMust(NotificationIndex.FIELD_REASON, reason);
		}
		return search;
	}

	public String getReason()
	{
		return reason;
	}

	@Override
	public boolean isSecondLevel()
	{
		return secondLevel;
	}

	@Override
	public SectionInfo setupForward(SectionInfo from)
	{
		SectionInfo forward;
		if( from != null )
		{
			forward = from.createForward(RootNotificationListSection.URL);
		}
		else
		{
			forward = sectionsController.createForward(RootNotificationListSection.URL);
		}
		FilterByNotificationReason filter = forward.lookupSection(FilterByNotificationReason.class);
		filter.setReason(forward, reason);
		return forward;
	}

	@Override
	public Label getName()
	{
		return new KeyLabel(KEY_FILTERNAMES + identifier);
	}

	@Override
	public String getParentIdentifier()
	{
		return NotifcationPortalConstants.ID_ALL;
	}

	@BindFactory
	public interface NotificationFilterFactory
	{
		NotificationFilter create(@Assisted("identifier") String id, @Assisted("reason") String reason,
			boolean secondLevel);

	}
}
