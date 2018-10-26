package com.snowplowanalytics

import com.spotify.scio._
import org.apache.beam.sdk.io.{Compression, FileBasedSink, TextIO}
import org.apache.beam.sdk.io.fs.ResourceId
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.options.ValueProvider.NestedValueProvider
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.transforms.windowing.{FixedWindows, Window}
import org.joda.time.Duration

object CloudStorageLoader {
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

  private def getCompression(s: String): Compression = s.trim.toLowerCase match {
    case "bz2" => Compression.BZIP2
    case "gzip" => Compression.GZIP
    case _ => Compression.UNCOMPRESSED
  }
}
