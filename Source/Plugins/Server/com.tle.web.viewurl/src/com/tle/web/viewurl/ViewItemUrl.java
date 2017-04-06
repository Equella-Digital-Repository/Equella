package com.tle.web.viewurl;

import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.common.Constants;
import com.tle.core.services.UrlService;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.generic.InfoBookmark;

@SuppressWarnings("nls")
public class ViewItemUrl implements Bookmark
{
	// public static int FLAG_NO_BACK = 1;
	public static final int FLAG_IGNORE_TRANSIENT = 2;
	public static final int FLAG_FULL_URL = 4;
	/**
	 * This means the url represents a resource which can leave of some
	 * parameters, for example hiding navigation doesn't need to be included.
	 */
	public static final int FLAG_IS_RESOURCE = 8;
	public static final int FLAG_FOR_PREVIEW = 16;
	// public static int FLAG_PASSTHROUGH_BACKTO = 32;
	public static final int FLAG_NO_SELECTION = 64;

	public static final int FLAG_IGNORE_SESSION_TEMPLATE = 128;
	public static final int FLAG_PRESERVE_PARAMS = 256;

	public static final String VIEWONLY_CONTEXT = "VIEWING_ONLY";

	private String itemdir;
	private UrlEncodedString filepath;
	private String queryString;
	private int flags;
	private boolean skipDrm = false;
	private Boolean showNav;
	private String viewer;
	private final SectionInfo info;
	private String anchor;

	private List<ItemUrlExtender> extenders;
	private List<BookmarkModifier> modifiers;
	private String href;
	private final UrlService urlService;

	public ViewItemUrl(SectionInfo info, String itemdir, UrlEncodedString filepath, UrlService urlService, int flags)
	{
		this.info = info;
		this.itemdir = itemdir;
		this.filepath = filepath;
		this.urlService = urlService;
		this.flags = flags;
		this.anchor = Constants.BLANK;
	}

	public SectionInfo getSectionInfo()
	{
		return info;
	}

	public ViewItemUrl add(BookmarkModifier modifier)
	{
		if( modifiers == null )
		{
			modifiers = new ArrayList<BookmarkModifier>();
		}
		modifiers.add(modifier);
		return this;
	}

	public ViewItemUrl add(ItemUrlExtender extender)
	{
		if( extenders == null )
		{
			extenders = new ArrayList<ItemUrlExtender>();
		}
		extenders.add(extender);
		return this;
	}

	public void setSkipDrm(boolean skipDrm)
	{
		this.skipDrm = skipDrm;
	}

	public void forward(SectionInfo from)
	{
		flags |= FLAG_FULL_URL;
		from.forwardToUrl(getHref() + getAnchor());
	}

	public void setShowNav(boolean showNav)
	{
		this.showNav = showNav;
	}

	public String getItemdir()
	{
		return itemdir;
	}

	public void setItemdir(String itemdir)
	{
		this.itemdir = itemdir;
	}

	public void addAll(List<ItemUrlExtender> extenderList)
	{
		if( extenders == null )
		{
			extenders = new ArrayList<ItemUrlExtender>();
		}
		extenders.addAll(extenderList);
	}

	public String getQueryString()
	{
		if( queryString == null )
		{
			BookmarkEvent bookmarkEvent = new BookmarkEvent();
			if( (flags & FLAG_IGNORE_TRANSIENT) != 0 )
			{
				bookmarkEvent.setIgnoredContexts(BookmarkEvent.CONTEXT_SESSION);
			}
			if( (flags & (FLAG_NO_SELECTION | FLAG_IS_RESOURCE)) != 0 )
			{
				bookmarkEvent.setContexts(VIEWONLY_CONTEXT);
			}

			BookmarkAndModify bookmark = new BookmarkAndModify(new InfoBookmark(info, bookmarkEvent), modifiers);
			// I'm not one to take a free horse in the mouth
			ViewItemUrlProcessor processor = info.lookupSection(ViewItemUrlProcessor.class);
			processor.processModel(info, this);

			if( extenders != null )
			{
				for( ItemUrlExtender extender : extenders )
				{
					extender.execute(info);
				}
			}
			queryString = bookmark.getQuery();
		}
		return queryString;
	}

	@Override
	public String getHref()
	{
		if( href == null )
		{
			href = itemdir + filepath;
			String query = getQueryString();
			if( query.length() > 0 )
			{
				href += '?' + query;
			}
			if( (flags & FLAG_FULL_URL) != 0 )
			{
				href = urlService.institutionalise(href);
			}
		}
		return href;
	}

	public int getFlags()
	{
		return flags;
	}

	public ViewItemUrl addFlag(int flag)
	{
		flags |= flag;
		return this;
	}

	public void removeFlag(int flag)
	{
		flags = flags & ~flag;
	}

	public boolean isSkipDrm()
	{
		return skipDrm;
	}

	public boolean isShowNav()
	{
		return showNav == null || showNav;
	}

	public boolean isShowNavOveridden()
	{
		return showNav != null;
	}

	public UrlEncodedString getFilepath()
	{
		return filepath;
	}

	public void setFilepath(UrlEncodedString filepath)
	{
		this.filepath = filepath;
	}

	public String getViewer()
	{
		return viewer;
	}

	public ViewItemUrl setViewer(String viewer)
	{
		this.viewer = viewer;
		return this;
	}

	public String getAnchor()
	{
		return anchor;
	}

	public void setAnchor(String anchor)
	{
		this.anchor = anchor;
	}
}