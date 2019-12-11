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

import org.apache.beam.sdk.io.FileBasedSink.FilenamePolicy
import org.apache.beam.sdk.io.{DefaultFilenamePolicy, FileSystems, Compression}
import org.apache.beam.sdk.io.FileBasedSink.OutputFileHints
import org.apache.beam.sdk.io.FileIO.Write.FileNaming
import org.apache.beam.sdk.io.fs.ResolveOptions.StandardResolveOptions
import org.apache.beam.sdk.io.fs.ResourceId
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider
import org.apache.beam.sdk.options.ValueProvider
import org.apache.beam.sdk.transforms.windowing.{IntervalWindow, PaneInfo, BoundedWindow}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory

/**
 * Case class providing a policy on how the output files will be named in the output bucket.
 * It supports only windowed files.
 * @param outputDirectory Cloud Storage directory to output to
 * @param outputFilenamePrefix the prefix with which the filenames will be prepended
 * @param shardTemplate the template controlling how shard numbers will be incorporated into
 * filenames
 * @param outputFilenameSuffix the suffix with which the filenames will be appended
 * @param dateTemplate the template controlling how date directory will be constructed
 * @param rowType row type to create directory which rows with same type to put under
 */
final case class WindowedFilenamePolicy(
  outputDirectory: Option[ValueProvider[String]],
  outputFilenamePrefix: ValueProvider[String],
  shardTemplate: ValueProvider[String],
  outputFilenameSuffix: ValueProvider[String],
  dateTemplate: ValueProvider[String],
  rowType: Option[String]
) extends FilenamePolicy with FileNaming {
  private val logger = LoggerFactory.getLogger(this.getClass)
  logger.info(s"Using a WindowedFilenamePolicy for $rowType")

  /** Generates a filename from window information, fill possible date templates. */
  override def windowedFilename(
    shardNumber: Int,
    numShards: Int,
    window: BoundedWindow,
    paneInfo: PaneInfo,
    outputFileHints: OutputFileHints
  ): ResourceId = {
    val outputFile = resolveWithDateTemplates(
      outputDirectory.map(_.get),
      rowType,
      window,
      dateTemplate.get
    ).resolve(outputFilenamePrefix.get, StandardResolveOptions.RESOLVE_FILE)
    logger.info(s"Policy filename: ${outputFile.getFilename()}")
    val policy = DefaultFilenamePolicy.fromStandardParameters(
      StaticValueProvider.of(outputFile),
      shardTemplate.get,
      outputFilenameSuffix.get,
      windowedWrites = true
    )
    policy.windowedFilename(shardNumber, numShards, window, paneInfo, outputFileHints)
  }

  /** Throws an UnsupportedOperationException since this policy is for windowed filenames. */
  override def unwindowedFilename(
    shardNumber: Int,
    numShards: Int,
    outputFileHints: OutputFileHints
  ): ResourceId =
    throw new UnsupportedOperationException("This policy only supports windowed files")

  override def getFilename(
    window: BoundedWindow,
    pane: PaneInfo,
    numShards: Int,
    shardIndex: Int,
    compression: Compression
  ): String = {
    windowedFilename(shardIndex, numShards, window, pane, new OutputFileHints {
      override def getMimeType: String = null
      override def getSuggestedFilenameSuffix: String = ""
    }).toString.stripPrefix("/") ++ compression.getSuggestedSuffix
  }
  /**
   * Fill the date templates with actual time information from the window we are in.
   * @param outputDirectory Cloud Storage directory to output to
   * @param rowType row type to create directory which rows with same type to put under
   * @param window time window we are currently in
   * @param dateTemplate the template controlling how date directory will be constructed
   * @return a resource id with the date templated in
   */
  private def resolveWithDateTemplates(
    outputDirectory: Option[String],
    rowType: Option[String],
    window: BoundedWindow,
    dateTemplate: String
  ): ResourceId = {
    val outputDir = (outputDirectory, rowType) match {
      case (Some(o), Some(t)) => FileSystems.matchNewResource(o, isDirectory = true)
        .resolve(t, StandardResolveOptions.RESOLVE_DIRECTORY)
      case (Some(o), None) => FileSystems.matchNewResource(o, isDirectory = true)
      case (None, Some(t)) => FileSystems.matchNewResource(t, isDirectory = true)
      case (None, None) => FileSystems.matchNewResource("", isDirectory = true)
    }
    window match {
      case w: IntervalWindow =>
        val windowEndTime = w.end.toDateTime
        val dateStr = dateFormat(windowEndTime, dateTemplate)
        outputDir.resolve(dateStr, StandardResolveOptions.RESOLVE_DIRECTORY)
      case _ => outputDir
    }
  }

  /**
   * Successively applies time formats to the template with values from the supplied date time.
   * Supported templates are `YYYY`, `MM`, `dd` and `HH`.
   * @param time date time to template the values with
   * @param template string in which the templates are to be replaced with time values
   * @return a templated string
   */
  private def dateFormat(time: DateTime, template: String): String =
    List("YYYY", "MM", "dd", "HH")
      .map { pattern =>
        (s: String) => s.replace(pattern, DateTimeFormat.forPattern(pattern).print(time))
      }
      .fold(identity[String] _)(_ andThen _)(template)
}
