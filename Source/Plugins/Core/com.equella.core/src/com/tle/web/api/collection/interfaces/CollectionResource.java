/*
 * Copyright 2019 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.api.collection.interfaces;

import com.tle.web.api.collection.interfaces.beans.AllCollectionsSecurityBean;
import com.tle.web.api.collection.interfaces.beans.CollectionBean;
import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.EntityLockBean;
import com.tle.web.api.interfaces.beans.PagingBean;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Path("collection/")
@Api(value = "/collection", description = "collection")
public interface CollectionResource extends BaseEntityResource<CollectionBean, AllCollectionsSecurityBean> {
    @GET
    @Path("/")
    @ApiOperation(value = "List all collections", notes = "Retrieve a list of all collections")
    PagingBean<CollectionBean> list(
            @Context UriInfo uriInfo,
            @ApiParam("Search name and description") @QueryParam("q") String q,
            @ApiParam("Privilege(s) to filter by") @QueryParam("privilege") List<String> privilege,
            @QueryParam("resumption") @ApiParam("Resumption token for paging") String resumptionToken,
            @QueryParam("length") @ApiParam("Number of results") @DefaultValue("10") int length,
            @QueryParam("full") @ApiParam("Return full entity (needs VIEW or EDIT privilege)") boolean full);

    @GET
    @Path("/acl")
    @ApiOperation(value = "List global collection acls", notes = "Manage global collection ACLs")
    public AllCollectionsSecurityBean getAcls(@Context UriInfo uriInfo);

    @PUT
    @Path("/acl")
    @ApiOperation(value = "Edit global collection acls")
    public Response editAcls(@Context UriInfo uriInfo,
                             @ApiParam(value = "The global collection acls") AllCollectionsSecurityBean security);

    @GET
    @Path("/{uuid}")
    @ApiOperation(value = "Get a collection", notes = "Manage collections")
    public CollectionBean get(@Context UriInfo uriInfo,
                              @ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid);

    @DELETE
    @Path("/{uuid}")
    @ApiOperation("Delete a collection")
    public Response delete(@Context UriInfo uriInfo, @ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid);

    @POST
    @ApiOperation(value = "Create a new collection", notes = "Create new or list existing collections")
    @ApiResponses({@ApiResponse(code = 201, message = "Location: {newcollection uri}")})
    public Response create(@Context UriInfo uriInfo, @ApiParam(value = "Collection") CollectionBean bean,
                           @ApiParam(required = false) @QueryParam(value = "file") String stagingUuid);

    @PUT
    @Path("/{uuid}")
    @ApiOperation("Edit a collection")
    @ApiResponses({@ApiResponse(code = 200, message = "Location: {collection uri}")})
    public Response edit(@Context UriInfo uriInfo, @ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid,
                         @ApiParam CollectionBean bean,
                         @ApiParam(required = false, value = "Staging area UUID") @QueryParam("file") String stagingUuid,
                         @ApiParam(required = false, value = "The lock UUID if locked") @QueryParam("lock") String lockId,
                         @ApiParam(value = "Unlock collection after edit") @QueryParam("keeplocked") boolean keepLocked);

    @GET
    @Path("/{uuid}/lock")
    @ApiOperation(value = "Read the lock for a collection", response = EntityLockBean.class)
    public Response getLock(@Context UriInfo uriInfo,
                            @ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid);

    @POST
    @Path("/{uuid}/lock")
    @ApiOperation(value = "Lock a collection", notes = "A collection lock will prevent others from editing it while locked", response = EntityLockBean.class)
    public Response lock(@Context UriInfo uriInfo, @ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid);

    @DELETE
    @Path("/{uuid}/lock")
    @ApiOperation("Unlock a collection")
    public Response unlock(@Context UriInfo uriInfo, @ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid);
}
