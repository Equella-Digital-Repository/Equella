package com.tle.web.lti.consumers.api.interfaces;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.lti.consumers.api.beans.LtiConsumerBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Aaron
 *
 */
@Produces({"application/json"})
@Path("lti/consumer")
@Api(value = "/lti/consumer", description = "lti-consumer")
public interface LtiConsumerResource extends BaseEntityResource<LtiConsumerBean, BaseEntitySecurityBean>
{
	@Override
	@GET
	@Path("/acl")
	@ApiOperation(value = "List global LTI consumer acls")
	public BaseEntitySecurityBean getAcls(@Context UriInfo uriInfo);

	@Override
	@PUT
	@Path("/acl")
	@ApiOperation(value = "Edit global LTI consumer acls")
	public Response editAcls(@Context UriInfo uriInfo, BaseEntitySecurityBean security);

	@Override
	@GET
	@ApiOperation(value = "List all LTI consumers")
	public SearchBean<LtiConsumerBean> list(@Context UriInfo uriInfo);

	@Override
	@GET
	@Path("/{uuid}")
	@ApiOperation(value = "Get an LTI consumer")
	public LtiConsumerBean get(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@Override
	@DELETE
	@Path("/{uuid}")
	@ApiOperation("Delete an LTI consumer")
	public Response delete(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@Override
	@POST
	@ApiOperation("Create a new LTI consumer")
	public Response create(@Context UriInfo uriInfo, @ApiParam LtiConsumerBean bean,
		@ApiParam(required = false) @QueryParam("file") String stagingUuid);

	@Override
	@PUT
	@Path("/{uuid}")
	@ApiOperation(value = "Edit an LTI consumer")
	public Response edit(@Context UriInfo uriInfo, @PathParam("uuid") String uuid, @ApiParam LtiConsumerBean bean,
		@ApiParam(required = false, value = "Staging area UUID") @QueryParam("file") String stagingUuid,
		@ApiParam(required = false) @QueryParam("lock") String lockId,
		@ApiParam(required = false) @QueryParam("keeplocked") boolean keepLocked);

	@Override
	@GET
	@Path("/{uuid}/lock")
	@ApiOperation("Read the lock for an LTI consumer")
	public Response getLock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@Override
	@POST
	@Path("/{uuid}/lock")
	@ApiOperation("Lock an LTI consumer")
	public Response lock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@Override
	@DELETE
	@Path("/{uuid}/lock")
	@ApiOperation("Unlock an LTI consumer")
	public Response unlock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);
}
