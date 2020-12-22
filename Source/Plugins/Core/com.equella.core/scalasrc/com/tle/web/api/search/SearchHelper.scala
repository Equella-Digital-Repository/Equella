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

package com.tle.web.api.search

import java.time.format.DateTimeParseException
import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneId}
import java.util.Date

import com.dytech.edge.exceptions.BadRequestException
import com.tle.beans.entity.DynaCollection
import com.tle.beans.item.{Comment, ItemIdKey}
import com.tle.common.Check
import com.tle.common.beans.exception.NotFoundException
import com.tle.common.search.DefaultSearch
import com.tle.common.search.whereparser.WhereParser
import com.tle.core.freetext.queries.FreeTextBooleanQuery
import com.tle.core.item.security.ItemSecurityConstants
import com.tle.core.item.serializer.{ItemSerializerItemBean, ItemSerializerService}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.interfaces.beans.AbstractExtendableBean
import com.tle.web.api.item.equella.interfaces.beans.{
  AbstractFileAttachmentBean,
  FileAttachmentBean
}
import com.tle.web.api.item.interfaces.beans.AttachmentBean
import com.tle.web.api.search.model.{SearchParam, SearchResultAttachment, SearchResultItem}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
  * This object provides functions that help validate a variety of search criteria(e.g.
  * the UUID of a collection and the UUID of an advanced search) and create an instance
  * of search and search-related objects.
  *
  * It also provides functions that can convert EquellaItemBean and AttachmentBean to
  * SearchResultItem and SearchResultAttachment, respectively.
  */
object SearchHelper {
  val privileges = Array(ItemSecurityConstants.VIEW_ITEM)

  /**
    * Create a new search with search criteria.
    * The search criteria is dependent on what parameters are passed in.
    * @param params Search parameters.
    * @return An instance of DefaultSearch
    */
  def createSearch(params: SearchParam): DefaultSearch = {
    val search = new DefaultSearch
    search.setUseServerTimeZone(true)
    search.setQuery(params.query)
    search.setOwner(params.owner)

    val orderType =
      DefaultSearch.getOrderType(Option(params.order).map(_.toLowerCase).orNull, params.query)
    search.setSortFields(orderType.getSortField(params.reverseOrder))

    val collectionUuids = handleCollections(params.advancedSearch, params.collections)
    search.setCollectionUuids(collectionUuids.orNull)

    val itemStatus = if (params.status.isEmpty) None else Some(params.status.toList.asJava)
    search.setItemStatuses(itemStatus.orNull)

    // The time of start should be '00:00:00' whereas the time of end should be '23:59:59'.
    val modifiedAfter  = handleModifiedDate(params.modifiedAfter, LocalTime.MIN)
    val modifiedBefore = handleModifiedDate(params.modifiedBefore, LocalTime.MAX)
    if (modifiedBefore.isDefined || modifiedAfter.isDefined) {
      search.setDateRange(Array(modifiedAfter.orNull, modifiedBefore.orNull))
    }
    val dynaCollectionQuery = handleDynaCollection(params.dynaCollection)
    val whereQuery = Option(params.whereClause) match {
      case Some(where) => WhereParser.parse(where)
      case None        => null
    }
    // If dynaCollectionQuery is not empty then combine it with whereQuery, and then assign it to freeTextQuery.
    // Otherwise, just assign whereQuery to freeTextQuery.
    val freeTextQuery = dynaCollectionQuery match {
      case Some(q) => q.add(whereQuery)
      case None    => whereQuery
    }
    search.setFreeTextQuery(freeTextQuery)

    search
  }

  /**
    * Return a free text query based on what dynamic collection uuid is provided.
    * @param dynaCollectionUuid The uuid of a dynamic collection.
    * @return An option which wraps an instance of FreeTextBooleanQuery.
    */
  def handleDynaCollection(dynaCollectionUuid: String): Option[FreeTextBooleanQuery] = {
    if (Check.isEmpty(dynaCollectionUuid)) {
      return None
    }
    val virtualDynaColl = LegacyGuice.dynaCollectionService.getByCompoundId(dynaCollectionUuid)
    Option(virtualDynaColl) match {
      case Some(v) =>
        val dynaCollection: DynaCollection = v.getVt
        val uuidAndVirtual: Array[String]  = dynaCollectionUuid.split(":")
        val virtual                        = if (uuidAndVirtual.length > 1) uuidAndVirtual(1) else null
        Some(LegacyGuice.dynaCollectionService.getSearchClause(dynaCollection, virtual))
      case None =>
        throw new NotFoundException(s"No dynamic collection matching UUID $dynaCollectionUuid")
    }
  }

