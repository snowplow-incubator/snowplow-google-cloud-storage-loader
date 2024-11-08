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
    val scio          = "0.14.8"
    val beam          = "2.60.0"
    val scalaMacros   = "2.1.1"
    val slf4j         = "1.7.36"
    val scalatest     = "3.2.10"
    val scalatestPlus = "3.1.2.0"
    val circe         = "0.14.3"
    val igluCore      = "1.1.3"
    val jackson       = "2.17.2" // An override, to mitigate a CVE
    val nettyCodec    = "4.1.108.Final" // An override, to mitigate a CVE
    val avro          = "1.11.4" // An override, to mitigate a CVE
    val protobuf      = "3.25.5" // An override, to mitigate a CVE
    val kaml          = "0.53.0" // An override, to mitigate a CVE
    val paradise      = "2.1.1"
  }

  object Libraries {
    val beam        = "org.apache.beam"              % "beam-runners-google-cloud-dataflow-java" % V.beam
    val scioCore    = "com.spotify"                  %% "scio-core"                              % V.scio
    val scioRepl    = "com.spotify"                  %% "scio-repl"                              % V.scio
    val circe       = "io.circe"                     %% "circe-parser"                           % V.circe
    val igluCore    = "com.snowplowanalytics"        %% "iglu-core-circe"                        % V.igluCore
    val slf4j       = "org.slf4j"                    %  "slf4j-simple"                           % V.slf4j
    val paradise    = "org.scalamacros"              %  "paradise"                               % V.paradise
    val jackson     = "com.fasterxml.jackson.module" %% "jackson-module-scala"                   % V.jackson
    val avro        = "org.apache.avro"              %  "avro"                                   % V.avro
    val protobuf    = "com.google.protobuf"          %  "protobuf-java-util"                     % V.protobuf
    val nettyCodec  = "io.netty"                     %  "netty-codec-http2"                      % V.nettyCodec
    val kaml        = "com.charleskorn.kaml"         %  "kaml"                                   % V.kaml
    val reflect     = "org.scala-lang"               %  "scala-reflect"

    // Test
    val scioTest  = "com.spotify"       %% "scio-test"   % V.scio          % Test
    val scalatest = "org.scalatest"     %% "scalatest"   % V.scalatest     % Test
    val mockito   = "org.scalatestplus" %% "mockito-3-2" % V.scalatestPlus % Test
  }

}
