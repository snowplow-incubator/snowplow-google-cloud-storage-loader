package com.snowplowanalytics

import org.apache.beam.sdk.options._
import org.apache.beam.sdk.options.Validation.Required

trait Options extends PipelineOptions with StreamingOptions {
  @Description("The Cloud Pub/Sub subscription to read from")
  @Required
  def getInputSubscription: ValueProvider[String]
  def setInputSubscription(value: ValueProvider[String]): Unit

  @Description("The Cloud Storage directory to output files to, ends with /")
  @Required
  def getOutputDirectory: ValueProvider[String]
  def setOutputDirectory(value: ValueProvider[String]): Unit

  @Description("The Cloud Storage prefix to output files to")
  @Default.String("output")
  @Required
  def getOutputFilenamePrefix: ValueProvider[String]
  def setOutputFilenamePrefix(value: ValueProvider[String]): Unit

  @Description("The shard template which will be part of the filennams")
  @Default.String("-W-P-SSSSS-of-NNNNN")
  def getShardTemplate: ValueProvider[String]
  def setShardTemplate(value: ValueProvider[String]): Unit

  @Description("The suffix of the filenames written out")
  @Default.String(".txt")
  def getOutputFilenameSuffix: ValueProvider[String]
  def setOutputFilenameSuffix(value: ValueProvider[String]): Unit

  @Description("The window duration in minutes, defaults to 5")
  @Default.Integer(5)
  def getWindowDuration: Int
  def setWindowDuration(value: Int): Unit

  @Description("The compression used (gzip, bz2 or none), bz2 can't be loaded into BigQuery")
  @Default.String("none")
  def getCompression: String
  def setCompression(value: String): Unit

  @Description("The maximum number of output shards produced when writing")
  @Default.Integer(1)
  def getNumShards: Int
  def setNumShards(value: Int): Unit
}
