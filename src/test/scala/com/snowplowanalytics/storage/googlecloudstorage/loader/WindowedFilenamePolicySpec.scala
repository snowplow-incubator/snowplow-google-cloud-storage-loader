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
package com.snowplowanalytics.storage.googlecloudstorage.loader

import java.io.File
import java.nio.file.Files

import org.apache.beam.sdk.io.FileBasedSink.OutputFileHints
import org.apache.beam.sdk.io.LocalResources
import org.apache.beam.sdk.io.fs.ResolveOptions.StandardResolveOptions
import org.apache.beam.sdk.transforms.windowing.{BoundedWindow, IntervalWindow, PaneInfo}
import org.joda.time.DateTime

import org.mockito.Mockito.when
import org.scalatest.FreeSpec
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar

class WindowedFilenamePolicySpec extends FreeSpec with MockitoSugar {
  object TestOutputFileHints extends OutputFileHints {
    override def getMimeType: String = ""
    override def getSuggestedFilenameSuffix: String = ""
  }

  "the WindowedFilenamePolicy" - {
    "make a windowedFilename function available" - {
      "which produces a windowed filename" in {
        val outputDirectory = LocalResources.fromFile(tempDir(), isDirectory = true)
          .resolve("WindowedFilenamePolicy", StandardResolveOptions.RESOLVE_DIRECTORY)
        val window = mock[BoundedWindow]
        val paneInfo = PaneInfo.createPane(false, true, PaneInfo.Timing.ON_TIME, 0, 0)
        val policy = WindowedFilenamePolicy(outputDirectory.toString, "out", "-SSS-NNN", ".txt")

        val filename = policy.windowedFilename(1, 1, window, paneInfo, TestOutputFileHints)

        filename.getFilename shouldEqual "out-001-001.txt"
      }
      "which produces a dynamic filename" in {
        val outputDirectory = LocalResources.fromFile(tempDir(), isDirectory = true)
          .resolve("YYYY/MM/dd/HH", StandardResolveOptions.RESOLVE_DIRECTORY)
        val paneInfo = PaneInfo.createPane(false, true, PaneInfo.Timing.ON_TIME, 0, 0)
        val window = mock[IntervalWindow]
        val windowBegin = new DateTime(2018, 1, 8, 10, 55, 0).toInstant
        val windowEnd = new DateTime(2018, 1, 8, 10, 56, 0).toInstant
        when(window.maxTimestamp).thenReturn(windowEnd)
        when(window.start).thenReturn(windowBegin)
        when(window.end).thenReturn(windowEnd)
        val policy = WindowedFilenamePolicy(outputDirectory.toString, "out", "-SSS-NNN", ".txt")

        val filename = policy.windowedFilename(1, 1, window, paneInfo, TestOutputFileHints)

        filename.getCurrentDirectory.toString should endWith("2018/01/08/10/")
        filename.getFilename shouldEqual "out-001-001.txt"
      }
    }
    "make a unwindowedFilename function available" - {
      "which throws an unsupported operation" in {
        val outputDirectory = LocalResources.fromFile(tempDir(), isDirectory = true)
          .resolve("WindowedFilenamePolicy", StandardResolveOptions.RESOLVE_DIRECTORY)
        val policy = WindowedFilenamePolicy(outputDirectory.toString, "out", "-SSS-NNN", ".txt")

        an [UnsupportedOperationException] should be thrownBy
          policy.unwindowedFilename(1, 1, TestOutputFileHints)
      }
    }
  }

  private def tempDir(): File =
    Files.createTempDirectory("WindowedFilenamePolicy").toFile
}
