package com.tle.core.qti.parse.v1x;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A response contains a list of available options (elements) that a user can
 * select for a question. The type can be fib (Fill-In-Blank) or choice
 * (multiple or single cardinality)
 * 
 * @author will
 */
public class QTIResponse implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String id;
	private String type;
	private boolean lookupId;
	private QTIMaterial display;
	private String cardinality;
	private Map<String, QTIResponseElement> elements = new LinkedHashMap<String, QTIResponseElement>();

	public QTIResponse()
	{

	}

	public QTIResponse(String id, String type, QTIMaterial display, String cardinality, boolean lookupId)
	{
		this.id = id;
		this.type = type;
		this.display = display;
		this.cardinality = cardinality;
		this.lookupId = lookupId;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public Map<String, QTIResponseElement> getElements()
	{
		return elements;
	}

	public void setElements(Map<String, QTIResponseElement> elements)
	{
		this.elements = elements;
	}

	public void putElement(String id, QTIResponseElement ele)
	{
		elements.put(id, ele);
	}

	public void setDisplay(QTIMaterial display)
	{
		this.display = display;
	}

	public QTIMaterial getDisplay()
	{
		return display;
	}

	public void setCardinality(String cardinality)
	{
		this.cardinality = cardinality;
	}

	public String getCardinality()
	{
		return cardinality;
	}

	public void setLookupId(boolean lookupId)
	{
		this.lookupId = lookupId;
	}

	public boolean isLookupId()
	{
		return lookupId;
	}
}