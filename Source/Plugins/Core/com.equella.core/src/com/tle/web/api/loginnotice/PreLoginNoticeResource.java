/*
 * Copyright 2017 Apereo
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

package com.tle.web.api.loginnotice;

import io.swagger.annotations.Api;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/** @author Samantha Fisher */
@Path("preloginnotice/")
@Api("Pre Login Notice")
public interface PreLoginNoticeResource {
  @GET
  @Produces(MediaType.TEXT_HTML)
  Response retrievePreLoginNotice();

  @PUT
  @Consumes(MediaType.TEXT_HTML)
  Response setPreLoginNotice(String loginNotice) throws IOException;

  @GET
  @Path("image/{name}")
  @PathParam("name")
  Response getPreLoginNoticeImage(@PathParam("name") String name) throws IOException;

  @PUT
  @Path("image/{name}")
  @PathParam("name")
  Response uploadPreLoginNoticeImage(
      File imageFile, @PathParam("name") String name, @Context UriInfo info) throws IOException;

  @DELETE
  Response deletePreLoginNotice();
}
