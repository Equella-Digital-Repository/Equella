val tikaVersion = "1.27"

libraryDependencies ++= Seq(
  "org.slf4j"       % "slf4j-api"    % "1.7.31",
  "org.slf4j"       % "slf4j-simple" % "1.7.31",
  "org.apache.tika" % "tika-core"    % tikaVersion,
  "org.apache.tika" % "tika-parsers" % tikaVersion
)

excludeDependencies += "commons-logging" % "commons-logging"
(assembly / assemblyOption) := (assembly / assemblyOption).value.copy(includeScala = false)
(assembly / assemblyMergeStrategy) := {
  case PathList("META-INF", "cxf", "bus-extensions.txt") => MergeStrategy.first

  // Due to the error: deduplicate: different file contents found in the following:
  //  .../org.apache.cxf/cxf-core/bundles/cxf-core-3.3.6.jar:META-INF/blueprint.handlers
  //  .../org.apache.cxf/cxf-rt-frontend-jaxrs/bundles/cxf-rt-frontend-jaxrs-3.3.6.jar:META-INF/blueprint.handlers
  //  .../org.apache.cxf/cxf-rt-rs-client/bundles/cxf-rt-rs-client-3.3.6.jar:META-INF/blueprint.handlers
  //  .../org.apache.cxf/cxf-rt-transports-http/bundles/cxf-rt-transports-http-3.3.6.jar:META-INF/blueprint.handlers
  // Different blueprint.handlers may specify different classes.  Using the first one allows testing to pass.
  case PathList("META-INF", "blueprint.handlers") => MergeStrategy.first

  // OK to do in Java 8 - interesting that the global case for module-info.class didn't pick up the bouncy castle files
  // deduplicate: different file contents found in the following:
  //  .../org.bouncycastle/bcmail-jdk15on/jars/bcmail-jdk15on-1.65.jar:META-INF/versions/9/module-info.class
  //  .../org.bouncycastle/bcpkix-jdk15on/jars/bcpkix-jdk15on-1.65.jar:META-INF/versions/9/module-info.class
  //  .../org.bouncycastle/bcprov-jdk15on/jars/bcprov-jdk15on-1.65.jar:META-INF/versions/9/module-info.class
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.first

  // The idea is to keep the later suffix list.
  // Due to the error: deduplicate: different file contents found in the following:
  // ...
  //  .../org.apache.cxf/cxf-rt-transports-http/bundles/cxf-rt-transports-http-3.3.6.jar:mozilla/public-suffix-list.txt
  //  .../org.apache.httpcomponents/httpclient/jars/httpclient-4.5.12.jar:mozilla/public-suffix-list.txt
  // ...
  case PathList("mozilla", "public-suffix-list.txt") => MergeStrategy.last

  // OK to do in Java 8
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

(assembly / mainClass) := Some("com.tle.conversion.Main")
