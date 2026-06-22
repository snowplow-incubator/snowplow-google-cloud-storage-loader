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
    val scio          = "0.15.7"
    val beam          = "2.74.0"
    val scalaMacros   = "2.1.1"
    val slf4j         = "1.7.36"
    val scalatest     = "3.2.10"
    val scalatestPlus = "3.1.2.0"
    val circe         = "0.14.3"
    val igluCore      = "1.1.3"
    val jackson       = "2.18.7" // An override, to mitigate a CVE
    val nettyCodec    = "4.1.135.Final" // An override, to mitigate a CVE
    val opentelemetry = "1.62.0" // An override, to mitigate a CVE
    val wire          = "6.3.0"  // An override, to mitigate a CVE (wire-runtime)
    val paradise      = "2.1.1"
  }

  // wire-runtime / wire-runtime-jvm (pulled transitively via Beam's protobuf
  // extension -> apicurio) carry CVEs only fixed in the 6.x line. wire requires
  // a single version across its modules, so every wire artifact in the graph is
  // forced to V.wire. wire-grpc-server / wire-grpc-server-generator have no 6.x
  // release and are no longer pulled once wire-grpc-client is at 6.x.
  val wireOverrides = Seq(
    "wire-compiler", "wire-grpc-client-jvm", "wire-java-generator", "wire-kotlin-generator",
    "wire-runtime", "wire-runtime-jvm", "wire-schema", "wire-schema-jvm", "wire-swift-generator"
  ).map(a => "com.squareup.wire" % a % V.wire)

  object Libraries {
    val beam        = "org.apache.beam"              % "beam-runners-google-cloud-dataflow-java" % V.beam
    val scioCore    = "com.spotify"                  %% "scio-core"                              % V.scio
    val scioRepl    = "com.spotify"                  %% "scio-repl"                              % V.scio
    val circe       = "io.circe"                     %% "circe-parser"                           % V.circe
    val igluCore    = "com.snowplowanalytics"        %% "iglu-core-circe"                        % V.igluCore
    val slf4j       = "org.slf4j"                    %  "slf4j-simple"                           % V.slf4j
    val paradise    = "org.scalamacros"              %  "paradise"                               % V.paradise
    val jackson     = "com.fasterxml.jackson.module" %% "jackson-module-scala"                   % V.jackson
    val nettyCodec  = "io.netty"                     %  "netty-codec-http2"                      % V.nettyCodec
    val nettyProxy  = "io.netty"                     %  "netty-handler-proxy"                    % V.nettyCodec
    val opentelemetry  = "io.opentelemetry"          %  "opentelemetry-api"                      % V.opentelemetry
    val reflect     = "org.scala-lang"               %  "scala-reflect"

    // Test
    val scioTest  = "com.spotify"       %% "scio-test"   % V.scio          % Test
    val scalatest = "org.scalatest"     %% "scalatest"   % V.scalatest     % Test
    val mockito   = "org.scalatestplus" %% "mockito-3-2" % V.scalatestPlus % Test
  }

}
