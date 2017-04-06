package com.tle.web.api.collection.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.collect.Lists;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.guice.Bind;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.security.TLEAclManager;
import com.tle.web.api.collection.DynaCollectionBeanSerializer;
import com.tle.web.api.collection.beans.DynaCollectionBean;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.remoting.rest.service.UrlLinkService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author larry
 */

@Bind
@Produces({"application/json"})
@Path("dynacollection")
@Api(value = "/dynacollection", description = "dynacollection")
@Singleton
public class DynaCollectionResource
{
	@Inject
	private DynaCollectionService dynaCollectionService;
	@Inject
	private UrlLinkService urlLinkService;
	@Inject
	private DynaCollectionBeanSerializer serializer;
	@Inject
	private TLEAclManager aclManager;

	/**
	 * The SOAP implementation was tailored for "harvesterUsage", so we'll
	 * provide for an optional query parameter to facilitate this
	 * specialisation.
	 * 
	 * @return
	 */
	@GET
	@Path("")
	@ApiOperation(value = "Retrieve dynamic collections")
	public Response getDynamicCollections(
		@ApiParam(value = "usage string: search or harvester", required = false) @QueryParam("usage") String usage)
	{
		if( !Check.isEmpty(usage) )
		{
			// the constants in use are searchUsage or harvesterUsage
			// so we'll allow for ..?usage=search or ?usage=harvester
			if( !usage.endsWith("Usage") )
			{
				usage += "Usage";
			}
		}
		List<VirtualisableAndValue<DynaCollection>> virtualisables = Lists.newArrayList(dynaCollectionService
			.enumerateExpanded(usage));

		Iterator<VirtualisableAndValue<DynaCollection>> it = virtualisables.iterator();
		while( it.hasNext() )
		{
			DynaCollection dynaCol = it.next().getVt();
			if( aclManager.filterNonGrantedPrivileges(dynaCol, Collections.singleton("LIST_DYNA_COLLECTION")).isEmpty() )
			{
				it.remove();
			}
		}

		List<DynaCollectionBean> results = dynaBeansFromDynaColls(virtualisables);

		SearchBean<DynaCollectionBean> searchBean = new SearchBean<DynaCollectionBean>();
		searchBean.setStart(0);
		searchBean.setLength(results.size());
		searchBean.setAvailable(results.size());
		searchBean.setResults(results);
		return Response.ok(searchBean).build();
	}

	@GET
	@Path("/{uuid}")
	@ApiOperation(value = "Get details on a single Dynamic Collection")
	public Response getDynaCollection(@ApiParam("dynaCollection uuid") @PathParam("uuid") String uuid)
	{
		VirtualisableAndValue<DynaCollection> dynaColl = dynaCollectionService.getByCompoundId(uuid);
		if( dynaColl == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		DynaCollection vt = dynaColl.getVt();
		if( aclManager.filterNonGrantedPrivileges(vt, Collections.singleton("VIEW_DYNA_COLLECTION")).isEmpty() )
		{
			return Response.status(Status.FORBIDDEN).build();
		}

		DynaCollectionBean bean = dynaBeanFromDynaColl(dynaColl, true);
		return Response.ok(bean).build();
	}

	/**
	 * @param allDynaColls
	 * @return List of DynaCollectionBean
	 */
	private List<DynaCollectionBean> dynaBeansFromDynaColls(List<VirtualisableAndValue<DynaCollection>> allDynaColls)
	{
		List<DynaCollectionBean> results = new ArrayList<DynaCollectionBean>();
		if( !Check.isEmpty(allDynaColls) )
		{
			for( VirtualisableAndValue<DynaCollection> dynaColl : allDynaColls )
			{
				DynaCollectionBean bean = dynaBeanFromDynaColl(dynaColl, false);
				results.add(bean);
			}
		}
		return results;
	}

	private DynaCollectionBean dynaBeanFromDynaColl(VirtualisableAndValue<DynaCollection> dynaCollVirtualised,
		boolean heavy)
	{
		DynaCollection dynaColl = dynaCollVirtualised.getVt();
		String virtualValue = dynaCollVirtualised.getVirtualisedValue();

		DynaCollectionBean bean = serializer.serialize(dynaColl, virtualValue, heavy);

		// link to self must allow for compound id
		Map<String, String> links = Collections.singletonMap("self", getSelfLink(dynaColl.getUuid(), virtualValue)
			.toString());
		bean.set("links", links);

		return bean;
	}

	/**
	 * link to self must allow for compound id
	 * 
	 * @param courseUuid
	 * @return
	 */
	private URI getSelfLink(String dynaCollUuid, String virtualValue)
	{
		String compound = dynaCollUuid;
		if( !Check.isEmpty(virtualValue) )
		{
			compound += ":" + URLUtils.basicUrlEncode(virtualValue);
		}
		return urlLinkService.getMethodUriBuilder(getClass(), "getDynaCollection").build(compound);
	}
}
