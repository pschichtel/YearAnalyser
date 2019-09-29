name := "YearAnalyser"
 
version := "1.0" 
      
lazy val `yearanalyser` = (project in file("."))
    .enablePlugins(PlayScala)
    .enablePlugins(AshScriptPlugin)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
    ehcache,
    ws,
    specs2 % Test,
    guice,
    "net.sf.biweekly" % "biweekly" % "0.6.3",
)

scalacOptions ++= Seq("unchecked", "-deprecation")

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

bashScriptTemplateLocation := `yearanalyser`.base / "conf" / "launch-script.sh"

      