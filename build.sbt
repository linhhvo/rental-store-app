scalaVersion := "3.5.2"
Global / onChangedBuildSource := ReloadOnSourceChanges
libraryDependencies += "org.scalafx" % "scalafx_3" % "23.0.1-R34"
libraryDependencies += "com.lihaoyi" %% "upickle" % "4.1.0"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.11.4"
//libraryDependencies += "org.playframework" %% "play-json" % "3.0.4"
val circeVersion = "0.14.10"

libraryDependencies ++= Seq(
   "io.circe" %% "circe-core",
   "io.circe" %% "circe-generic",
   "io.circe" %% "circe-parser"
   ).map(_ % circeVersion)