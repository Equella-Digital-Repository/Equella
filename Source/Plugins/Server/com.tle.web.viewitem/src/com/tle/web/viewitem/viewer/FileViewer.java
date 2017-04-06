package com.tle.web.viewitem.viewer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.utils.TokenModifier;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.viewurl.ResourceViewerConfig;
import com.tle.web.viewurl.ResourceViewerConfigDialog;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
public class FileViewer extends AbstractResourceViewer
{
	@Inject
	private UserService userService;
	@Inject
	private ComponentFactory componentFactory;

	@Override
	public String getViewerId()
	{
		return MimeTypeConstants.VAL_DEFAULT_VIEWERID;
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return null;
	}

	@Override
	public Bookmark createStreamUrl(SectionInfo info, ViewableResource resource)
	{
		boolean appendToken = isAppendToken(resource);
		if( appendToken || resource.isExternalResource() )
		{
			return createViewItemUrl(resource, appendToken);
		}
		return resource.createCanonicalUrl();
	}

	private boolean isAppendToken(ViewableResource resource)
	{
		ResourceViewerConfig config = getViewerConfig(resource);
		return (config != null && Boolean.TRUE.equals(config.getAttr().get(
			MimeTypeConstants.KEY_VIEWER_CONFIG_APPENDTOKEN)));
	}

	private ViewItemUrl createViewItemUrl(ViewableResource resource, boolean addToken)
	{
		ViewItemUrl viewerUrl = resource.createDefaultViewerUrl();
		viewerUrl.setViewer(getViewerId());
		if( addToken )
		{
			viewerUrl.add(new TokenModifier(userService));
		}
		return viewerUrl;
	}

	@Override
	public ViewItemUrl createViewItemUrl(SectionInfo info, ViewableResource resource)
	{
		return createViewItemUrl(resource, isAppendToken(resource));
	}

	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		FileViewerConfigDialog cd = componentFactory.createComponent(parentId, "fcd", tree, //$NON-NLS-1$
			FileViewerConfigDialog.class, true);
		cd.setTemplate(dialogTemplate);
		return cd;
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return true;
	}
}
