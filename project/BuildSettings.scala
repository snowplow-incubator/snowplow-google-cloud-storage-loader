/*
 * Copyright (c) 2018-2022 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and
 * limitations there under.
 */
import sbt._
import sbt.Keys._

import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport._
import sbtdynver.DynVerPlugin.autoImport._

object BuildSettings {

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

  lazy val appSettings = Seq(
      name := "snowplow-google-cloud-storage-loader",
      Docker / packageName := "snowplow-google-cloud-storage-loader",
      description := "Snowplow Google Cloud Storage Loader",
      publish / skip := true,
      ThisBuild / dynverVTagPrefix := false, // Otherwise git tags required to have v-prefix
      ThisBuild / dynverSeparator := "-", // to be compatible with docker
      libraryDependencies ++= Seq(
        Dependencies.Libraries.scioCore,
        Dependencies.Libraries.beam,
        Dependencies.Libraries.circe,
        Dependencies.Libraries.igluCore,
        Dependencies.Libraries.slf4j,
        Dependencies.Libraries.jackson,
        Dependencies.Libraries.avro,
        Dependencies.Libraries.protobuf,
        Dependencies.Libraries.nettyCodec,
        Dependencies.Libraries.kaml,
        Dependencies.Libraries.scioTest,
        Dependencies.Libraries.scalatest,
        Dependencies.Libraries.mockito
      ),
      resolvers += "Confluent Repository" at "https://packages.confluent.io/maven/"
    )

  lazy val replSettings = Seq(
      name := "repl",
      description := "Scio REPL for snowplow-google-cloud-storage-loader",
      libraryDependencies ++= Seq(Dependencies.Libraries.scioRepl),
      Compile / mainClass := Some("com.spotify.scio.repl.ScioShell"),
    )

  lazy val macroSettings = Seq(
    libraryDependencies += Dependencies.Libraries.reflect % scalaVersion.value,
    addCompilerPlugin(Dependencies.Libraries.paradise cross CrossVersion.full)
  )
}
