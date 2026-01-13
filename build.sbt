name := "YearAnalyser"
 
version := "1.0" 
      
lazy val `yearanalyser` = (project in file("."))
    .enablePlugins(PlayScala)
    .enablePlugins(AshScriptPlugin)
      
scalaVersion := "3.8.0"

turbo := true

libraryDependencies ++= Seq(
    caffeine,
    ws,
    specs2 % Test,
    "net.sf.biweekly" % "biweekly" % "0.6.8",
)

scalacOptions ++= Seq("unchecked", "-deprecation")

Compile / doc / sources := Seq.empty

Compile / packageDoc / publishArtifact := false

bashScriptTemplateLocation := `yearanalyser`.base / "conf" / "launch-script.sh"

      
