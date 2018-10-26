package com.snowplowanalytics

import com.spotify.scio.testing._

class CloudStorageLoaderSpec extends PipelineSpec {
  val expected = (1 to 10).map(_.toString)

  "CloudStorageLoader" should "output a file" in {
    val sub = "projects/project/subscriptions/sub"
    JobTest[CloudStorageLoader.type]
      .args(s"--inputSubscription=${sub}", "--outputDirectory=gs://out-dir/")
      .input(CustomIO[String]("input"), expected)
      .output(CustomIO[String]("output"))(_ should containInAnyOrder(expected))
      .run()
  }
}
