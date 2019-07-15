name := "YearAnalyser"
 
version := "1.0" 
      
lazy val `yearanalyser` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
    ehcache,
    ws,
    specs2 % Test,
    guice,
    "net.sf.biweekly" % "biweekly" % "0.6.3",
)

      