val springVersion = "5.3.9"

libraryDependencies ++= Seq(
  "com.github.equella.jpf" % "jpf"             % "1.0.7",
  "com.google.guava"       % "guava"           % "18.0",
  "org.slf4j"              % "jcl-over-slf4j"  % "1.7.31",
  "org.slf4j"              % "slf4j-simple"    % "1.7.31",
  "org.springframework"    % "spring-web"      % springVersion,
  "org.springframework"    % "spring-aop"      % springVersion,
  "org.springframework"    % "spring-context"  % springVersion,
  "com.fifesoft"           % "rsyntaxtextarea" % "3.1.3",
  "com.miglayout"          % "miglayout-swing" % "4.2",
  xstreamDep
)

excludeDependencies ++= Seq(
  "commons-logging" % "commons-logging",
  // Spring 5 added a default logging bridge.  In oEQ, this results in
  // a [deduplicate: different file contents found in the following] error
  // ...org.slf4j/jcl-over-slf4j/jars/jcl-over-slf4j-1.7.30.jar:org/apache/commons/logging/Log.class
  // ...org.springframework/spring-jcl/jars/spring-jcl-5.3.9.jar:org/apache/commons/logging/Log.class
  // As per https://github.com/spring-projects/spring-framework/issues/20611 ,
  // since we already have logging in place, we can safely exclude the dep from spring.
  "org.springframework" % "spring-jcl"
)

(assembly / packageOptions) += Package.ManifestAttributes("Permissions" -> "all-permissions")
(assembly / assemblyOption) := (assembly / assemblyOption).value
(assembly / assemblyMergeStrategy) := {
  case PathList("org", "xmlpull", "v1", _*) => MergeStrategy.first
  // The following three were added when the hibernate-types was added the the hibernate module
  case PathList("javax", "activation", _*)       => MergeStrategy.first
  case PathList("javax", "xml", _*)              => MergeStrategy.first
  case PathList("META-INF", "versions", "9", _*) => MergeStrategy.first
  // Added due to a [deduplicate: different file contents found in the following] error against:
  // org.springframework/spring-context/jars/spring-context-3.2.18.RELEASE.jar:overview.html
  // org.springframework/spring-web/jars/spring-web-3.2.18.RELEASE.jar:overview.html
  case x if x.contains("overview.html") => MergeStrategy.first
  // Post SpringHib5 upgrade, the following error was thrown on build:
  // deduplicate: different file contents found in the following:
  // [error] .../com.fasterxml/classmate/bundles/classmate-1.5.1.jar:module-info.class
  // [error] .../com.sun.istack/istack-commons-runtime/jars/istack-commons-runtime-3.0.7.jar:module-info.class
  // [error] .../com.sun.xml.fastinfoset/FastInfoset/jars/FastInfoset-1.2.15.jar:module-info.class
  // [error] .../javax.xml.bind/jaxb-api/jars/jaxb-api-2.3.1.jar:module-info.class
  // [error] .../org.glassfish.jaxb/jaxb-runtime/jars/jaxb-runtime-2.3.1.jar:module-info.class
  // [error] .../org.glassfish.jaxb/txw2/jars/txw2-2.3.1.jar:module-info.class
  // [error] .../org.jvnet.staxex/stax-ex/jars/stax-ex-1.8.jar:module-info.class
  // As per https://stackoverflow.com/questions/54834125/sbt-assembly-deduplicate-module-info-class , discarding is OK for Java 8
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
dependsOn(platformCommon, platformSwing, platformEquella, LocalProject("com_tle_webstart_admin"))
