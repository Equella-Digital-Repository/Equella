package com.tle.core.institution.importhandler;

import java.util.Iterator;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.institution.XmlHelper;

public class SingleFileImportHandler<T> extends AbstractImportHandler<T>
{
	private final List<T> nodes;

	public SingleFileImportHandler(SubTemporaryFile folder, String path, XmlHelper xmlHelper, XStream xstream)
	{
		super(xmlHelper, xstream);
		this.nodes = xmlHelper.readXmlFile(folder, path, getXStream());
	}

	@Override
	public int getNodeCount()
	{
		return nodes.size();
	}

	@Override
	public Iterator<T> iterateNodes()
	{
		return nodes.iterator();
	}
}
