package com.snowplowanalytics

import org.apache.beam.sdk.options._
import org.apache.beam.sdk.options.Validation.Required
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider

trait Options extends PipelineOptions with StreamingOptions {
  @Description("The Cloud Pub/Sub subscription to read from")
  @Default.String("projects/project/subscriptions/subscription")
  def getInputSubscription: String
  def setInputSubscription(value: String): Unit

  @Description("The Cloud Storage path to output files to, ends with the filenames suffix")
  @Default.String("gs://tmp/subscription-")
  def getOutputPath: String
  def setOutputPath(value: String): Unit

  @Description("The suffix of the filenames written out")
  @Default.String(".bz2")
  def getOutputFilenameSuffix: String
  def setOutputFilenameSuffix(value: String): Unit

  @Description("The window duration in minutes, defaults to 5")
  @Default.Integer(5)
  def getWindowDuration: Int
  def setWindowDuration(value: Int): Unit

  @Description("The maximum number of output shards produced when writing")
  @Default.Integer(1)
  def getNumShards: Int
  def setNumShards(value: Int): Unit
}
