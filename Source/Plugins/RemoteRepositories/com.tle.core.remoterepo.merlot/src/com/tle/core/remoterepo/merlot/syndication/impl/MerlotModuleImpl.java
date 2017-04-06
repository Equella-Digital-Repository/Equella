package com.tle.core.remoterepo.merlot.syndication.impl;

import com.sun.syndication.feed.module.ModuleImpl;
import com.tle.core.remoterepo.merlot.syndication.MerlotModule;

/**
 * @author aholland
 */
@SuppressWarnings("serial")
public class MerlotModuleImpl extends ModuleImpl implements MerlotModule
{
	private String title;
	private String url;

	protected MerlotModuleImpl()
	{
		super(MerlotModule.class, MerlotModule.URI);
	}

	@Override
	public void copyFrom(Object obj)
	{
		MerlotModule other = (MerlotModule) obj;
		title = other.getTitle();
		url = other.getUrl();
	}

	@Override
	public Class<MerlotModule> getInterface()
	{
		return MerlotModule.class;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public void setTitle(String title)
	{
		this.title = title;
	}

	@Override
	public String getUrl()
	{
		return url;
	}

	@Override
	public void setUrl(String url)
	{
		this.url = url;
	}
}
