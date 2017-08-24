import com.typesafe.config.Config
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import de.heikoseeberger.sbtheader._
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object CommonSettings extends AutoPlugin {
  object autoImport {
    lazy val versionProperties = taskKey[File]("Version property file")
    lazy val upgradeZip = taskKey[File]("Create upgrade zip")
    lazy val installerZip = taskKey[File]("Create the installer zip")
    lazy val equellaVersion = settingKey[EquellaVersion]("The major equella version")
    lazy val oracleDriverJar = settingKey[Option[File]]("The oracle driver jar")
    lazy val buildConfig = settingKey[Config]("The build configuration settings")
    lazy val prepareDevConfig = taskKey[Unit]("Prepare the dev learningedge-config folder")
    lazy val writeSourceZip = taskKey[File]("Write out a zip containing all sources")

    lazy val platformCommon = LocalProject("com_tle_platform_common")
    lazy val platformSwing = LocalProject("com_tle_platform_swing")
    lazy val platformEquella = LocalProject("com_tle_platform_equella")
    lazy val log4jCustom = LocalProject("com_tle_log4j")
    lazy val xstreamDep = "com.thoughtworks.xstream" % "xstream" % "1.4.9"
  }

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = HeaderPlugin && JvmPlugin

  import autoImport._
  override def projectSettings = Seq(
    organization := "org.apereo.equella",
    javacOptions ++= Seq("-source", "1.8"),
    compileOrder := CompileOrder.Mixed,
    headerLicense := Some(HeaderLicense.ALv2("2017", "Apereo")),
    resolvers ++= Seq(
      "Local EQUELLA deps" at IO.toURI(file(Path.userHome.absolutePath) / "/equella-deps").toString,
      "EBI Nexus" at "http://www.ebi.ac.uk/intact/maven/nexus/content/repositories/ebi-repo/",
      Resolver.bintrayRepo("omegat-org", "maven")
    ),
    libraryDependencies += "junit" % "junit" % "4.12" % Test
  )
}