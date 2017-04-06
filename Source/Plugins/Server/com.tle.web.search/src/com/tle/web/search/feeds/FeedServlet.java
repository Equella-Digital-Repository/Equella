package com.tle.web.search.feeds;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndLinkImpl;
import com.sun.syndication.io.WireFeedOutput;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.system.SearchSettings;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.security.RunAsUser;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.item.FreeTextService;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.user.UserService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.exceptions.AuthenticationException;
import com.tle.web.i18n.BundleCache;
import com.tle.web.itemlist.item.ItemList;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.login.LogonSection;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractRootSearchSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.equella.search.event.SearchResultsListener;
import com.tle.web.sections.generic.DummyRenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;

@SuppressWarnings("nls")
@Bind
@Singleton
public class FeedServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	public static final String AUTH_BASIC = "basic";
	private static final String SERVLET_PATH = "feed";
	private static final String PARAM_FEED = "feed";
	private static final String PARAM_PATH = "path";
	private static final String PARAM_LENGTH = "length";
	private static final String PARAM_AUTHTYPE = "auth";

	@Inject
	private SectionsController controller;
	@Inject
	private ConfigurationService configService;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private UrlService urlService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private FreeTextService freeTextService;
	@Inject
	private UserService userService;
	@Inject
	private RunAsUser runAsUser;

	public BookmarkModifier getModifier(final SectionInfo info, final String feedType, final String authType)
	{
		return new BookmarkModifier()
		{
			@Override
			public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState)
			{
				String authValue = authType;
				if( Check.isEmpty(authType)
					&& configService.getProperties(new SearchSettings()).isAuthenticateFeedsByDefault() )
				{
					authValue = AUTH_BASIC;
				}
				if( !Check.isEmpty(authValue) )
				{
					bookmarkState.put(PARAM_AUTHTYPE, new String[]{authValue});
				}
				bookmarkState.put(PARAM_FEED, new String[]{feedType});
				bookmarkState.put(PARAM_PATH, new String[]{info.getAttribute(SectionInfo.KEY_PATH)});
				bookmarkState.put(SectionInfo.KEY_PATH, new String[]{urlService.institutionalise(SERVLET_PATH)});
			}
		};
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		int length = 10;
		Map<String, String[]> params = new LinkedHashMap<String, String[]>(req.getParameterMap());
		String feedType = removeParam(params, PARAM_FEED);
		String path = removeParam(params, PARAM_PATH);
		String slength = removeParam(params, PARAM_LENGTH);
		if( slength != null )
		{
			length = Math.min(50, Integer.parseInt(slength));
			if( length < 5 )
			{
				length = 5;
			}
		}
		String authtype = removeParam(params, PARAM_AUTHTYPE);
		boolean asGuest = authtype == null;
		if( !asGuest && handleAuth(req, resp, authtype) )
		{
			return;
		}
		OutputRunnable runnable = new OutputRunnable(path, params, req, resp, length, feedType);
		if( asGuest )
		{
			runAsUser.executeAsGuest(CurrentInstitution.get(), runnable, userService.getWebAuthenticationDetails(req));
		}
		else
		{
			runnable.run();
		}
	}

	private boolean handleAuth(HttpServletRequest req, HttpServletResponse resp, String authtype) throws IOException
	{
		if( !CurrentUser.isGuest() )
		{
			return false;
		}
		if( AUTH_BASIC.equals(authtype) )
		{
			String auth = req.getHeader("Authorization");
			if( auth != null && auth.toUpperCase().startsWith("BASIC ") )
			{
				// Get encoded user and password, comes after "BASIC "
				String userpassEncoded = auth.substring(6);

				// Decode it, using any base 64 decoder
				String userpassDecoded = new String(Base64.decodeBase64(userpassEncoded.getBytes()));
				String[] split = userpassDecoded.split(":");
				if( split.length >= 2 )
				{
					String username = split[0];
					String password = split[1];
					try
					{
						UserState authenticated = userService.authenticate(username, password,
							userService.getWebAuthenticationDetails(req));
						CurrentUser.setUserState(authenticated);
						return false;
					}
					catch( AuthenticationException ae )
					{
						// fall through
					}
				}
			}
			resp.setHeader("WWW-Authenticate", "BASIC realm=\"EQUELLA\"");
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return true;
		}
		StringBuilder requestUrl = new StringBuilder(urlService.removeInstitution(req.getRequestURL().toString()));
		LogonSection.forwardToLogon(controller, req, resp, requestUrl.append('?').append(req.getQueryString())
			.toString(), LogonSection.STANDARD_LOGON_PATH);
		return true;
	}

	public class OutputRunnable implements Runnable
	{
		private final String path;
		private final SectionInfo info;
		private final HttpServletResponse response;
		private final String feedType;
		private final int length;

		public OutputRunnable(String path, Map<String, String[]> params, HttpServletRequest req,
			HttpServletResponse resp, int length, String feedType)
		{
			this.path = path;
			this.info = controller.createInfo(path, req, resp, null, params, null);
			this.response = resp;
			this.feedType = feedType;
			this.length = length;
		}

		@Override
		public void run()
		{
			// Not terribly efficient
			AbstractRootSearchSection<?> rootSearch = info.lookupSection(AbstractRootSearchSection.class);
			AbstractFreetextResultsSection<?, ?> searchResults = info
				.lookupSection(AbstractFreetextResultsSection.class);

			FreetextSearchEvent event = searchResults.createSearchEvent(info);
			info.processEvent(event);

			FreetextSearchResults<FreetextResult> results = freeTextService.search(event.getFinalSearch(), 0, length);
			if( feedType.equals("rss_2.0") )
			{
				response.setContentType("application/rss+xml; charset=UTF-8");
			}
			else
			{
				response.setContentType("application/atom+xml; charset=UTF-8");
			}
			SyndFeed feed = getFeed(info, searchResults, results);

			feed.setFeedType(feedType);
			String title = rootSearch.getTitle(info).getText();
			feed.setTitle(title);
			String urlPath = path;
			if( urlPath != null && urlPath.startsWith("/") )
			{
				urlPath = urlPath.substring(1);
			}
			feed.setLink(urlService.institutionalise(urlPath));
			feed.setDescription(title);
			WireFeed wfeed = feed.createWireFeed(feedType);
			if( wfeed instanceof Feed )
			{
				// add compulsory Atom fields
				Feed atomFeed = (Feed) wfeed;
				atomFeed.setId(urlService.institutionalise("atom_1.0"));
				atomFeed.setUpdated(new Date());
			}

			WireFeedOutput outputter = new WireFeedOutput();
			try
			{
				outputter.output(wfeed, response.getWriter());
			}
			catch( Exception fe )
			{
				throw new RuntimeException(fe);
			}
		}
	}

	private String removeParam(Map<String, String[]> params, String param)
	{
		String[] paramVals = params.remove(param);
		if( paramVals != null )
		{
			return paramVals[0];
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected SyndFeed getFeed(SectionInfo info, AbstractFreetextResultsSection<?, ?> searchResults,
		FreetextSearchResults<FreetextResult> results)

	{
		SyndFeed feed = new SyndFeedImpl();
		List<SyndEntryImpl> entries = new ArrayList<SyndEntryImpl>();
		ItemList<? extends ItemListEntry> itemList = searchResults.getItemList(info);
		if( itemList instanceof SearchResultsListener )
		{
			((SearchResultsListener<FreetextSearchResultEvent>) itemList).processResults(info,
				new FreetextSearchResultEvent(results, null, 0));
		}
		int i = 0;
		for( Item item : results.getResults() )
		{
			FreetextResult freetextResult = results.getResultData(i);
			itemList.addItem(info, item, freetextResult);
			i++;
		}
		List<? extends ItemListEntry> itemEntries = itemList.initEntries(new DummyRenderContext(info));
		for( ItemListEntry listEntry : itemEntries )
		{
			Item item = listEntry.getItem();
			SyndEntryImpl entry = new SyndEntryImpl();
			HtmlLinkState title = listEntry.getTitle();
			entry.setTitleEx(new LabelContent(title.getLabel()));
			String url = urlService.institutionalise(title.getBookmark().getHref());
			entry.setUri(url);
			entry.setLink(url);
			entry.setPublishedDate(item.getDateModified());
			entry.setDescription(new LabelContent(listEntry.getDescription()));

			final List<SyndEnclosure> contents = new ArrayList<SyndEnclosure>();
			final List<SyndLink> links = new ArrayList<SyndLink>();
			for( final Attachment attach : item.getAttachmentsUnmodifiable() )
			{
				if( attach.getAttachmentType() == AttachmentType.LINK )
				{
					final SyndLink link = new SyndLinkImpl();
					link.setRel("related");
					link.setHref(attach.getUrl());
					link.setTitle(attach.getDescription());
					links.add(link);
				}

				final SyndEnclosure enclosure = buildEnclosure(url, attach);
				if( enclosure != null )
				{
					contents.add(enclosure);
				}
			}
			entry.setEnclosures(contents);
			entry.setLinks(links);

			SyndCategory cat = new LabelCategory(new BundleLabel(item.getItemDefinition().getName(), bundleCache));
			entry.setCategories(Collections.singletonList(cat));

			entries.add(entry);
			i++;
		}
		feed.setEntries(entries);

		return feed;
	}

	private SyndEnclosure buildEnclosure(final String baseUrl, final Attachment attach)
	{
		switch( attach.getAttachmentType() )
		{
			case IMS:
				return makeEnclosure(baseUrl + URLUtils.urlEncode(attach.getUrl(), false),
					((ImsAttachment) attach).getSize(), false);

			case FILE:
				return makeEnclosure(baseUrl + URLUtils.urlEncode(attach.getUrl(), false),
					((FileAttachment) attach).getSize(), false);

			case LINK:
				return makeEnclosure(attach.getUrl(), 0, true);
		}
		return null;
	}

	private SyndEnclosure makeEnclosure(final String url, final long length, final boolean fullUrl)
	{
		final SyndEnclosure enclosure = new SyndEnclosureImpl();
		enclosure.setUrl(url);
		enclosure.setLength(length);
		String filename = url;
		if( fullUrl )
		{
			try
			{
				filename = new URL(url).getPath();
			}
			catch( MalformedURLException m )
			{
				// ignore
			}
		}
		enclosure.setType(mimeTypeService.getMimeTypeForFilename(filename));
		return enclosure;
	}

	public static class LabelCategory implements SyndCategory
	{
		private final Label label;

		public LabelCategory(Label label)
		{
			this.label = label;
		}

		// Sonar raises a superfluous objection here. By implementing
		// SyndCategory this class DOES implement Cloneable
		@Override
		public Object clone() throws CloneNotSupportedException
		{
			return super.clone(); // NOSONAR
		}

		@Override
		public String getTaxonomyUri()
		{
			return null;
		}

		@Override
		public String getName()
		{
			return label.getText();
		}

		@Override
		public void setTaxonomyUri(String taxonomyUri)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setName(String name)
		{
			throw new UnsupportedOperationException();
		}
	}

	public static class LabelContent implements SyndContent
	{
		private final Label label;

		public LabelContent(Label label)
		{
			this.label = label;
		}

		// Sonar raises a superfluous objection here. By implementing
		// SyndContent this class DOES implement Cloneable
		@Override
		public Object clone() throws CloneNotSupportedException
		{
			return super.clone(); // NOSONAR
		}

		@Override
		public String getMode()
		{
			return null;
		}

		@Override
		public String getType()
		{
			if( label.isHtml() )
			{
				return "text/html";
			}
			return "text/plain";
		}

		@Override
		public String getValue()
		{
			return label.getText();
		}

		@Override
		public void setMode(String mode)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setType(String type)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setValue(String value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void copyFrom(Object obj)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Class<?> getInterface()
		{
			throw new UnsupportedOperationException();
		}
	}
}
