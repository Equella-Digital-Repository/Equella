package com.tle.web.controls.youtube;

import com.google.inject.Singleton;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.searching.VideoPreviewRenderer;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.viewable.ViewableItem;

@Bind
@Singleton
public class YoutubePreviewRenderer implements VideoPreviewRenderer
{
	@Override
	public void preRender(PreRenderContext info)
	{
		// Nothing
	}

	@Override
	public SectionRenderable renderPreview(RenderContext context, Attachment attachment, ViewableItem<?> vitem,
		String mimeType)
	{
		if( supports(mimeType) )
		{
			String videoId = (String) attachment.getData("videoId");

			if( !Check.isEmpty(videoId) )
			{
				String embed;
				String ua = context.getRequest().getHeader("User-Agent");

				if( ua != null && (ua.contains("MSIE 7.0") || ua.contains("MSIE 8.0") || ua.contains("MSIE 9.0")) )
				{
					embed = "embed id=\"ytpreview\" class=\"preview\" style=\"width:320px; height:180px;\" src=\"//www.youtube.com/v/"
						+ videoId
						+ "?version=3\" type=\"application/x-shockwave-flash\" allowscriptaccess=\"always\" "
						+ "allowfullscreen=\"true\"";
				}
				else
				{
					embed = "iframe id=\"ytpreview\" type=\"text/html\" style=\"width:320px; height:180px;\" class=\"preview\" src=\"//www.youtube.com/embed/"
						+ videoId + "\" frameborder=\"0\" allowfullscreen";
				}

				return new TagRenderer(embed, new TagState());
			}
		}

		return null;
	}

	@Override
	public boolean supports(String mimeType)
	{
		if( mimeType.contains(YoutubeUtils.MIME_TYPE) )
		{
			return true;
		}
		return false;
	}
}
