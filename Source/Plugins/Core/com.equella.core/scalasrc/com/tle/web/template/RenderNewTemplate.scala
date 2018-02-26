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

package com.tle.web.template

import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.db.RunWithDB
import com.tle.core.plugins.PluginTracker
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.navigation.MenuService
import com.tle.web.resources.ResourcesService
import com.tle.web.sections._
import com.tle.web.sections.equella.js.StandardExpressions
import com.tle.web.sections.events._
import com.tle.web.sections.events.js.{BookmarkAndModify, JSHandler}
import com.tle.web.sections.jquery.libraries.JQueryCore
import com.tle.web.sections.js.JSUtils
import com.tle.web.sections.js.generic.expression.{ArrayExpression, ObjectExpression}
import com.tle.web.sections.js.generic.function.IncludeFile
import com.tle.web.sections.render._
import com.tle.web.settings.UISettings
import com.tle.web.template.Decorations.MenuMode
import com.tle.web.template.section.MenuContributor
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.JavaConverters._

case class ReactPageModel(getReactScript: String)

object RenderNewTemplate {
  val r = ResourcesService.getResourceHelper(getClass)

  val reactTemplate = r.url("reactjs/index.js")

  val NewLayoutKey = "NEW_LAYOUT"

  def isNewLayout(info: SectionInfo): Boolean = {
    Option(info.getAttribute(NewLayoutKey)).getOrElse {
      val oldOverride = Option(info.getRequest.getParameter("old")).map(_.toBoolean)
      val lookedUp = if (oldOverride.isEmpty) {
        RunWithDB.executeIfInInstitution(UISettings.cachedUISettings).map(_.newUI.enabled)
      } else None
      val newLayout = oldOverride.orElse(lookedUp).getOrElse(false)
      info.setAttribute(NewLayoutKey, newLayout)
      newLayout
    }
  }

  case class TemplateScript(getScriptUrl : String,  getRenderJs: ObjectExpression, getTemplate: TemplateResult)

  def renderHtml(viewFactory: FreemarkerFactory, context: RenderEventContext,
                 tempResult: TemplateResult, menuService: MenuService): SectionResult = {

    context.preRender(JQueryCore.PRERENDER)
    val decs = Decorations.getDecorations(context)
    if (decs.getReactUrl == null)
    {
      val precontext = context.getPreRenderContext
      precontext.preRender(RenderTemplate.STYLES_CSS)
      precontext.preRender(RenderTemplate.CUSTOMER_CSS)
    }

    val _bodyResult = tempResult.getNamedResult(context, "body")
    val unnamedResult = tempResult.getNamedResult(context, "unnamed")
    val bodyResult = CombinedRenderer.combineResults(_bodyResult, unnamedResult)
    val bodyTag = context.getBody
    if (!decs.isExcludeForm) {
      val formTag = context.getForm
      formTag.setNestedRenderable(bodyResult)
      bodyTag.setNestedRenderable(formTag)
    } else {
      bodyTag.setNestedRenderable(bodyResult)
    }
    val menuValues = menuOptions(context, menuService)
    val html = SectionUtils.renderToString(context, bodyTag)
    val title = Option(decs.getTitle).map(_.getText).getOrElse("")
    val reactScript = Option(decs.getReactUrl).getOrElse(reactTemplate)
    val htmlVals = new ObjectExpression("body", html)
    val renderData = new ObjectExpression("baseResources", r.url(""), "newUI", java.lang.Boolean.TRUE, "html", htmlVals, "title", title,
      "menuItems", new ArrayExpression(JSUtils.convertExpressions(menuValues.toSeq: _*)))
    viewFactory.createResultWithModel("layouts/outer/react.ftl", TemplateScript(reactScript, renderData, tempResult))
  }

  private val GUEST_FILTER = new PluginTracker.ParamFilter("enabledFor", "guest")
  private val SERVER_ADMIN_FILTER = new PluginTracker.ParamFilter("enabledFor", "serverAdmin")
  private val LOGGED_IN_FILTER = new PluginTracker.ParamFilter("enabledFor", true, "loggedIn")

  def menuOptions(context: RenderEventContext, menuService: MenuService): Iterable[ArrayExpression] = {
    val decorations = Decorations.getDecorations(context)
    val menuMode = decorations.getMenuMode
    if (menuMode == MenuMode.HIDDEN) Iterable.empty
    else {
      val contributors = menuService.getContributors
      val filter = if (CurrentInstitution.get == null) SERVER_ADMIN_FILTER
      else if (CurrentUser.isGuest) GUEST_FILTER
      else LOGGED_IN_FILTER
      contributors.getExtensions(filter).asScala.flatMap { ext =>
        contributors.getBeanByExtension(ext).getMenuContributions(context).asScala
      }.groupBy(_.getGroupPriority).toSeq.sortBy(_._1).map {
        case (_, links) =>
          val menuLinks = links.sortBy(_.getLinkPriority).map { mc =>
            val menuLink = mc.getLink
            val href = Option(menuLink.getBookmark).getOrElse(
              new BookmarkAndModify(context, menuLink.getHandlerMap.getHandler("click").getModifier)).getHref
            new ObjectExpression("title", menuLink.getLabelText, "href", href, "systemIcon", mc.getSystemIcon)
          }.asJava
          new ArrayExpression(menuLinks)
      }
    }
  }
}
