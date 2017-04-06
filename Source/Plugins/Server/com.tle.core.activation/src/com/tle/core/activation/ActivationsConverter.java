package com.tle.core.activation;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.common.io.UnicodeReader;
import com.google.common.base.Throwables;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.EntityInitialiserCallback;
import com.tle.core.institution.convert.ItemConverter.ItemConverterInfo;
import com.tle.core.institution.convert.ItemConverter.ItemExtrasConverter;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.InitialiserService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ActivationsConverter implements ItemExtrasConverter
{
	public static final String ACTIVATIONS_XML = "activations.xml";

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ActivateRequestDao requestDao;
	@Inject
	private InitialiserService initialiserService;

	@Override
	public void exportExtras(ItemConverterInfo info, XStream xstream, SubTemporaryFile extrasFolder)
	{
		Item item = info.getItem();
		List<ActivateRequest> allRequests = requestDao.getAllRequests(item);
		allRequests = initialiserService.initialise(allRequests, new EntityInitialiserCallback());
		if( !allRequests.isEmpty() )
		{
			for( ActivateRequest activateRequest : allRequests )
			{
				activateRequest.setItem(null);
			}
			String xml = xstream.toXML(allRequests);
			try
			{
				fileSystemService.write(extrasFolder, ACTIVATIONS_XML, new StringReader(xml), false);
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void importExtras(ItemConverterInfo info, XStream xstream, SubTemporaryFile extrasFolder)
	{
		Item item = info.getItem();
		if( fileSystemService.fileExists(extrasFolder, ACTIVATIONS_XML) )
		{
			try (Reader reader = new UnicodeReader(fileSystemService.read(extrasFolder, ACTIVATIONS_XML), "UTF-8"))
			{
				List<ActivateRequest> requests = (List<ActivateRequest>) xstream.fromXML(reader);
				for( ActivateRequest request : requests )
				{
					request.setItem(item);
					request.setId(0);
					requestDao.saveAny(request);
				}
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}
	}
}
