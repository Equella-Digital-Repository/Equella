package com.tle.core.institution.importhandler;

import com.thoughtworks.xstream.XStream;
import com.tle.core.institution.XmlHelper;

public abstract class AbstractImportHandler<T> implements ImportHandler<T>
{
	private XStream xstream;
	protected final XmlHelper xmlHelper;

	public AbstractImportHandler(XmlHelper xmlHelper, XStream xstream)
	{
		this.xmlHelper = xmlHelper;
		this.xstream = xstream;
	}

	protected synchronized XStream getXStream()
	{
		if( xstream == null )
		{
			xstream = xmlHelper.createXStream();
		}
		return xstream;
	}
}
