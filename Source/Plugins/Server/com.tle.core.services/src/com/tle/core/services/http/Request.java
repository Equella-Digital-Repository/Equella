package com.tle.core.services.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;

import com.dytech.edge.common.Constants;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.NameValue;

/**
 * To be used with HttpService
 * 
 * @author Aaron
 */
public class Request
{
	public enum Method
	{
		GET, POST, PUT, DELETE, HEAD, OPTIONS, OTHER;

		public static Method fromString(String method)
		{
			try
			{
				return Method.valueOf(method.toUpperCase());
			}
			catch( Exception e )
			{
				return Method.OTHER;
			}
		}
	}

	private final String url;
	private Method method = Method.GET;
	private final List<NameValue> params = Lists.newArrayList();
	private final List<NameValue> headers = Lists.newArrayList();
	// Currently doesn't allow streaming or binary bodies
	private String body;
	private String mimeType;
	private String charset;

	public Request(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}

	public Method getMethod()
	{
		return method;
	}

	public Request setMethod(Method method)
	{
		this.method = method;
		return this;
	}

	public List<NameValue> getParams()
	{
		return params;
	}

	public List<NameValue> getHeaders()
	{
		return headers;
	}

	public Request addParameter(String name, String value)
	{
		params.add(new NameValue(name, value));
		return this;
	}

	public Request addParameter(String name, int value)
	{
		params.add(new NameValue(name, Integer.toString(value)));
		return this;
	}

	public Request addHeader(String name, String value)
	{
		headers.add(new NameValue(name, value));
		return this;
	}

	public Request setAccept(String accept)
	{
		headers.add(new NameValue("Accept", accept));
		return this;
	}

	public String getBody()
	{
		return body;
	}

	public void setBody(String body)
	{
		this.body = body;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

	public String getCharset()
	{
		return charset;
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	public void setHtmlForm(FormParameters params)
	{
		setHtmlForm(params.getParameters());
	}

	public void setHtmlForm(List<NameValue> params)
	{
		setMimeType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
		setCharset(Constants.UTF8);
		setBody(URLEncodedUtils.format(Lists.transform(params, new ParamConverter()), Constants.UTF8));
	}

	private static final class ParamConverter implements Function<NameValue, NameValuePair>
	{
		@Override
		public NameValuePair apply(NameValue nv)
		{
			return new BasicNameValuePair(nv.getName(), nv.getValue());
		}
	}

	public static class FormParameters
	{
		private List<NameValue> parameters = new ArrayList<>();

		private List<NameValue> getParameters()
		{
			return parameters;
		}

		public FormParameters addParameter(String name, String value)
		{
			parameters.add(new NameValue(name, value));
			return this;
		}

		public FormParameters addParameter(String name, int value)
		{
			parameters.add(new NameValue(name, Integer.toString(value)));
			return this;
		}
	}
}
