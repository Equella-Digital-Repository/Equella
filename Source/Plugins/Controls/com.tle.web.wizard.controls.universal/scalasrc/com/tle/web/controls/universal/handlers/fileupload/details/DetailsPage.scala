package com.tle.web.controls.universal.handlers.fileupload.details

import com.tle.beans.item.attachments.Attachment
import com.tle.common.Check
import com.tle.common.filesystem.FileEntry
import com.tle.core.services.ZipProgress
import com.tle.web.controls.universal.handlers.fileupload.WebFileUploads
import com.tle.web.controls.universal.{ControlContext, StagingContext}
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.events.RenderContext
import com.tle.web.sections.render.{Label, SectionRenderable}
import com.tle.web.sections.standard.TextField
import com.tle.web.viewurl.ViewableResource

trait ZipHandler {
  def zipProgress: Option[ZipProgress]

  def selectedAttachments: Map[String, Attachment]

  def unzip: ZipProgress

  def removeUnzipped: Unit

  def unzipped: Boolean

  def unzippedEntries: Seq[FileEntry]
}

trait ViewerHandler {
  def viewableResource(info: SectionInfo): ViewableResource

  def viewerListModel: ViewersListModel
}

object DetailsPage {
  val LABEL_ERROR_BLANK = WebFileUploads.label("handlers.abstract.error.blank")
}

import DetailsPage._

trait DetailsPage {

  def editingAttachment: SectionInfo => Attachment

  def previewable: Boolean

  def renderDetails(context: RenderContext): SectionRenderable

  def prepareUI(info: SectionInfo): Unit

  def editAttachment(info: SectionInfo, a: Attachment, ctx: ControlContext): Attachment

  def validate(info: SectionInfo): Boolean

  def displayName: TextField

  def validateDisplayName(info: SectionInfo): Option[(String, Label)] = {
    if (Check.isEmpty(displayName.getValue(info))) {
      Some("displayName" -> LABEL_ERROR_BLANK)
    } else None
  }
}

