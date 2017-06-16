lazy val Birt = config("birt")
lazy val CustomCompile = config("compile") extend Birt

libraryDependencies ++= Seq(
  "rhino" % "js" % "1.7R2",
  "org.eclipse.birt" % "birt-api" % "3.7.2",
  "org.eclipse.birt.runtime" % "org.eclipse.datatools.connectivity.oda" % "3.3.3.v201110130935",
  "com.tle.reporting" % "reporting-common-6.3" % "0.20141006"
).map(_ % Birt)

ivyConfigurations := overrideConfigs(Birt, CustomCompile)(ivyConfigurations.value)

jpfLibraryJars := Classpaths.managedJars(Birt, Set("jar"), update.value)
