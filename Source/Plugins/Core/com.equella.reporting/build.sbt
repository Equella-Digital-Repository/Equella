lazy val Birt          = config("birt")
lazy val CustomCompile = config("compile") extend Birt

libraryDependencies ++= Seq(
  "com.github.equella.reporting" % "birt-api"                               % "3.7.2",
  "org.eclipse.birt.runtime"     % "org.eclipse.datatools.connectivity.oda" % "3.5.0.201603142002",
  "com.github.equella.reporting" % "reporting-common"                       % "6.5",
  "rhino"                        % "js"                                     % "1.7R2"
).map(_ % Birt)

ivyConfigurations := overrideConfigs(Birt, CustomCompile)(ivyConfigurations.value)

jpfLibraryJars := Classpaths.managedJars(Birt, Set("jar"), update.value)
