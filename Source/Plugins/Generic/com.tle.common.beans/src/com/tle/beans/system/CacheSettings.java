package com.tle.beans.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dytech.common.xml.TLEXStream;
import com.dytech.common.xml.XMLData;
import com.dytech.common.xml.XMLDataMappings;
import com.dytech.common.xml.mapping.ListMapping;
import com.dytech.common.xml.mapping.NodeMapping;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;
import com.tle.common.property.annotation.PropertyBag;

/**
 * @author Nicholas Read
 */
public class CacheSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1;
	private static TLEXStream xstream = new TLEXStream();

	@Property(key = "cache.enabled")
	private boolean enabled;
	@PropertyBag(key = "cache.groups")
	private PropBagEx groups;

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public Node getGroups()
	{
		if( groups == null )
		{
			return null;
		}
		return (Node) xstream.fromXML(groups, Node.class);
	}

	public void setGroups(Node groups)
	{
		this.groups = xstream.toPropBag(groups, "groups"); //$NON-NLS-1$
	}

	public static class Node implements XMLData
	{
		private static final long serialVersionUID = 1L;

		private static final XMLDataMappings smappings;
		static
		{
			smappings = new XMLDataMappings();
			smappings.addNodeMapping(new NodeMapping("name", "@name"));
			smappings.addNodeMapping(new NodeMapping("id", "@id"));
			smappings.addNodeMapping(new NodeMapping("uuid", "@uuid"));
			smappings.addNodeMapping(new ListMapping("includes", "include", ArrayList.class, Query.class));
			smappings.addNodeMapping(new ListMapping("excludes", "exclude", ArrayList.class, Query.class));
			smappings.addNodeMapping(new ListMapping("nodes", "user", ArrayList.class, Node.class)
			{
				@Override
				public boolean hasValue(Object object)
				{
					return false;
				}
			});
			smappings.addNodeMapping(new ListMapping("nodes", "group", ArrayList.class, Node.class));
		}

		private List<Query> includes;
		private List<Query> excludes;
		private List<Node> nodes;

		private String id;
		private String name;
		private String uuid;

		/**
		 * For serialisation only!
		 */
		public Node()
		{
			//
		}

		public Node(String string, boolean group)
		{
			if( group )
			{
				name = string;
			}
			else
			{
				id = string;
			}
			setUuid(UUID.randomUUID().toString());
		}

		@Override
		public XMLDataMappings getMappings()
		{
			return smappings;
		}

		public boolean isGroup()
		{
			return name != null;
		}

		public boolean isUser()
		{
			return id != null;
		}

		public List<Query> getExcludes()
		{
			if( excludes == null )
			{
				excludes = new ArrayList<Query>();
			}
			return excludes;
		}

		public void setExcludes(List<Query> excludes)
		{
			this.excludes = excludes;
		}

		public List<Query> getIncludes()
		{
			if( includes == null )
			{
				includes = new ArrayList<Query>();
			}
			return includes;
		}

		public void setIncludes(List<Query> includes)
		{
			this.includes = includes;
		}

		public List<Node> getNodes()
		{
			if( nodes == null )
			{
				nodes = new ArrayList<Node>();
			}
			return nodes;
		}

		public void setNodes(List<Node> nodes)
		{
			this.nodes = nodes;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}
	}

	public static class Query implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private long itemdef;
		private String uuid;
		private String script;

		public Query()
		{
			script = Constants.BLANK;
		}

		public Query(long id, String script)
		{
			itemdef = id;
			this.script = script;
		}

		public long getItemdef()
		{
			return itemdef;
		}

		public void setItemdef(long itemdef)
		{
			this.itemdef = itemdef;
		}

		public String getScript()
		{
			return script;
		}

		public void setScript(String script)
		{
			this.script = script;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public String getUuid()
		{
			return uuid;
		}
	}
}