  /**
    * Parse a string to a new instance of Date in the format of "yyyy-MM-dd".
    * @param dateString The string to parse.
    * @param time The time added to a date.
    * @return An Option which wraps an instance of Date, combining the successfully parsed dateString and provided time (based on the system's default timezone).
    */
  def handleModifiedDate(dateString: String, time: LocalTime): Option[Date] = {
    if (Check.isEmpty(dateString)) {
      return None
    }
    try {
      val dateTime = LocalDateTime.of(LocalDate.parse(dateString), time)
      //Need to convert back to util.date to work compatibly with old methods.
      Some(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant))
    } catch {
      case _: DateTimeParseException => throw new BadRequestException(s"Invalid date: $dateString")
    }
  }

  /**
    * Return a list of Collection IDs, depending on if Advanced search is provided or not.
    * @param advancedSearch The UUID of an Advanced search.
    * @param collections A list of Collection IDs.
    * @return An option which wraps a list of Collection IDs.
    */
  def handleCollections(advancedSearch: String,
                        collections: Array[String]): Option[java.util.Collection[String]] = {
    if (!Check.isEmpty(advancedSearch)) {
      Option(LegacyGuice.powerSearchService.getByUuid(advancedSearch)) match {
        case Some(ps) =>
          var collectionUuids = ListBuffer[String]()
          ps.getItemdefs.asScala.foreach(collectionUuids += _.getUuid)
          return Some(collectionUuids.toList.asJava)
        case None =>
          throw new NotFoundException(s"No advanced search UUID matching $advancedSearch")
      }
    }

    if (collections.isEmpty) {
      return None
    }

    val collectionIds = ListBuffer[String]()
    collections.foreach(c =>
      Option(LegacyGuice.itemDefinitionService.getByUuid(c)) match {
        case Some(_) => collectionIds += c
        case None    => throw new NotFoundException(s"No collection UUID matching $c")
    })
    Some(collectionIds.toList.asJava)
  }

  /**
    * Create a serializer for ItemBean.
    */
  def createSerializer(itemIds: List[ItemIdKey]): ItemSerializerItemBean = {
    val ids      = itemIds.map(_.getKey.asInstanceOf[java.lang.Long]).asJavaCollection
    val category = List(ItemSerializerService.CATEGORY_ALL).asJavaCollection
    LegacyGuice.itemSerializerService.createItemBeanSerializer(ids, category, false, privileges: _*)
  }

  /**
    * Convert a SearchItem to an instance of SearchResultItem.
    * @param item Represents a SearchItem, containing an ItemIdKey, EquellaItemBean, and
    * Boolean indicating if a search term has been found inside attachment content
    * @return An instance of SearchResultItem.
    */
  def convertToItem(item: SearchItem): SearchResultItem = {
    val key  = item.idKey
    val bean = item.bean
    SearchResultItem(
      uuid = key.getUuid,
      version = key.getVersion,
      name = Option(bean.getName),
      description = Option(bean.getDescription),
      status = bean.getStatus,
      createdDate = bean.getCreatedDate,
      modifiedDate = bean.getModifiedDate,
      collectionId = bean.getCollection.getUuid,
      commentCount = getItemCommentCount(key),
      starRatings = bean.getRating,
      attachments = convertToAttachment(bean.getAttachments, key),
      thumbnail = bean.getThumbnail,
      displayFields = bean.getDisplayFields.asScala.toList,
      displayOptions = Option(bean.getDisplayOptions),
      keywordFoundInAttachment = item.keywordFound,
      links = getLinksFromBean(bean)
    )
  }

  /**
    * Convert a list of AttachmentBean to a list of SearchResultAttachment
    */
  def convertToAttachment(attachmentBeans: java.util.List[AttachmentBean],
                          itemKey: ItemIdKey): Option[List[SearchResultAttachment]] = {
    Option(attachmentBeans).map(
      beans =>
        beans.asScala
          .map(att =>
            SearchResultAttachment(
              attachmentType = att.getRawAttachmentType,
              id = att.getUuid,
              description = Option(att.getDescription),
              preview = att.isPreview,
              mimeType = getMimetypeForAttachment(att),
              hasGeneratedThumb = thumbExists(itemKey, att),
              links = getLinksFromBean(att),
              filePath = getFilePathForAttachment(att)
          ))
          .toList)
  }

  def getItemComments(key: ItemIdKey): Option[java.util.List[Comment]] =
    Option(LegacyGuice.itemCommentService.getCommentsWithACLCheck(key, null, null, -1))

  def getItemCommentCount(key: ItemIdKey): Option[Integer] =
    Option(LegacyGuice.itemCommentService.getCommentCountWithACLCheck(key))

  /**
    * Determines if attachment contains a generated thumbnail in filestore
    */
  def thumbExists(itemKey: ItemIdKey, attachBean: AttachmentBean): Option[Boolean] = {
    attachBean match {
      case fileBean: FileAttachmentBean =>
        val item = LegacyGuice.viewableItemFactory.createNewViewableItem(itemKey)
        Option(fileBean.getThumbnail).map {
          LegacyGuice.fileSystemService.fileExists(item.getFileHandle, _)
        }
      case _ => None
    }
  }

  /**
    * Extract the mimetype for AbstractExtendableBean.
    */
  def getMimetypeForAttachment[T <: AbstractExtendableBean](bean: T): Option[String] = {
    bean match {
      case file: AbstractFileAttachmentBean =>
        Some(LegacyGuice.mimeTypeService.getMimeTypeForFilename(file.getFilename))
      case _ => None
    }
  }

  /**
    * If the attachment is a file, then return the path for that attachment.
    *
    * @param attachment a potential file attachment
    * @return the path of the provided file attachment
    */
  def getFilePathForAttachment(attachment: AttachmentBean): Option[String] =
    attachment match {
      case fileAttachment: FileAttachmentBean => Option(fileAttachment.getFilename)
      case _                                  => None
    }

  /**
    * Extract the value of 'links' from the 'extras' of AbstractExtendableBean.
    */
  def getLinksFromBean[T <: AbstractExtendableBean](bean: T) =
    bean.get("links").asInstanceOf[java.util.Map[String, String]]
}
