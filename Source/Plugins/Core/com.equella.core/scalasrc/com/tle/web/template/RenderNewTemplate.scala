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

import java.util.concurrent.ConcurrentHashMap

import cats.effect.IO
import com.tle.common.i18n.{CurrentLocale, LocaleUtils}
import com.tle.core.db.RunWithDB
import com.tle.core.i18n.LocaleLookup
import com.tle.legacy.LegacyGuice
import com.tle.web.DebugSettings
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.resources.ResourcesService
import com.tle.web.sections._
import com.tle.web.sections.equella.ScalaSectionRenderable
import com.tle.web.sections.events._
import com.tle.web.sections.jquery.libraries.JQueryCore
import com.tle.web.sections.js.generic.expression.ObjectExpression
import com.tle.web.sections.js.generic.function.IncludeFile
import com.tle.web.sections.js.generic.statement.ScriptStatement
import com.tle.web.sections.render._
import com.tle.web.settings.UISettings
import io.circe.generic.auto._
import org.jsoup.Jsoup

import scala.collection.JavaConverters._

case class ReactPageModel(getReactScript: String)

object RenderNewTemplate {

  val r            = ResourcesService.getResourceHelper(getClass)
  val DisableNewUI = "DISABLE_NEWUI"
  val SetupJSKey   = "setupJSData"
  val ReactHtmlKey = "reactJSHtml"

  val htmlBundleCache = new ConcurrentHashMap[String, (PreRenderable, String)]()

  val bundleJs = new PreRenderable {
    override def preRender(info: PreRenderContext): Unit = {
      new IncludeFile(
        s"api/language/bundle/${LocaleLookup.selectLocale.getLocale.toLanguageTag}/bundle.js")
        .preRender(info)
      new IncludeFile(s"api/theme/theme.js").preRender(info)
    }
  }

  def parseEntryHtml(filename: String): (PreRenderable, String) = {
    val inpStream = getClass.getResourceAsStream(s"/web/reactjs/$filename")
    if (inpStream == null) sys.error(s"Failed to find $filename react html bundle")
    val htmlDoc = Jsoup.parse(inpStream, "UTF-8", "")
    inpStream.close()
    val links   = htmlDoc.getElementsByTag("link").asScala
    val scripts = htmlDoc.getElementsByTag("script").asScala
    scripts.foreach { e =>
      if (e.hasAttr("src")) {
        e.attr("src", r.url(e.attr("src")))
      }
    }
    val prerender: PreRenderable = info => {
      info.preRender(JQueryCore.PRERENDER)
      if (Option(info.getRequest.getHeader("User-Agent")).exists(_.contains("Trident"))) {
        info.addJs("https://cdn.polyfill.io/v2/polyfill.min.js?features=es6")
      }
      info.preRender(bundleJs)
      links.foreach(l => info.addCss(r.url(l.attr("href"))))
    }
    (prerender, htmlDoc.getElementsByTag("body").toString)
  }

  val NewLayoutKey = "NEW_LAYOUT"

  def isNewLayout(info: SectionInfo): Boolean = {
    Option(info.getAttribute(NewLayoutKey)).getOrElse {
      val paramOverride = Option(info.getRequest.getParameter("old")).map(!_.toBoolean)
      val sessionOverride = paramOverride.fold(
        Option(LegacyGuice.userSessionService.getAttribute[Boolean](NewLayoutKey))) { newUI =>
        LegacyGuice.userSessionService.setAttribute(NewLayoutKey, newUI)
        Some(newUI)
      }
      val newLayout = sessionOverride.getOrElse {
        RunWithDB
          .executeIfInInstitution(UISettings.cachedUISettings)
          .getOrElse(UISettings.defaultSettings)
          .newUI
          .enabled
      }
      info.setAttribute(NewLayoutKey, newLayout)
      newLayout
    }
  }

  case object HeaderSection
      extends ScalaSectionRenderable({ writer =>
        writer.getInfo() match {
          case src: StandardRenderContext =>
            src.getJsFiles.asScala.foreach { s =>
              writer.writeTag("script", "src", s)
              writer.endTag("script")
            }
            src.getCssFiles.asScala.foreach { s: CssInclude =>
              writer.writeTag("link",
                              "rel",
                              "stylesheet",
                              "type",
                              "text/css",
                              "href",
                              s.getHref(src))
              writer.endTag("link")
            }
        }
      })

  def renderNewHtml(context: RenderEventContext, viewFactory: FreemarkerFactory): SectionResult = {
    val req = context.getRequest
    val _renderData =
      new ObjectExpression("baseResources", r.url(""), "newUI", java.lang.Boolean.TRUE)
    val renderData =
      Option(req.getAttribute(SetupJSKey).asInstanceOf[ObjectExpression => ObjectExpression])
        .map(_.apply(_renderData))
        .getOrElse(_renderData)

    val htmlPage =
      Option(req.getAttribute(ReactHtmlKey).asInstanceOf[String]).getOrElse("index.html")
    val (scriptPreRender, body) =
      if (DebugSettings.isDevMode) parseEntryHtml(htmlPage)
      else htmlBundleCache.computeIfAbsent(htmlPage, parseEntryHtml)
    context.preRender(scriptPreRender)
    renderReact(context, viewFactory, renderData, body)
  }

  def renderReact(context: RenderEventContext,
                  viewFactory: FreemarkerFactory,
                  renderData: ObjectExpression,
                  body: String): SectionResult = {
    if (DebugSettings.isAutoTestMode) {
      context.preRender(RenderTemplate.AUTOTEST_JS)
    }
    val tempResult = new GenericTemplateResult()
    tempResult.addNamedResult("header", HeaderSection)
    viewFactory.createResultWithModel("layouts/outer/react.ftl",
                                      TemplateScript(body, renderData, tempResult, ""))
  }

  case class TemplateScript(getBody: String,
                            getRenderJs: ObjectExpression,
                            getTemplate: TemplateResult,
                            htmlAttributes: String) {
    def getLang = LocaleUtils.toHtmlLang(CurrentLocale.getLocale)

    def isRightToLeft = CurrentLocale.isRightToLeft

    def getHtmlAttrs = htmlAttributes
  }

}
