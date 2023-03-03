name := "YearAnalyser"
 
version := "1.0" 
      
lazy val `yearanalyser` = (project in file("."))
    .enablePlugins(PlayScala)
    .enablePlugins(AshScriptPlugin)
      
scalaVersion := "2.13.10"

turbo := true

libraryDependencies ++= Seq(
    caffeine,
    ws,
    specs2 % Test,
    "net.sf.biweekly" % "biweekly" % "0.6.7",
)

scalacOptions ++= Seq("unchecked", "-deprecation")

Compile / doc / sources := Seq.empty

Compile / packageDoc / publishArtifact := false

bashScriptTemplateLocation := `yearanalyser`.base / "conf" / "launch-script.sh"

      
