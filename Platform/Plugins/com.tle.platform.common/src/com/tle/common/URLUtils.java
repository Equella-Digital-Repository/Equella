package com.tle.common;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtils
{
	public static final String CHARSET_ENCODING = "UTF-8"; //$NON-NLS-1$
	private static final Pattern URL_ENCODE_ANCHORS = Pattern.compile("^(.*)%23([^/]*?)$"); //$NON-NLS-1$

	public static URL newURL(String url)
	{
		try
		{
			return new URL(url);
		}
		catch( MalformedURLException mal )
		{
			throw new RuntimeException(url, mal);
		}
	}

	public static URL newURL(String context, String spec)
	{
		return newURL(newURL(context), spec);
	}

	@SuppressWarnings("nls")
	public static URL newURL(URL context, String spec)
	{
		try
		{
			String ctx = context.toString();
			if( !ctx.endsWith("/") )
			{
				ctx += "/";
			}
			return new URL(newURL(ctx), spec);
		}
		catch( MalformedURLException mal )
		{
			throw new RuntimeException(context.toString() + ' ' + spec, mal);
		}
	}

	public static String basicUrlEncode(String url)
	{
		try
		{
			return URLEncoder.encode(url, CHARSET_ENCODING);
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}

	public static String basicUrlDecode(String url)
	{
		try
		{
			return URLDecoder.decode(url, CHARSET_ENCODING);
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Synonymous with basicUrlPathEncode
	 */
	public static String urlEncode(String url)
	{
		return basicUrlPathEncode(url, true);
	}

	public static String urlEncode(String url, boolean preserveAnchor)
	{
		return basicUrlPathEncode(url, preserveAnchor);
	}

	private static String basicUrlPathEncode(String url, boolean preserveAnchor)
	{
		String encodedUrl = basicUrlEncode(url);

		// Ensure forward slashes are still slashes
		encodedUrl = encodedUrl.replaceAll("%2F", "/"); //$NON-NLS-1$ //$NON-NLS-2$

		// Ensure that pluses are changed into the correct %20
		encodedUrl = encodedUrl.replaceAll("\\+", "%20"); //$NON-NLS-1$ //$NON-NLS-2$

		if( preserveAnchor )
		{
			// Ensure that the last # after any forward slash is reestablished,
			// as
			// it is the URL anchor
			Matcher m = URL_ENCODE_ANCHORS.matcher(encodedUrl);
			if( m.matches() )
			{
				encodedUrl = m.group(1) + '#' + m.group(2);
			}
		}

		return encodedUrl;
	}

	/**
	 * @param url
	 * @return true if the url is of the form http://dddsdasd etc.
	 */
	@SuppressWarnings("unused")
	public static boolean isAbsoluteUrl(String url)
	{
		try
		{
			new URL(url);
		}
		catch( MalformedURLException mal )
		{
			return false;
		}
		return true;
	}

	@SuppressWarnings("nls")
	public static Map<String, Set<String>> parseQueryString(String queryString)
	{
		final Map<String, Set<String>> qs = new HashMap<String, Set<String>>();
		if( queryString != null )
		{
			final String[] params = queryString.split("&");
			for( String param : params )
			{
				final String[] nameVal = param.split("=");
				if( nameVal.length == 2 )
				{
					final String key = nameVal[0];
					Set<String> current = qs.get(key);
					if( current == null )
					{
						current = new HashSet<String>();
						qs.put(key, current);
					}
					current.add(nameVal[1]);
				}
			}
		}
		return qs;
	}

	/**
	 * More convenient to extract the values out.
	 * 
	 * @param queryString
	 * @param singleValues
	 * @return
	 */
	@SuppressWarnings("nls")
	public static Map<String, String> parseQueryString(String queryString, boolean singleValues)
	{
		final Map<String, String> qs = new HashMap<String, String>();
		if( queryString != null )
		{
			final String[] params = queryString.split("&");
			for( String param : params )
			{
				final String[] nameVal = param.split("=");
				if( nameVal.length == 2 )
				{
					qs.put(nameVal[0], nameVal[1]);
				}
			}
		}
		return qs;
	}

	public static String appendQueryString(String url, String qs)
	{
		if( url.indexOf('?') > 0 )
		{
			return url + '&' + qs;
		}
		return url + '?' + qs;
	}

	/**
	 * @param map One of: Map<?,Object[]> Map<?,Collection<Object>>
	 *            Map<?,Object>
	 * @return
	 */
	public static String getParameterString(Map<?, ?> map)
	{
		List<NameValue> nvs = new ArrayList<NameValue>();
		for( Map.Entry<?, ?> entry : map.entrySet() )
		{
			Collection<?> values = Collections.emptyList();

			Object obj = entry.getValue();
			if( obj != null )
			{
				if( obj instanceof Object[] )
				{
					values = Arrays.asList((Object[]) obj);
				}
				else if( obj instanceof Collection<?> )
				{
					values = Collection.class.cast(obj);
				}
				else
				{
					values = Collections.singleton(obj);
				}
			}

			for( Object name2 : values )
			{
				String element = name2.toString();
				nvs.add(new NameValue(entry.getKey().toString(), element));
			}
		}

		StringBuilder parameters = new StringBuilder();

		boolean first = true;
		for( NameValue nv : nvs )
		{
			if( !first )
			{
				parameters.append('&');
			}
			else
			{
				first = false;
			}

			parameters.append(URLUtils.basicUrlEncode(nv.getName()));
			parameters.append('=');
			parameters.append(URLUtils.basicUrlEncode(nv.getValue()));
		}

		return parameters.toString();
	}

}
