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

import io.circe.parser.parse

import com.spotify.scio._

import org.joda.time.Duration
import org.slf4j.LoggerFactory

import org.apache.beam.sdk.coders.StringUtf8Coder
import org.apache.beam.sdk.io.FileIO.Write.FileNaming
import org.apache.beam.sdk.io.{Compression, FileBasedSink, FileIO, TextIO}
import org.apache.beam.sdk.io.fs.ResourceId
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO
import org.apache.beam.sdk.options.{ValueProvider, PipelineOptionsFactory}
import org.apache.beam.sdk.options.ValueProvider.NestedValueProvider
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.transforms.windowing.{FixedWindows, Window}

import com.snowplowanalytics.iglu.core._
import com.snowplowanalytics.iglu.core.circe.implicits._

/** Dataflow job outputting the content from a Pubsub subscription to a Cloud Storage bucket. */
object CloudStorageLoader {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    PipelineOptionsFactory.register(classOf[Options])
    val options = PipelineOptionsFactory
      .fromArgs(args: _*)
      .withValidation
      .as(classOf[Options])

    options.setStreaming(true)

    run(options)
  }

  def run(options: Options): Unit = {
    val sc = ScioContext(options)

    val outputDirectory = options.getOutputDirectory
    val outputFileNamePrefix = options.getOutputFilenamePrefix
    val shardTemplate = options.getShardTemplate
    val outputFilenameSuffix = options.getOutputFilenameSuffix
    val dateFormat = options.getDateFormat
    val partitionErrorDirectory = options.getPartitionErrorDirectory()

    val inputIO = PubsubIO.readStrings().fromSubscription(options.getInputSubscription)

    val input  = sc
      .customInput("input", inputIO)
      .applyTransform(
        Window.into(FixedWindows.of(Duration.standardMinutes(options.getWindowDuration.toLong)))
      ).withName("windowed")

    if (options.getPartitionBySchema()) {
      // Partition output according to row type
      logger.debug(s"Partitioning: ${options.getPartitionBySchema()}")
      val outputDynamic = FileIO.writeDynamic[String, String]()
        .by(getRowType(_, partitionErrorDirectory).getName)
        .via(TextIO.sink())
        .to(outputDirectory.get())
        .withNumShards(options.getNumShards)
        .withCompression(getCompression(options.getCompression))
        .withDestinationCoder(StringUtf8Coder.of())
        .withNaming(new SerializableFunction[String, FileNaming] {
          // Create FileNaming for partition of window which
          // partitioned according to row type
          override def apply(rowType: String): FileNaming ={
            logger.debug(s"WindowFileNamePolicy(None, $outputFileNamePrefix, $shardTemplate, $outputFilenameSuffix, $dateFormat, Some($rowType))")
            WindowedFilenamePolicy(
              None,
              outputFileNamePrefix,
              shardTemplate,
              outputFilenameSuffix,
              dateFormat,
              Some(rowType)
            )}
        })
      input.saveAsCustomOutput("output", outputDynamic)
    } else {
      // Output to same directory without partitioning
      // according to row type
      logger.debug(s"Not partitioning: ${options.getPartitionBySchema()}")
      val outputIO = TextIO.write()
        .withWindowedWrites
        .withNumShards(options.getNumShards)
        .withWritableByteChannelFactory(
          FileBasedSink.CompressionType.fromCanonical(getCompression(options.getCompression)))
        .withTempDirectory(NestedValueProvider.of(
          outputDirectory,
          new SerializableFunction[String, ResourceId] {
            def apply(input: String): ResourceId =
              FileBasedSink.convertToFileResourceIfPossible(input)
          }
        ))
        .to(WindowedFilenamePolicy(
          Some(outputDirectory),
          outputFileNamePrefix,
          shardTemplate,
          outputFilenameSuffix,
          dateFormat,
          None
        ))
      input.saveAsCustomOutput("output", outputIO)
    }

    sc.close()
  }

  /**
    * Find type of the row according to its schema key
    * @param row string to find the type of it
    * @return row type of given string
    */
  private[loader] def getRowType(row: String, partitionErrorDir: ValueProvider[String]): RowType =
    parse(row) match {
      case Left(_) => RowType.PartitionError(partitionErrorDir.get)
      case Right(json) => SchemaKey.extract(json).fold(
        _ => RowType.PartitionError(partitionErrorDir.get),
        k => RowType.SelfDescribing(k)
      )
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
