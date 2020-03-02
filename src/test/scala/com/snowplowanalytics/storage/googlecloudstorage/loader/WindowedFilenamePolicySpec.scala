/*
 * Copyright (c) 2018-2020 Snowplow Analytics Ltd. All rights reserved.
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
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider
import org.apache.beam.sdk.transforms.windowing.{IntervalWindow, PaneInfo, BoundedWindow}

import org.joda.time.DateTime

import org.mockito.Mockito.when

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.Matchers._
import org.scalatestplus.mockito.MockitoSugar

class WindowedFilenamePolicySpec extends AnyFreeSpec with MockitoSugar {
  object TestOutputFileHints extends OutputFileHints {
    override def getMimeType: String = ""
    override def getSuggestedFilenameSuffix: String = ""
  }

  "the WindowedFilenamePolicy" - {
    "makes a windowedFilename function available" - {
      "which produces a windowed filename" in {
        val outputDirectoryStr = "outputDirectory"
        val filenamePrefixStr = "file-prefix"
        val filenameSuffixStr = ".pdf"
        val rowTypeStr = "rowType"
        val outputDirectory = Some(StaticValueProvider.of(outputDirectoryStr))
        val window = mock[BoundedWindow]
        val paneInfo = PaneInfo.createPane(false, true, PaneInfo.Timing.ON_TIME, 0, 0)
        val policy = WindowedFilenamePolicy(
          outputDirectory,
          StaticValueProvider.of(filenamePrefixStr),
          StaticValueProvider.of("-SSS-NNN"),
          StaticValueProvider.of(filenameSuffixStr),
          StaticValueProvider.of("YYYY/MM/dd/HH"),
          Some(rowTypeStr)
        )

        val filename = policy.windowedFilename(1, 1, window, paneInfo, TestOutputFileHints)

        filename.toString should endWith(s"/$outputDirectoryStr/$rowTypeStr/$filenamePrefixStr-001-001$filenameSuffixStr")
      }
      "which produces a correct file name with interval window" in {
        val outputDirectoryStr = "outputDirectory"
        val filenamePrefixStr = "file-prefix"
        val filenameSuffixStr = ".pdf"
        val rowTypeStr = "rowType"
        val outputDirectory = Some(StaticValueProvider.of(outputDirectoryStr))
        val window = mock[IntervalWindow]
        val windowBegin = new DateTime(2018, 1, 8, 10, 53, 0).toInstant
        val windowEnd = new DateTime(2018, 1, 8, 10, 56, 0).toInstant
        when(window.maxTimestamp).thenReturn(windowEnd)
        when(window.start).thenReturn(windowBegin)
        when(window.end).thenReturn(windowEnd)
        val paneInfo = PaneInfo.createPane(false, true, PaneInfo.Timing.ON_TIME, 0, 0)
        val policy = WindowedFilenamePolicy(
          outputDirectory,
          StaticValueProvider.of(filenamePrefixStr),
          StaticValueProvider.of("-W-P-SSS-NNN"),
          StaticValueProvider.of(filenameSuffixStr),
          StaticValueProvider.of("YYYY/dd/MM/HH"),
          Some(rowTypeStr)
        )

        val filename = policy.windowedFilename(1, 1, window, paneInfo, TestOutputFileHints)

        filename.toString should endWith(s"/$outputDirectoryStr/$rowTypeStr/2018/08/01/10/$filenamePrefixStr-$windowBegin-$windowEnd-pane-0-last-001-001$filenameSuffixStr")
      }
      "which produces a correct file name when given output directory is None" in {
        val filenamePrefixStr = "file-prefix"
        val filenameSuffixStr = ".pdf"
        val rowTypeStr = "rowType"
        val window = mock[BoundedWindow]
        val paneInfo = PaneInfo.createPane(false, true, PaneInfo.Timing.ON_TIME, 0, 0)
        val policy = WindowedFilenamePolicy(
          None,
          StaticValueProvider.of(filenamePrefixStr),
          StaticValueProvider.of("-SSS-NNN"),
          StaticValueProvider.of(filenameSuffixStr),
          StaticValueProvider.of("YYYY/MM/dd/HH"),
          Some(rowTypeStr)
        )

        val filename = policy.windowedFilename(1, 1, window, paneInfo, TestOutputFileHints)

        filename.toString should endWith(s"/$rowTypeStr/$filenamePrefixStr-001-001$filenameSuffixStr")
      }
      "which produces a correct file name when both given type prefix is None" in {
        val outputDirectoryStr = "outputDirectory"
        val filenamePrefixStr = "file-prefix"
        val filenameSuffixStr = ".pdf"
        val outputDirectory = Some(StaticValueProvider.of(outputDirectoryStr))
        val window = mock[BoundedWindow]
        val paneInfo = PaneInfo.createPane(false, true, PaneInfo.Timing.ON_TIME, 0, 0)
        val policy = WindowedFilenamePolicy(
          outputDirectory,
          StaticValueProvider.of(filenamePrefixStr),
          StaticValueProvider.of("-SSS-NNN"),
          StaticValueProvider.of(filenameSuffixStr),
          StaticValueProvider.of("YYYY/MM/dd/HH"),
          None
        )

        val filename = policy.windowedFilename(1, 1, window, paneInfo, TestOutputFileHints)

        filename.toString should endWith(s"/$outputDirectoryStr/$filenamePrefixStr-001-001$filenameSuffixStr")
      }
      "which produces a correct file name when both given output directory and type prefix is None" in {
        val filenamePrefixStr = "file-prefix"
        val filenameSuffixStr = ".pdf"
        val window = mock[BoundedWindow]
        val paneInfo = PaneInfo.createPane(false, true, PaneInfo.Timing.ON_TIME, 0, 0)
        val policy = WindowedFilenamePolicy(
          None,
          StaticValueProvider.of(filenamePrefixStr),
          StaticValueProvider.of("-SSS-NNN"),
          StaticValueProvider.of(filenameSuffixStr),
          StaticValueProvider.of("YYYY/MM/dd/HH"),
          None
        )

        val filename = policy.windowedFilename(1, 1, window, paneInfo, TestOutputFileHints)

        filename.toString should endWith(s"/$filenamePrefixStr-001-001$filenameSuffixStr")
      }
    }
    "makes a unwindowedFilename function available" - {
      "which throws an unsupported operation" in {
        val outputDirectory = Some(StaticValueProvider.of("outputDirectory"))
        val policy = WindowedFilenamePolicy(
          outputDirectory,
          StaticValueProvider.of("out"),
          StaticValueProvider.of("-SSS-NNN"),
          StaticValueProvider.of(".txt"),
          StaticValueProvider.of("YYYY/MM/dd/HH"),
          Some("typePrefix")
        )

        an [UnsupportedOperationException] should be thrownBy
          policy.unwindowedFilename(1, 1, TestOutputFileHints)
      }
    }
  }

  private def tempDir(): File =
    Files.createTempDirectory("WindowedFilenamePolicy").toFile
}
