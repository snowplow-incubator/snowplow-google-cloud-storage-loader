package com.snowplowanalytics

import com.spotify.scio._
import org.apache.beam.sdk.io.{Compression, FileBasedSink, TextIO}
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.values.PDone
import org.joda.time.Duration

object CloudStorageLoader {
  def main(args: Array[String]): Unit = {
    val options = PipelineOptionsFactory
      .fromArgs(args: _*)
      .withValidation
      .as(classOf[Options])

    options.setStreaming(true)

    run(options)
  }

  def run(options: Options): Unit = {
    val sc = ScioContext(options)

    val input = sc.pubsubSubscription[String](options.getInputSubscription).withName("input")

    val windowed = input
      .withFixedWindows(Duration.standardMinutes(options.getWindowDuration)).withName("windowed")

    windowed
      .saveAsCustomOutput("output", TextIO.write()
        .withWindowedWrites
        .withNumShards(options.getNumShards)
        .withSuffix(options.getOutputFilenameSuffix)
        .withShardNameTemplate("SSSS-NNNN")
        .withWritableByteChannelFactory(
          FileBasedSink.CompressionType.fromCanonical(Compression.BZIP2))
        .to(options.getOutputPath)
      )

    sc.close()
  }
}
