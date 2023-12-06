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

object Dependencies {

  object V {
    val scio          = "0.13.6"
    val beam          = "2.41.0"
    val scalaMacros   = "2.1.1"
    val slf4j         = "1.7.36"
    val scalatest     = "3.2.10"
    val scalatestPlus = "3.1.2.0"
    val circe         = "0.14.1"
    val igluCore      = "1.0.1"
    val jackson       = "2.13.4.2" // An override, to mitigate a CVE
    val googleOauth   = "1.34.0" // An override, to mitigate a CVE
    val guava         = "31.1-jre" // An override, to mitigate a CVE
    val snakeYaml     = "1.33" // An override, to mitigate a CVE
    val paradise      = "2.1.1"
  }

  object Libraries {
    val scioCore = ("com.spotify" %% "scio-core" % V.scio)
        .exclude("org.codehaus.jackson", "jackson-mapper-asl") // address security vulnerabilities
        .exclude("org.apache.beam", "beam-sdks-java-extensions-sql") // address security vulnerabilities
    val scioRepl = ("com.spotify" %% "scio-repl" % V.scio)
        .exclude("org.codehaus.jackson", "jackson-mapper-asl") // address security vulnerabilities
        .exclude("org.apache.beam", "beam-sdks-java-extensions-sql") // address security vulnerabilities
    val beam = ("org.apache.beam" % "beam-runners-google-cloud-dataflow-java" % V.beam)
        .exclude("org.codehaus.jackson", "jackson-mapper-asl") // address security vulnerabilities
        .exclude("org.apache.beam", "beam-sdks-java-extensions-sql") // address security vulnerabilities

    val circe       = "io.circe"                   %% "circe-parser"       % V.circe
    val igluCore    = "com.snowplowanalytics"      %% "iglu-core-circe"    % V.igluCore
    val slf4j       = "org.slf4j"                  %  "slf4j-simple"       % V.slf4j
    val jackson     = "com.fasterxml.jackson.core" %  "jackson-databind"   % V.jackson
    val paradise    = "org.scalamacros"            %  "paradise"           % V.paradise
    val googleOauth = "com.google.oauth-client"    % "google-oauth-client" % V.googleOauth
    val guava       = "com.google.guava"           % "guava"               % V.guava
    val snakeYaml   = "org.yaml"                   %  "snakeyaml"          % V.snakeYaml
    val reflect     = "org.scala-lang"             %  "scala-reflect"

    // Test
    val scioTest  = "com.spotify"       %% "scio-test"   % V.scio          % Test
    val scalatest = "org.scalatest"     %% "scalatest"   % V.scalatest     % Test
    val mockito   = "org.scalatestplus" %% "mockito-3-2" % V.scalatestPlus % Test
  }

}
