/*
 * Created on Sep 2, 2004 For "The Learning Edge"
 */
package com.tle.web.integration;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.layout.LayoutSelector;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionSession;
import com.tle.web.viewable.ViewableItem;

/**
 * @author jmaginnis
 */
@NonNullByDefault
public interface Integration<T extends IntegrationSessionData>
{
	T createDataForViewing(SectionInfo info);

	String getClose(T data);

	String getCourseInfoCode(T data);

	/**
	 * Location for storing in activations. (course folder?)
	 * 
	 * @param data
	 * @return
	 */
	NameValue getLocation(T data);

	void setupSingleSignOn(SectionInfo info, SingleSignonForm form) throws Exception;

	@Nullable
	SelectionSession setupSelectionSession(SectionInfo info, T data, SelectionSession session, SingleSignonForm model);

	void select(SectionInfo info, T data, SelectionSession session);

	LayoutSelector createLayoutSelector(SectionInfo info, T data);

	<I extends IItem<?>> ViewableItem<I> createViewableItem(ItemId itemId, boolean latest,
		@Nullable String itemExtensionType);

	LmsLinkInfo getLinkForResource(SectionInfo info, ViewableItem<? extends IItem<?>> vitem, SelectedResource resource,
		boolean relative, boolean attachmentUuidUrls);

	public static class LmsLinkInfo
	{
		private final IItem<?> resourceItem;
		private final IAttachment resourceAttachment;
		private final LmsLink lmsLink;

		protected LmsLinkInfo(IItem<?> resourceItem, IAttachment resourceAttachment, LmsLink lmsLink)
		{
			this.resourceItem = resourceItem;
			this.resourceAttachment = resourceAttachment;
			this.lmsLink = lmsLink;
		}

		public IItem<?> getResourceItem()
		{
			return resourceItem;
		}

		public IAttachment getResourceAttachment()
		{
			return resourceAttachment;
		}

		public LmsLink getLmsLink()
		{
			return lmsLink;
		}
	}

	public static class LmsLink
	{
		private String url;
		private String name;
		private String description;
		private String attachmentUuid;

		public LmsLink(String url, String name, String description, String attachmentUuid)
		{
			this.url = url;
			this.name = name;
			this.description = description;
			this.attachmentUuid = attachmentUuid;
		}

		public String getUrl()
		{
			return url;
		}

		public String getName()
		{
			return name;
		}

		public String getDescription()
		{
			return description;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public String getAttachmentUuid()
		{
			return attachmentUuid;
		}

		public void setAttachmentUuid(String attachmentUuid)
		{
			this.attachmentUuid = attachmentUuid;
		}
	}
}
