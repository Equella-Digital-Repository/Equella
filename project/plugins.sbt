addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.1")

addSbtPlugin("de.johoop" % "sbt-testng-plugin" % "3.1.1")

addSbtPlugin("com.etsy" % "sbt-checkstyle-plugin" % "3.1.1")
// Old version used because something else depends on an old JAWN
val circeVersion = "0.7.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "com.typesafe"           % "config"                % "1.4.1",
  "org.jacoco"             % "org.jacoco.report"     % "0.8.7",
  "org.jdom"               % "jdom2"                 % "2.0.6",
  "org.apache.axis2"       % "axis2-kernel"          % "1.7.9",
  "org.apache.axis2"       % "axis2-java2wsdl"       % "1.7.9",
  "org.apache.axis2"       % "axis2-adb"             % "1.7.9",
  "org.apache.axis2"       % "axis2-jaxbri"          % "1.7.9",
  "org.apache.axis2"       % "axis2-adb-codegen"     % "1.7.9",
  "org.apache.axis2"       % "axis2-codegen"         % "1.7.9",
  "org.apache.axis2"       % "axis2-xmlbeans"        % "1.7.9",
  "commons-logging"        % "commons-logging"       % "1.2",
  "commons-discovery"      % "commons-discovery"     % "0.5",
  "commons-configuration"  % "commons-configuration" % "1.10",
  "commons-beanutils"      % "commons-beanutils"     % "1.9.4",
  "commons-codec"          % "commons-codec"         % "1.15",
  "org.slf4j"              % "slf4j-nop"             % "1.7.31",
  "com.yahoo.platform.yui" % "yuicompressor"         % "2.4.8"
)
dependencyOverrides += "com.puppycrawl.tools" % "checkstyle" % "8.44"
