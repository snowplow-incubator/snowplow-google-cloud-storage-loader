import sbt._
import Keys._

val scioVersion = "0.7.4"
val scalaMacrosVersion = "2.1.1"
val slf4jVersion = "1.7.25"
val scalatestVersion = "3.0.5"
val mockitoVersion = "2.23.0"

lazy val compilerOptions = Seq(
  "-target:jvm-1.8",
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Xfuture",
  "-Ypartial-unification"
)

lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.snowplowanalytics",
  version := "0.1.0",
  scalaVersion := "2.12.10",
  scalacOptions ++= compilerOptions,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

lazy val paradiseDependency =
  "org.scalamacros" % "paradise" % scalaMacrosVersion cross CrossVersion.full
lazy val macroSettings = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  addCompilerPlugin(paradiseDependency)
)

import com.typesafe.sbt.packager.docker._
dockerRepository := Some("snowplow-docker-registry.bintray.io")
dockerUsername := Some("snowplow")
dockerBaseImage := "snowplow-docker-registry.bintray.io/snowplow/base-debian:0.1.0"
maintainer in Docker := "Snowplow Analytics Ltd. <support@snowplowanalytics.com>"
daemonUser in Docker := "snowplow"

lazy val root: Project = project
  .in(file("."))
  .settings(commonSettings)
  .settings(macroSettings)
  .settings(
    name := "snowplow-google-cloud-storage-loader",
    description := "Snowplow Google Cloud Storage Loader",
    publish / skip := true,
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-core" % scioVersion,
      "com.spotify" %% "scio-test" % scioVersion % Test,
      "org.slf4j" % "slf4j-simple" % slf4jVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
      "org.mockito" % "mockito-core" % mockitoVersion % Test
    )
  )
  .enablePlugins(JavaAppPackaging)

lazy val repl: Project = project
  .in(file(".repl"))
  .settings(commonSettings)
  .settings(macroSettings)
  .settings(
    name := "repl",
    description := "Scio REPL for snowplow-google-cloud-storage-loader",
    libraryDependencies ++= Seq(
      "com.spotify" %% "scio-repl" % scioVersion
    ),
    Compile / mainClass := Some("com.spotify.scio.repl.ScioShell"),
    publish / skip := true
  )
  .dependsOn(root)
