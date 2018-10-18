package com.snowplowanalytics

import org.apache.beam.sdk.io.DefaultFilenamePolicy
import org.apache.beam.sdk.io.FileBasedSink.{FilenamePolicy, OutputFileHints}
import org.apache.beam.sdk.io.FileSystems
import org.apache.beam.sdk.io.fs.ResolveOptions.StandardResolveOptions
import org.apache.beam.sdk.io.fs.ResourceId
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider
import org.apache.beam.sdk.transforms.windowing.{BoundedWindow, IntervalWindow, PaneInfo}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

final case class WindowedFilenamePolicy(
  outputDirectory: String,
  outputFilenamePrefix: String,
  shardTemplate: String,
  outputFilenameSuffix: String
) extends FilenamePolicy {
  override def windowedFilename(
    shardNumber: Int,
    numShards: Int,
    window: BoundedWindow,
    paneInfo: PaneInfo,
    outputFileHints: OutputFileHints
  ): ResourceId = {
    val outputFile = resolveWithDateTemplates(outputDirectory, window)
      .resolve(outputFilenamePrefix, StandardResolveOptions.RESOLVE_FILE)
    val policy = DefaultFilenamePolicy.fromStandardParameters(
      StaticValueProvider.of(outputFile), shardTemplate, outputFilenameSuffix, windowedWrites = true)
    policy.windowedFilename(shardNumber, numShards, window, paneInfo, outputFileHints)
  }

  override def unwindowedFilename(
    shardNumber: Int,
    numShards: Int,
    outputFileHints: OutputFileHints
  ): ResourceId = throw new UnsupportedOperationException("This policy only supports windowed files")

  private def resolveWithDateTemplates(
    outputDirectory: String,
    window: BoundedWindow
  ): ResourceId = {
    val outputDir = FileSystems.matchNewResource(outputDirectory, isDirectory = true)

    window match {
      case w: IntervalWindow =>
        val windowEndTime = w.end.toDateTime
        val outputPath = dateFormat(windowEndTime)(outputDir.toString)
        FileSystems.matchNewResource(outputPath, isDirectory = true)
      case _ => outputDir
    }
  }

  private def dateFormat(t: DateTime): String => String =
    ((s: String) => s.replace("YYYY", DateTimeFormat.forPattern("YYYY").print(t))) andThen
      ((s: String) => s.replace("MM", DateTimeFormat.forPattern("MM").print(t))) andThen
      ((s: String) => s.replace("DD", DateTimeFormat.forPattern("DD").print(t)))
}
