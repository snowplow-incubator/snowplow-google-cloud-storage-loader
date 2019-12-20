/*
 * Copyright (c) 2018-2019 Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.storage.googlecloudstorage.loader

import com.spotify.scio._
import org.apache.beam.sdk.io.{Compression, FileBasedSink, TextIO}
import org.apache.beam.sdk.io.fs.ResourceId
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.runners.dataflow.options._
import org.apache.beam.sdk.options.ValueProvider.NestedValueProvider
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.transforms.windowing.{FixedWindows, Window}
import org.joda.time.Duration
import scala.collection.JavaConverters._

/** Dataflow job outputting the content from a Pubsub subscription to a Cloud Storage bucket. */
object CloudStorageLoader {
  def main(args: Array[String]): Unit = {
    PipelineOptionsFactory.register(classOf[Options])
    val options = PipelineOptionsFactory
      .fromArgs(args: _*)
      .withValidation
      .as(classOf[Options])

    // val cfg: java.util.Map[String, java.lang.Object] = Map("APICurated" -> true).mapValues(_.asInstanceOf[java.lang.Object]).asJava
    import java.util.stream._
    val cfg = new java.util.HashMap[String, Boolean](){{ put("APICurated", true) }}.asInstanceOf[DataflowProfilingOptions.DataflowProfilingAgentConfiguration]
    options.setProfilingAgentConfiguration(cfg)
    options.setStreaming(true)

    run(options)
  }

  def run(options: Options): Unit = {
    val sc = ScioContext(options)

    val inputIO = PubsubIO.readStrings().fromSubscription(options.getInputSubscription)
    val outputIO = TextIO.write()
        .withWindowedWrites
        .withNumShards(options.getNumShards)
        .withWritableByteChannelFactory(
          FileBasedSink.CompressionType.fromCanonical(getCompression(options.getCompression)))
        .withTempDirectory(NestedValueProvider.of(
          options.getOutputDirectory,
          new SerializableFunction[String, ResourceId] {
            def apply(input: String): ResourceId =
              FileBasedSink.convertToFileResourceIfPossible(input)
          }
        ))
        .to(WindowedFilenamePolicy(
          options.getOutputDirectory,
          options.getOutputFilenamePrefix,
          options.getShardTemplate,
          options.getOutputFilenameSuffix
        ))


    sc
      .customInput("input", inputIO)
      .applyTransform(
        Window.into(FixedWindows.of(Duration.standardMinutes(options.getWindowDuration.toLong)))
      ).withName("windowed")
      .saveAsCustomOutput("output", outputIO)

    sc.close()
  }

  /**
   * Tries to parse a string as a [[Compression]], falls back to uncompressed.
   * @param compression string to parse
   * @return the parsed compression
   */
  private def getCompression(compression: String): Compression =
    compression.trim.toLowerCase match {
      case "bz2" => Compression.BZIP2
      case "gzip" => Compression.GZIP
      case _ => Compression.UNCOMPRESSED
    }
}
