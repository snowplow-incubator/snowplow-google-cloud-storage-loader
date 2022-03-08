import sbt._
import Keys._

val scioVersion = "0.11.5"
val beamVersion = "2.36.0"
val scalaMacrosVersion = "2.1.1"
val slf4jVersion = "1.7.29"
val scalatestVersion = "3.2.10"
val scalatestPlusVersion = "3.1.2.0"
val circe = "0.14.1"
val igluCore = "1.0.1"
val netty = "4.1.68.Final" // An override, to mitigate a CVE

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
dockerRepository := Some("snowplow")
dockerBaseImage := "eclipse-temurin:8-jre-focal"
Docker / packageName := "snowplow-google-cloud-storage-loader"
Docker / maintainer := "Snowplow Analytics Ltd. <support@snowplowanalytics.com>"
Docker / daemonUser := "snowplow"
Docker / defaultLinuxInstallLocation := "/home/snowplow"
dockerUpdateLatest := true

import sbtdynver.DynVerPlugin.autoImport._
ThisBuild / dynverVTagPrefix := false // Otherwise git tags required to have v-prefix
ThisBuild / dynverSeparator := "-" // to be compatible with docker

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
      "org.apache.beam" % "beam-runners-google-cloud-dataflow-java" % beamVersion,
      "io.circe" %% "circe-parser" % circe,
      "com.snowplowanalytics" %% "iglu-core-circe" % igluCore,
      "org.slf4j" % "slf4j-simple" % slf4jVersion,
      "io.netty" % "netty-codec" % netty,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
      "org.scalatestplus" %% "mockito-3-2" % scalatestPlusVersion % Test
    ),
    resolvers += "Confluent Repository" at "https://packages.confluent.io/maven/"
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
