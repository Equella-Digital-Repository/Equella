package com.tle.core.remoterepo.parser.mods.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.util.XmlDocument;
import com.tle.core.remoterepo.parser.mods.ModsRecord;

/**
 * A strict implementation of the MODS 3.4 format, massaged into the
 * Z3950FullRecord interface
 * 
 * @author aholland
 */
@SuppressWarnings("nls")
public class Mods34Record implements ModsRecord
{
	protected final XmlDocument xml;
	protected final Node context;

	protected ModsTitleInfo titleInfo;
	protected ModsNames names;
	protected String typeOfResource;
	protected String genre;
	protected ModsPart originInfo;
	protected ModsPart language;
	protected ModsPhysicalDescription physicalDescription;
	protected String abstrakt;
	protected String tableOfContents;
	protected String targetAudience;
	protected ModsNotes notes;
	protected ModsPart subject;
	protected String classification;
	protected Mods34Record relatedItem;
	protected Map<String, String> identifiers = new HashMap<String, String>();
	protected ModsPart location;
	protected String accessCondition;
	protected ModsPart part;
	protected String extension;
	protected ModsPart recordInfo;

	public Mods34Record(PropBagEx bag)
	{
		this.xml = new XmlDocument(bag.toString());
		this.context = xml.node("mods");
	}

	public void setIdentifier(String type, String value)
	{
		identifiers.put(type, value);
	}

	@Override
	public String getTitle()
	{
		if( titleInfo == null )
		{
			titleInfo = new ModsTitleInfo(xml, context);
			return titleInfo.getTitle();
		}
		return null;
	}

	@Override
	public String getIsbn()
	{
		return identifiers.get("isbn");
	}

	@Override
	public String getIssn()
	{
		return identifiers.get("issn");
	}

	@Override
	public String getLccn()
	{
		return identifiers.get("lccn");
	}

	@Override
	public String getUri()
	{
		return identifiers.get("uri");
	}

	@Override
	public String getUrl()
	{
		return getUri();
	}

	@Override
	public String getDescription()
	{
		if( abstrakt == null )
		{
			abstrakt = xml.nodeValue("abstract", context);
		}
		if( !Check.isEmpty(abstrakt) )
		{
			return abstrakt;
		}
		if( titleInfo == null )
		{
			titleInfo = new ModsTitleInfo(xml, context);
			return titleInfo.getSubTitle();
		}
		return null;
	}

	@Override
	public Collection<String> getAuthors()
	{
		if( names == null )
		{
			names = new ModsNames(xml, context);
		}
		return names.getNames();
	}

	@Override
	public PropBagEx getXml()
	{
		return new PropBagEx(xml.toString());
	}

	// MODS

	@Override
	public Set<String> getNotes()
	{
		if( notes == null )
		{
			notes = new ModsNotes(xml, context);
		}
		return notes.getNotes();
	}

	@Override
	public String getPhysicalDescription()
	{
		if( physicalDescription == null )
		{
			physicalDescription = new ModsPhysicalDescription(xml, context);
		}
		return physicalDescription.getExtent();
	}

	@Override
	public String getTypeOfResource()
	{
		return xml.nodeValue("typeOfResource", context);
	}

	@Override
	public String getType()
	{
		return "MODS";
	}
}
