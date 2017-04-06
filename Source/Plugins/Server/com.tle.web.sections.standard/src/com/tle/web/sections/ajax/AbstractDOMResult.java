package com.tle.web.sections.ajax;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractDOMResult
{
	private String script;
	private Collection<String> css;
	private Collection<String> js;
	private Map<String, Object> formParams;

	public AbstractDOMResult()
	{
		// nothing
	}

	public AbstractDOMResult(AbstractDOMResult result)
	{
		setCss(result.getCss());
		setJs(result.getJs());
		setScript(result.getScript());
		setFormParams(result.getFormParams());
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public Collection<String> getCss()
	{
		return css;
	}

	public void setCss(Collection<String> css)
	{
		this.css = css;
	}

	public Collection<String> getJs()
	{
		return js;
	}

	public void setJs(Collection<String> js)
	{
		this.js = js;
	}

	public Map<String, Object> getFormParams()
	{
		return formParams;
	}

	public void setFormParams(Map<String, Object> formParams)
	{
		this.formParams = formParams;
	}
}
