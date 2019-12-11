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

import org.apache.beam.sdk.options._
import org.apache.beam.sdk.options.Validation.Required

/** Trait regrouping the loader's configuration. */
trait Options extends PipelineOptions with StreamingOptions {
  @Description("The Cloud Pub/Sub subscription to read from, formatted as projects/[PROJECT]/subscriptions/[SUB]")
  @Required
  def getInputSubscription: ValueProvider[String]
  def setInputSubscription(value: ValueProvider[String]): Unit

  @Description("The Cloud Storage directory to output files to, ends with /")
  @Required
  def getOutputDirectory: ValueProvider[String]
  def setOutputDirectory(value: ValueProvider[String]): Unit

  @Description("The prefix to prepend to filenames")
  @Default.String("output")
  @Required
  def getOutputFilenamePrefix: ValueProvider[String]
  def setOutputFilenamePrefix(value: ValueProvider[String]): Unit

  @Description("Date format")
  @Default.String("YYYY/MM/dd/HH")
  def getDateFormat: ValueProvider[String]
  def setDateFormat(value: ValueProvider[String]): Unit

  @Description("The shard template which will be part of the filennams")
  @Default.String("-W-P-SSSSS-of-NNNNN")
  def getShardTemplate: ValueProvider[String]
  def setShardTemplate(value: ValueProvider[String]): Unit

  @Description("The suffix of the filenames written out")
  @Default.String(".txt")
  def getOutputFilenameSuffix: ValueProvider[String]
  def setOutputFilenameSuffix(value: ValueProvider[String]): Unit

  @Description("The window duration in minutes")
  @Default.Integer(5)
  def getWindowDuration: Int
  def setWindowDuration(value: Int): Unit

  @Description("The compression used (gzip, bz2 or none), bz2 can't be loaded into BigQuery")
  @Default.String("none")
  def getCompression: String
  def setCompression(value: String): Unit

  @Description("The maximum number of output shards produced when writing. Default: 0 - let runner manage")
  @Default.Integer(0)
  def getNumShards: Int
  def setNumShards(value: Int): Unit

  @Description("Partition output according to schema")
  @Default.Boolean(false)
  def getPartitionBySchema(): Boolean
  def setPartitionBySchema(value: Boolean): Unit

  @Description("The directory for rows which gives error during type partition")
  @Default.String("")
  def getPartitionErrorDirectory(): ValueProvider[String]
  def setPartitionErrorDirectory(value: ValueProvider[String]): Unit
}

