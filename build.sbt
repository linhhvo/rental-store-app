scalaVersion                                         := "3.5.2"
Global / onChangedBuildSource                        := ReloadOnSourceChanges
libraryDependencies += "org.scalafx"                  % "scalafx_3" % "23.0.1-R34"
libraryDependencies += "com.lihaoyi" %% "upickle"     % "4.1.0"
libraryDependencies += "com.lihaoyi" %% "os-lib"      % "0.11.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19"    % "test"