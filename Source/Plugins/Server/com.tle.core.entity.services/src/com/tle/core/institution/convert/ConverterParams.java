/*
 * Created on 24/05/2006
 */
package com.tle.core.institution.convert;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tle.core.progress.ListProgressCallback;
import com.tle.core.progress.ListProgressCallback.MessageCallback;

public class ConverterParams
{
	public static final String NO_ITEMS = "NO_ITEMS"; //$NON-NLS-1$
	public static final String NO_ITEMSATTACHMENTS = "NO_ITEMSATTACHMENTS"; //$NON-NLS-1$
	public static final String NO_AUDITLOGS = "NO_AUDITLOGS"; //$NON-NLS-1$

	private final Map<Long, Long> entities;
	private final Map<Long, Long> items;
	private final Map<Long, Long> hierarchies;
	private final Map<Object, Object> attributes;
	private InstitutionInfo instituionInfo;
	private ListProgressCallback callback;
	private Set<String> flags;
	private URL oldServerURL;
	private URL currentServerURL;
	private final String versionString;
	private final String branchString;

	public ConverterParams(InstitutionInfo instInfo)
	{
		// these maps could now be used concurrently (see
		// ItemConverter.importIt)
		this.entities = new ConcurrentHashMap<Long, Long>();
		this.items = new ConcurrentHashMap<Long, Long>();
		this.hierarchies = new ConcurrentHashMap<Long, Long>();
		this.attributes = new ConcurrentHashMap<Object, Object>();
		this.instituionInfo = instInfo;

		// Extract the legacy version number from the import data if it exists.
		Pattern p = Pattern.compile("^(\\d+\\.\\d+) \\[(.+) r(\\d+)\\]$"); //$NON-NLS-1$
		Matcher matcher = p.matcher(instInfo.getBuildVersion());
		matcher = p.matcher(instInfo.getBuildVersion());
		if( matcher.matches() )
		{
			versionString = matcher.group(1) + '.' + matcher.group(3);
			branchString = matcher.group(2);
		}
		else
		{
			versionString = null;
			branchString = null;
		}

		flags = instInfo.getFlags();
	}

	public ConverterParams(InstitutionInfo instInfo, ListProgressCallback callback)
	{
		this(instInfo);
		this.callback = callback;
	}

	public synchronized void setAttribute(Object key, Object value)
	{
		attributes.put(key, value);
	}

	// Sonar likes the get(...) to be synchronised if the set(...) is
	@SuppressWarnings("unchecked")
	public synchronized <T> T getAttribute(Object key)
	{
		return (T) attributes.get(key);
	}

	public String getVersionString()
	{
		return versionString;
	}

	public String getBranchString()
	{
		return branchString;
	}

	public ListProgressCallback getCallback()
	{
		return callback;
	}

	public Map<Long, Long> getOld2new()
	{
		return entities;
	}

	public void addError(String error)
	{
		callback.addError(false, error, null);
	}

	public void taskCompleted()
	{
		callback.incrementCurrent();
	}

	public Map<Long, Long> getItems()
	{
		return items;
	}

	public Map<Long, Long> getHierarchies()
	{
		return hierarchies;
	}

	public void setFlags(Set<String> flags)
	{
		this.flags = flags;
	}

	public boolean hasFlag(String flag)
	{
		return flags.contains(flag);
	}

	public void setMessageCallback(MessageCallback message)
	{
		if( callback != null )
		{
			callback.setMessageCallback(message);
		}
	}

	public void setOldServerURL(URL url)
	{
		this.oldServerURL = url;
	}

	public URL getOldServerURL()
	{
		return oldServerURL;
	}

	public void setCurrentServerURL(URL currentServerURL)
	{
		this.currentServerURL = currentServerURL;
	}

	public URL getCurrentServerURL()
	{
		return currentServerURL;
	}

	public InstitutionInfo getInstituionInfo()
	{
		return instituionInfo;
	}

	public void setInstituionInfo(InstitutionInfo instituionInfo)
	{
		this.instituionInfo = instituionInfo;
	}

	public void addFlag(String flag)
	{
		flags.add(flag);
	}
}
