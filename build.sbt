name := """tubelytics"""
organization := "TubeLytics"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"

libraryDependencies += guice
libraryDependencies += "com.google.apis" % "google-api-services-youtube" % "v3-rev222-1.25.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.0"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.13.0"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-annotations" % "2.13.0"
