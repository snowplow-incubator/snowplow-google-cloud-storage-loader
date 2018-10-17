package com.snowplowanalytics

import com.spotify.scio.testing._

class CloudStorageLoaderSpec extends PipelineSpec {
  val expected = (1 to 10).map(_.toString)

  "CloudStorageLoader" should "output a file" in {
    JobTest[CloudStorageLoader.type]
      .args("--inputSubscription=in", "--outputDirectory=gs://out-dir/")
      .input(PubsubIO("in"), expected)
      .output(CustomIO[String]("output"))(_ should containInAnyOrder(expected))
      .run()
  }
}
