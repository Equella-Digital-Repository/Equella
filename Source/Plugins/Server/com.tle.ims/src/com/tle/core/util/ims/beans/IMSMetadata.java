/*
 * Created on Jun 14, 2005
 */
package com.tle.core.util.ims.beans;

import java.util.ArrayList;
import java.util.List;

import com.dytech.common.xml.XMLData;
import com.dytech.common.xml.XMLDataMappings;
import com.dytech.common.xml.mapping.DataMapping;
import com.dytech.common.xml.mapping.ListMapping;
import com.dytech.common.xml.mapping.NodeMapping;

/**
 * @author jmaginnis
 */
public class IMSMetadata implements XMLData
{
	private static final long serialVersionUID = 1L;

	private static XMLDataMappings mappings;

	private boolean activityplan;
	private String planTitle;
	private String title;
	private String keywords;
	private String description;
	private List<IMSActivity> activities = new ArrayList<IMSActivity>();
	private IMSCustomData data;

	public boolean isActivityplan()
	{
		return activityplan;
	}

	public String getDescription()
	{
		return description;
	}

	public String getKeywords()
	{
		return keywords;
	}

	public String getPlanTitle()
	{
		return planTitle;
	}

	public String getTitle()
	{
		return title;
	}

	public List<IMSActivity> getActivities()
	{
		return activities;
	}

	public void setActivityplan(boolean activityplan)
	{
		this.activityplan = activityplan;
	}

	public void setPlanTitle(String planTitle)
	{
		this.planTitle = planTitle;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setKeywords(String keywords)
	{
		this.keywords = keywords;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setActivities(List<IMSActivity> activities)
	{
		this.activities = activities;
	}

	public IMSCustomData getData()
	{
		return data;
	}

	public void setData(IMSCustomData data)
	{
		this.data = data;
	}

	@Override
	@SuppressWarnings("nls")
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings();
			mappings.addNodeMapping(new NodeMapping("activityplan", "lom/general/activityplan"));
			mappings.addNodeMapping(new NodeMapping("planTitle", "lom/general/plantitle/langstring"));
			mappings.addNodeMapping(new NodeMapping("title", "lom/general/title/langstring"));
			mappings.addNodeMapping(new NodeMapping("keywords", "lom/general/keyword/langstring"));
			mappings.addNodeMapping(new NodeMapping("description", "lom/general/description/langstring"));
			// Presumably the intent is to return the implementation class, so
			// we ignore Sonar's "loose coupling" warning
			mappings.addNodeMapping(new ListMapping("activities", "lom/general/activities/activity", ArrayList.class, // NOSONAR
				IMSActivity.class));
			mappings.addNodeMapping(new DataMapping("data", "data", IMSCustomData.class));
		}
		return mappings;
	}
}
