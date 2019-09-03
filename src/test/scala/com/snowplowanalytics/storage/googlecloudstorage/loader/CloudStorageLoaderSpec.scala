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

import com.spotify.scio.io.CustomIO
import com.spotify.scio.testing._
import com.spotify.scio.io.CustomIO
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider

class CloudStorageLoaderSpec extends PipelineSpec {
  val expected = (1 to 10).map(_.toString)
  val errorDir = "err_dir"

  "CloudStorageLoader" should "output a file" in {
    val sub = "projects/project/subscriptions/sub"
    JobTest[CloudStorageLoader.type]
      .args(s"--inputSubscription=${sub}", "--outputDirectory=gs://out-dir/", "--partitionBySchema=true")
      .input(CustomIO[String]("input"), expected)
      .output(CustomIO[String]("output"))(_ should containInAnyOrder(expected))
      .run()
  }

  "getRowType" should "return parsing error when given json is not valid" in {
    val invalidJson =
      """
        |{
        | "key": "value
        |}
      """.stripMargin
    CloudStorageLoader.getRowType(invalidJson, StaticValueProvider.of(errorDir)) should equal(RowType.PartitionError(errorDir))
  }

  "getRowType" should "return NonSelfDescribing as RowType when given json is not self describing json" in {
    val nonSelfDescribingJson =
      """
        |{
        | "key": "value"
        |}
      """.stripMargin
    CloudStorageLoader.getRowType(nonSelfDescribingJson, StaticValueProvider.of(errorDir)) should equal(RowType.PartitionError(errorDir))
  }

  "getRowType" should "return SelfDescribing as RowType when given json is self describing json" in {
    val json =
      """
        |{
        |  "schema": "iglu:com.acme2/example1/jsonschema/2-0-1",
        |  "data": "data3"
        |}
      """.stripMargin
    CloudStorageLoader.getRowType(json, StaticValueProvider.of(errorDir)).getName should equal("com.acme2.example1")
  }
}
