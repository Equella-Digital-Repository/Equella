val springVersion = "5.3.9"

libraryDependencies ++= Seq(
  "org.slf4j"              % "jcl-over-slf4j"  % "1.7.31",
  "org.slf4j"              % "slf4j-api"       % "1.7.31",
  "org.slf4j"              % "slf4j-simple"    % "1.7.31",
  "org.jvnet.hudson"       % "xstream"         % "1.3.1-hudson-8",
  "com.google.guava"       % "guava"           % "18.0",
  "com.github.equella.jpf" % "jpf"             % "1.0.7",
  "com.fifesoft"           % "rsyntaxtextarea" % "3.1.3",
  "com.miglayout"          % "miglayout-swing" % "4.2",
  "org.springframework"    % "spring-web"      % springVersion,
  "org.springframework"    % "spring-aop"      % springVersion,
  "org.springframework"    % "spring-context"  % springVersion
)

(Compile / unmanagedJars) += file(sys.props("java.home")) / "lib/javaws.jar"

(run / fork) := true

(Compile / run / mainClass) := Some("com.tle.client.harness.ClientLauncher")
