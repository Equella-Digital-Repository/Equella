/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.api.drm

import com.dytech.edge.exceptions.{DRMException, ItemNotFoundException}
import com.tle.beans.item.{Item, ItemId}
import com.tle.exceptions.AccessDeniedException
import com.tle.legacy.LegacyGuice
import com.tle.web.api.ApiErrorResponse.{
  badRequest,
  forbiddenRequest,
  resourceNotFound,
  unauthorizedRequest
}
import io.swagger.annotations.{Api, ApiParam}
import org.jboss.resteasy.annotations.cache.NoCache
import javax.ws.rs.core.Response
import javax.ws.rs.{BadRequestException, GET, NotFoundException, POST, Path, PathParam, Produces}
import scala.util.{Failure, Success, Try}

@NoCache
@Path("item/{uuid}/{version}/drm")
@Produces(Array("application/json"))
@Api("Item DRM")
class DrmResource {
  val drmService = LegacyGuice.drmService

  @GET
  def getDrmTerms(@ApiParam("Item UUID") @PathParam("uuid") uuid: String,
                  @ApiParam("Item Version") @PathParam("version") version: Int): Response = {
    val getTerms = getItem.andThen(_.getDrmSettings)
    Try {
      Option(getTerms(new ItemId(uuid, version))) match {
        case Some(drmSettings) => ItemDrmDetails(drmSettings)
        case None =>
          throw new NotFoundException(s"Failed to find DRM terms for item: $uuid/$version")
      }
    } match {
      case Success(drmDetails) => Response.ok().entity(drmDetails).build()
      case Failure(e)          => mapException(e)(Seq(e.getMessage))
    }
  }

  @POST
  def acceptDrm(@ApiParam("Item UUID") @PathParam("uuid") uuid: String,
                @ApiParam("Item Version") @PathParam("version") version: Int): Response = {

    val acceptLicense: Item => Long = drmService.acceptLicenseOrThrow
    Try {
      (getItem andThen acceptLicense)(new ItemId(uuid, version))
    } match {
      case Success(id) => Response.ok().entity(id).build()
      case Failure(e)  => mapException(e)(Seq(e.getMessage))
    }
  }

  // Take a subtype of Throwable and return a function which takes a sequence of string and returns a Response.
  private def mapException[T <: Throwable](e: T): Seq[String] => Response = {
    e match {
      case _: BadRequestException                               => badRequest
      case _: AccessDeniedException                             => forbiddenRequest
      case _: DRMException                                      => unauthorizedRequest
      case _ @(_: ItemNotFoundException | _: NotFoundException) => resourceNotFound
    }
  }

  private val getItem: ItemId => Item = LegacyGuice.itemService.getUnsecure
}
