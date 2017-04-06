package com.tle.core.util.ims.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dytech.common.xml.XMLDataMappings;
import com.dytech.common.xml.mapping.ListMapping;
import com.dytech.common.xml.mapping.NodeMapping;
import com.tle.common.Utils;

public class IMSItem extends IMSChild
{
	private static final long serialVersionUID = 1L;

	private static XMLDataMappings mappings;

	private String identifier;
	private String identifierRef;

	private List<IMSItem> items = new ArrayList<IMSItem>();
	private String visibleVal;

	public void addToXMLString(StringBuilder sbuf, Map<String, IMSResource> resmap)
	{
		sbuf.append("<wrapper type=\"3\" isvisible=\""); //$NON-NLS-1$
		sbuf.append(isVisible());
		sbuf.append("\">"); //$NON-NLS-1$
		sbuf.append(Utils.ent(getTitle()));

		IMSResource imsres = resmap.get(identifierRef);
		if( imsres != null )
		{
			imsres.addToXMLString(sbuf);
		}

		for( IMSItem item : items )
		{
			item.addToXMLString(sbuf, resmap);
		}

		sbuf.append("</wrapper>"); //$NON-NLS-1$
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String ident)
	{
		this.identifier = ident;
	}

	public String getIdentifierRef()
	{
		return identifierRef;
	}

	public void setIdentifierRef(String identifierref)
	{
		this.identifierRef = identifierref;
	}

	public boolean isVisible()
	{
		return visibleVal == null ? true : !(visibleVal.equals("false") || visibleVal.equals("0")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void setVisible(boolean visible)
	{
		this.visibleVal = visible ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public List<IMSItem> getItems()
	{
		return items;
	}

	public void setItems(List<IMSItem> items)
	{
		this.items = items;
	}

	@Override
	@SuppressWarnings("nls")
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings(super.getMappings());
			// Presumably the intent is to return the implementation class, so
			// we ignore Sonar's "loose coupling" warning
			mappings.addNodeMapping(new ListMapping("items", "item", ArrayList.class, IMSItem.class)); // NOSONAR
			mappings.addNodeMapping(new NodeMapping("identifierRef", "@identifierref"));
			mappings.addNodeMapping(new NodeMapping("identifier", "@identifier"));
			mappings.addNodeMapping(new NodeMapping("visibleVal", "@isvisible"));
		}
		return mappings;
	}

	@Override
	public String getTitle()
	{
		String actTitle = super.getTitle();

		// Try using the resource filename if no title.
		if( actTitle.length() == 0 )
		{
			IMSResource res = getRootManifest().getResourceMap().get(identifierRef);
			if( res != null )
			{
				actTitle = res.getHref();
				while( actTitle.endsWith("/") ) //$NON-NLS-1$
				{
					actTitle = actTitle.substring(0, actTitle.length() - 1);
				}

				int lastSlash = actTitle.lastIndexOf('/');
				if( lastSlash >= 0 && lastSlash < actTitle.length() - 1 )
				{
					actTitle = actTitle.substring(lastSlash + 1);
				}
			}
		}

		if( actTitle.length() == 0 )
		{
			actTitle = "Unnamed"; //$NON-NLS-1$
		}

		return actTitle;
	}
}