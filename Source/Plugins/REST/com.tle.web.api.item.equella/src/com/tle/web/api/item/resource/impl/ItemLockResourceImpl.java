package com.tle.web.api.item.resource.impl;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.dytech.edge.exceptions.NotFoundException;
import com.google.common.collect.Maps;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemLock;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.ItemLockingService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.interfaces.ItemLockResource;
import com.tle.web.api.item.interfaces.beans.ItemLockBean;
import com.tle.web.remoting.rest.service.UrlLinkService;

@SuppressWarnings("nls")
@Bind(ItemLockResource.class)
@Singleton
public class ItemLockResourceImpl implements ItemLockResource
{
	@Inject
	private ItemLockingService lockingService;
	@Inject
	private UrlLinkService urlLinkService;

	@Override
	public Response get(UriInfo uriInfo, String uuid, int version)
	{
		final ItemLock itemLock = lockingService.get(new ItemId(uuid, version));
		if( itemLock == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		final ItemLockBean lockBean = convertLock(itemLock, uuid, version);
		return Response.status(Status.OK).entity(lockBean).build();
	}

	@Override
	public Response lock(UriInfo uriInfo, String uuid, int version)
	{
		final ItemLock itemLock;
		itemLock = lockingService.lock(new ItemId(uuid, version));
		final ItemLockBean lockBean = convertLock(itemLock, uuid, version);
		return Response.status(Status.CREATED).entity(lockBean).build();
	}

	@Override
	public Response unlock(String uuid, int version)
	{
		try
		{
			lockingService.unlock(new ItemId(uuid, version));
		}
		// it actually throws this if the item is not locked
		catch( AccessDeniedException a )
		{
			throw new NotFoundException(a.getMessage());
		}
		return Response.status(Status.NO_CONTENT).build();
	}

	private ItemLockBean convertLock(ItemLock lock, String uuid, int version)
	{
		final URI loc = urlLinkService.getMethodUriBuilder(ItemLockResource.class, "get").build(uuid, version);
		final ItemLockBean lockBean = new ItemLockBean();
		final Map<String, String> linkMap = Maps.newHashMap();
		linkMap.put("self", loc.toString());
		lockBean.setOwner(new UserBean(lock.getUserID()));
		lockBean.setUuid(lock.getUserSession());
		lockBean.set("links", linkMap);
		return lockBean;
	}
}
