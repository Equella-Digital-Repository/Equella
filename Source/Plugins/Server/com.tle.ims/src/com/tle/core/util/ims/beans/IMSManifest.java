package com.tle.core.util.ims.beans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dytech.common.xml.XMLDataMappings;
import com.dytech.common.xml.mapping.DataMapping;
import com.dytech.common.xml.mapping.ListMapping;
import com.tle.common.Utils;

public class IMSManifest extends IMSChild
{
	private static final long serialVersionUID = 1L;

	private List<IMSManifest> subManifests = new ArrayList<IMSManifest>();
	private List<IMSOrganisation> organisations = new ArrayList<IMSOrganisation>();
	private List<IMSResource> resources = new ArrayList<IMSResource>();
	private IMSMetadata metadata;
	private Map<String, IMSResource> resourceMap;
	private static XMLDataMappings mappings;

	public String toXMLString()
	{
		StringBuilder sbuf = new StringBuilder();
		addToXMLString(sbuf);
		return sbuf.toString();
	}

	public Map<String, IMSResource> getResourceMap()
	{
		if( resourceMap == null )
		{
			resourceMap = new LinkedHashMap<String, IMSResource>();
			for( IMSResource res : resources )
			{
				resourceMap.put(res.getIdentifier(), res);
			}
		}
		return resourceMap;
	}

	public void addToXMLString(StringBuilder sbuf)
	{
		sbuf.append("<wrapper type=\"2\">");
		sbuf.append(Utils.ent(getTitle()));

		// Organisations:
		for( IMSOrganisation org : organisations )
		{
			org.addToXMLString(sbuf, getResourceMap());
		}

		// Files:
		// This mustn't have duplicate names,this can happen since multiple
		// resources can point to the same files but this reduced form will
		// liat all files from all resources in a single list.
		// see Jira Defect TLE-924 :
		// http://apps.dytech.com.au/jira/browse/TLE-924

		Set<IMSFileWrapper> files = new HashSet<IMSFileWrapper>(); // this won't
																	// allow
																	// duplicates
																	// of files

		for( IMSResource imsres : resources )
		{
			files.addAll(imsres.getFiles());
		}

		for( IMSFileWrapper file : files )
		{
			file.addToXMLString(sbuf);
		}

		// Submanifests:
		for( IMSManifest mans : subManifests )
		{
			mans.addToXMLString(sbuf);
		}

		sbuf.append("</wrapper>");
	}

	public List<IMSResource> getAllResources()
	{
		List<IMSResource> allmap = new ArrayList<IMSResource>(resources);

		for( IMSManifest man : subManifests )
		{
			allmap.addAll(man.getAllResources());
		}

		return allmap;
	}

	/**
	 * @return Returns the organisations.
	 */
	public List<IMSOrganisation> getOrganisations()
	{
		return organisations;
	}

	/**
	 * @param organisations The organisations to set.
	 */
	public void setOrganisations(List<IMSOrganisation> organisations)
	{
		this.organisations = organisations;
	}

	/**
	 * @return Returns the resources.
	 */
	public List<IMSResource> getResources()
	{
		return resources;
	}

	/**
	 * @param resources The resources to set.
	 */
	public void setResources(List<IMSResource> resources)
	{
		this.resources = resources;
	}

	/**
	 * @return Returns the subManifests.
	 */
	public List<IMSManifest> getSubManifests()
	{
		return subManifests;
	}

	/**
	 * @param subManifests The subManifests to set.
	 */
	public void setSubManifests(List<IMSManifest> subManifests)
	{
		this.subManifests = subManifests;
	}

	public IMSMetadata getMetadata()
	{
		return metadata;
	}

	public void setMetadata(IMSMetadata metadata)
	{
		this.metadata = metadata;
	}

	@Override
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings(super.getMappings());
			mappings.addNodeMapping(new ListMapping("subManifests", "manifest", ArrayList.class, IMSManifest.class));
			mappings.addNodeMapping(new ListMapping("resources", "resources/resource", ArrayList.class,
				IMSResource.class));
			mappings.addNodeMapping(new ListMapping("organisations", "organizations/organization", ArrayList.class,
				IMSOrganisation.class));
			mappings.addNodeMapping(new DataMapping("metadata", "metadata", IMSMetadata.class));
		}
		return mappings;
	}
}