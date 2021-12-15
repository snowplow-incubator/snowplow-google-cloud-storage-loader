import sbt._
import Keys._

val scioVersion = "0.11.1"
val beamVersion = "2.19.0"
val scalaMacrosVersion = "2.1.1"
val slf4jVersion = "1.7.29"
val scalatestVersion = "3.1.0"
val scalatestPlusVersion = s"$scalatestVersion.0"
val circe = "0.11.2"
val igluCore = "0.5.1"

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
  version := "0.3.1",
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
dockerUsername := Some("snowplow")
dockerBaseImage := "snowplow/k8s-dataflow:0.1.0"
Docker / maintainer := "Snowplow Analytics Ltd. <support@snowplowanalytics.com>"
Docker / daemonUser := "snowplow"
dockerCommands := dockerCommands.value.map{
  case ExecCmd("ENTRYPOINT", args) => ExecCmd("ENTRYPOINT", "docker-entrypoint.sh", args)
  case e => e
}

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
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
      "org.scalatestplus" %% "mockito-3-2" % scalatestPlusVersion % Test
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
