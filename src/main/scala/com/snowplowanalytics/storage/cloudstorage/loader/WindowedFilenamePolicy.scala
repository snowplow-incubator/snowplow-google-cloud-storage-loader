/*
 * Copyright (c) 2018-2018 Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.storage.cloudstorage.loader

import org.apache.beam.sdk.io.DefaultFilenamePolicy
import org.apache.beam.sdk.io.FileBasedSink.{FilenamePolicy, OutputFileHints}
import org.apache.beam.sdk.io.FileSystems
import org.apache.beam.sdk.io.fs.ResolveOptions.StandardResolveOptions
import org.apache.beam.sdk.io.fs.ResourceId
import org.apache.beam.sdk.options.ValueProvider
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider
import org.apache.beam.sdk.transforms.windowing.{BoundedWindow, IntervalWindow, PaneInfo}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Case class providing a policy on how the output files will be named in the output bucket.
 * It supports only windowed files.
 * @param outputDirectory Cloud Storage directory to output to, must end with a /
 * @param outputFilenamePrefix the prefix with which the filenames will be prepended
 * @param shardTemplate the template controlling how shard numbers will be incorporated into
 * filenames
 * @param outputFilenameSuffix the suffix with which the filenames will be appended
 */
final case class WindowedFilenamePolicy(
  outputDirectory: ValueProvider[String],
  outputFilenamePrefix: ValueProvider[String],
  shardTemplate: ValueProvider[String],
  outputFilenameSuffix: ValueProvider[String]
) extends FilenamePolicy {
  /** Generates a filename from window information, fill possible date templates. */
  override def windowedFilename(
    shardNumber: Int,
    numShards: Int,
    window: BoundedWindow,
    paneInfo: PaneInfo,
    outputFileHints: OutputFileHints
  ): ResourceId = {
    val outputFile = resolveWithDateTemplates(outputDirectory, window)
      .resolve(outputFilenamePrefix.get, StandardResolveOptions.RESOLVE_FILE)
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

  /**
   * Fill the date templates with actual time information from the window we are in.
   * @param outputDirectory directory possibly containing date templates
   * @param window time window we are currently in
   * @return a resource id with the date templated in
   */
  private def resolveWithDateTemplates(
    outputDirectory: ValueProvider[String],
    window: BoundedWindow
  ): ResourceId = {
    val outputDir = FileSystems.matchNewResource(outputDirectory.get, isDirectory = true)

    window match {
      case w: IntervalWindow =>
        val windowEndTime = w.end.toDateTime
        val outputPath = dateFormat(windowEndTime, outputDir.toString)
        FileSystems.matchNewResource(outputPath, isDirectory = true)
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

object WindowedFilenamePolicy {
  def apply(
    outputDirectory: String,
    outputFilenamePrefix: String,
    shardTemplate: String,
    outputFilenameSuffix: String
  ): WindowedFilenamePolicy = WindowedFilenamePolicy(
    StaticValueProvider.of(outputDirectory),
    StaticValueProvider.of(outputFilenamePrefix),
    StaticValueProvider.of(shardTemplate),
    StaticValueProvider.of(outputFilenameSuffix)
  )
}
