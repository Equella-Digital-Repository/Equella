/*
 * Created on Jun 6, 2004 For "The Learning Edge"
 */
package com.tle.web.viewitem.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.ssi.SSIExternalResolver;
import org.apache.log4j.Logger;

import com.google.common.io.ByteStreams;
import com.tle.beans.filesystem.FileHandle;
import com.tle.core.services.FileSystemService;

/**
 * @author jmaginnis
 */
public class SSIResolver implements SSIExternalResolver
{
	private static final Logger LOGGER = Logger.getLogger(SSIResolver.class);

	private FileSystemService fsys;
	private FileHandle handle;
	private String extrapath;
	private Map<String, String> vars = new HashMap<String, String>();

	public SSIResolver(FileSystemService fsys, FileHandle handle, String extrapath)
	{
		this.fsys = fsys;
		this.handle = handle;

		int slash = extrapath.lastIndexOf('/');
		if( slash > 0 )
		{
			extrapath = extrapath.substring(0, slash + 1);
		}
		this.extrapath = extrapath;
	}

	@Override
	public void addVariableNames(@SuppressWarnings("rawtypes") Collection arg0)
	{
		LOGGER.info("addVariableNames():" + arg0); //$NON-NLS-1$
	}

	@Override
	public String getVariableValue(String name)
	{
		return vars.get(name);
	}

	@Override
	public void setVariableValue(String name, String value)
	{
		vars.put(name, value);
	}

	@Override
	public Date getCurrentDate()
	{
		return new Date();
	}

	private String getFileName(String fname)
	{
		fname = extrapath + fname;
		// LOGGER.info("File="+fname);
		return fname;
	}

	@Override
	public long getFileSize(String fname, boolean virtual) throws IOException
	{
		fname = getFileName(fname);

		return fsys.fileLength(handle, fname);
	}

	@Override
	public long getFileLastModified(String fname, boolean virtual) throws IOException
	{
		fname = getFileName(fname);

		return fsys.lastModified(handle, fname);
	}

	@Override
	public String getFileText(String fname, boolean virtual) throws IOException
	{
		fname = getFileName(fname);

		int length = (int) fsys.fileLength(handle, fname);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(length);

		try (InputStream in = fsys.read(handle, fname))
		{
			ByteStreams.copy(in, baos);
		}

		return baos.toString("UTF-8"); //$NON-NLS-1$
	}

	@Override
	public void log(String msg, Throwable t)
	{
		LOGGER.info(msg, t);
	}
}
