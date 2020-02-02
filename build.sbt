name := "YearAnalyser"
 
version := "1.0" 
      
lazy val `yearanalyser` = (project in file("."))
    .enablePlugins(PlayScala)
    .enablePlugins(AshScriptPlugin)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.13.1"

turbo := true

libraryDependencies ++= Seq(
    caffeine,
    ws,
    specs2 % Test,
    "net.sf.biweekly" % "biweekly" % "0.6.3",
)

scalacOptions ++= Seq("unchecked", "-deprecation")

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

bashScriptTemplateLocation := `yearanalyser`.base / "conf" / "launch-script.sh"

      