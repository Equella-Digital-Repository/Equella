/*
 * Created on Oct 19, 2005
 */
package com.tle.web.service.oai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import ORG.oclc.oai.server.catalog.RecordFactory;
import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.crosswalk.CrosswalkItem;
import ORG.oclc.oai.server.crosswalk.Crosswalks;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import ORG.oclc.oai.server.verb.ServerVerb;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.SchemaTransform;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.core.schema.SchemaService;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.item.ItemService;
import com.tle.core.util.ItemHelper;

public class XMLRecordFactory extends RecordFactory
{
	private static final Logger LOGGER = Logger.getLogger(XMLRecordFactory.class);

	@Inject
	private ItemService itemService;
	@Inject
	private SchemaService schemaService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private UrlService urlService;
	@Inject
	private ItemHelper itemHelper;

	/*
	 * DO NOT REMOVE ARGUMENT! Called by Reflection - very necessary.
	 */
	public XMLRecordFactory(Properties properties // NOSONAR
	)
	{
		// Avoid trying to load Crosswalks from properties as ours are dynamic
		super(new HashMap<Object, Object>());
	}

	@Override
	public Crosswalks getCrosswalks()
	{
		// TODO: We should do some caching here
		return new Crosswalks(getSchemaTypes());
	}

	@Override
	public String fromOAIIdentifier(String identifier)
	{
		return identifier;
	}

	@Override
	public String getOAIIdentifier(Object record)
	{
		return OAIUtils.getInstance(urlService, configService).getIdentifier(getItem(record).getItemId());
	}

	@Override
	public String getDatestamp(Object record)
	{
		return ServerVerb.createResponseDate(getItem(record).getDateModified());
	}

	@Override
	public boolean isDeleted(Object record)
	{
		return getItem(record).getStatus().equals(ItemStatus.DELETED);
	}

	@Override
	public Iterator<String> getSetSpecs(Object record)
	{
		return null;
	}

	@Override
	public String quickCreate(Object record, String schemaLocation, String metadataPrefix)
	{
		return null;
	}

	@Override
	public Iterator<?> getAbouts(Object record)
	{
		return null;
	}

	private Item getItem(Object record)
	{
		return (Item) record;
	}

	private Map<String, CrosswalkItem> getSchemaTypes()
	{
		Map<String, CrosswalkItem> rv = new HashMap<String, CrosswalkItem>();
		for( String metadataPrefix : schemaService.getExportSchemaTypes() )
		{
			metadataPrefix = metadataPrefix.toLowerCase();
			if( !rv.containsKey(metadataPrefix) )
			{
				// TODO: Get some better values for createCrosswalk() from the
				// schema so DC is done correctly, etc...
				rv.put(metadataPrefix, createCrossWalkItem(metadataPrefix, metadataPrefix, metadataPrefix));
			}
		}
		return rv;
	}

	private CrosswalkItem createCrossWalkItem(String metadataPrefix, String schema, String metadataNamespace)
	{
		return new CrosswalkItem(metadataPrefix, schema, metadataNamespace, new TLECrossWalk(metadataPrefix,
			metadataNamespace + ' ' + schema, null, null, null));
	}

	private class TLECrossWalk extends Crosswalk
	{
		private final String metadataPrefix;

		public TLECrossWalk(String metadataPrefix, String schemaLocation, String contentType, String docType,
			String encoding)
		{
			super(schemaLocation, contentType, docType, encoding);
			this.metadataPrefix = metadataPrefix;
		}

		@Override
		public boolean isAvailableFor(Object nativeItem)
		{
			Schema schema = getSchema((Item) nativeItem);
			for( SchemaTransform st : schema.getExportTransforms() )
			{
				if( metadataPrefix.equalsIgnoreCase(st.getType()) )
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public String createMetadata(Object nativeItem) throws CannotDisseminateFormatException
		{
			Item item = (Item) nativeItem;
			try
			{
				ItemPack pack = new ItemPack();
				pack.setItem(item);
				pack.setXml(itemService.getItemXmlPropBag(item));
				PropBagEx xml = itemHelper.convertToXml(pack);

				String s = schemaService.transformForExport(getSchema(item).getId(), metadataPrefix, xml, true);
				if( s != null )
				{
					return s;
				}
			}
			catch( Exception ex )
			{
				LOGGER.error("Error transforming", ex);
			}

			throw new CannotDisseminateFormatException(metadataPrefix);
		}

		private Schema getSchema(Item xml)
		{
			return xml.getItemDefinition().getSchema();
		}

	}
}