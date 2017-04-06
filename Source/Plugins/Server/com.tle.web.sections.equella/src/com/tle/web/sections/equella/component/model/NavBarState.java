package com.tle.web.sections.equella.component.model;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.web.sections.equella.component.NavBarBuilder;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class NavBarState extends HtmlComponentState
{
	private Link titleLink;
	private List<NavBarElement> left;
	private List<NavBarElement> right;
	private List<NavBarElement> middle;
	private final RendererFactory rendererFactory;

	public NavBarState(RendererFactory rendererFactory)
	{
		this.rendererFactory = rendererFactory;
	}

	public Link getTitleLink()
	{
		return titleLink;
	}

	public void setTitleLink(Link titleLink)
	{
		this.titleLink = titleLink;
	}

	public NavBarBuilder buildLeft()
	{
		if( left == null )
		{
			left = Lists.newArrayList();
		}
		return new NavBarBuilder(left, rendererFactory);
	}

	public NavBarBuilder buildRight()
	{
		if( right == null )
		{
			right = Lists.newArrayList();
		}
		return new NavBarBuilder(right, rendererFactory);
	}

	public NavBarBuilder buildMiddle()
	{
		if( middle == null )
		{
			middle = Lists.newArrayList();
		}
		return new NavBarBuilder(middle, rendererFactory);
	}

	public List<NavBarElement> getLeft()
	{
		return left;
	}

	public void setLeft(List<NavBarElement> left)
	{
		this.left = left;
	}

	public List<NavBarElement> getRight()
	{
		return right;
	}

	public void setRight(List<NavBarElement> right)
	{
		this.right = right;
	}

	public List<NavBarElement> getMiddle()
	{
		return middle;
	}

	public void setMiddle(List<NavBarElement> middle)
	{
		this.middle = middle;
	}

	public static class NavBarElement
	{
		private final String cssClass;
		private final Object renderable;

		public NavBarElement(String cssClass, Object renderable)
		{
			this.cssClass = cssClass;
			this.renderable = renderable;
		}

		public String getCssClass()
		{
			return cssClass;
		}

		public Object getRenderable()
		{
			return renderable;
		}
	}
}
