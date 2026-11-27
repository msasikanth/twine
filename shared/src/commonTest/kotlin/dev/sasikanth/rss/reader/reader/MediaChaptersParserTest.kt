/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.sasikanth.rss.reader.reader

import dev.sasikanth.rss.reader.core.model.local.MediaChapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MediaChaptersParserTest {

  @Test
  fun parsesStandardAndBracketedTimestampLines() {
    val input =
      """
      00:00 Intro
      [02:15] Architecture Overview
      (05:30) - Deep Dive
      1:05:10 — Closing Q&A
      """
        .trimIndent()

    val chapters = extractTimestampChapters(input)

    assertEquals(
      listOf(
        MediaChapter(timestampMs = 0L, title = "Intro"),
        MediaChapter(timestampMs = 135_000L, title = "Architecture Overview"),
        MediaChapter(timestampMs = 330_000L, title = "Deep Dive"),
        MediaChapter(timestampMs = 3_910_000L, title = "Closing Q&A"),
      ),
      chapters,
    )
    assertEquals(listOf("00:00", "02:15", "05:30", "01:05:10"), chapters.map { it.formattedTime })
  }

  @Test
  fun parsesMarkdownLinksBoldMarkersAndHtmlShowNotes() {
    val input =
      """
      <p>- [00:30](https://youtu.be/abc?t=30) Kickoff<br/>* **04:05** Main Topic &amp; Notes</p>
      <ul><li>12:00 Listener Mail</li></ul>
      """
        .trimIndent()

    val chapters = extractTimestampChapters(input)

    assertEquals(
      listOf(
        MediaChapter(timestampMs = 30_000L, title = "Kickoff"),
        MediaChapter(timestampMs = 245_000L, title = "Main Topic & Notes"),
        MediaChapter(timestampMs = 720_000L, title = "Listener Mail"),
      ),
      chapters,
    )
  }

  @Test
  fun deduplicatesByTimestampAndSortsAscending() {
    val input =
      """
      10:00 Final Segment
      00:00 Cold Open
      10:00 Duplicate Segment
      03:20 Middle Topic
      """
        .trimIndent()

    val chapters = extractTimestampChapters(input)

    assertEquals(
      listOf(
        MediaChapter(timestampMs = 0L, title = "Cold Open"),
        MediaChapter(timestampMs = 200_000L, title = "Middle Topic"),
        MediaChapter(timestampMs = 600_000L, title = "Final Segment"),
      ),
      chapters,
    )
  }

  @Test
  fun ignoresInvalidTimestampsEmptyTitlesAndClockTimes() {
    val input =
      """
      16:9 widescreen aspect ratio
      00:75 invalid seconds
      01:80:00 invalid minutes
      05:00 -
      06:00 123
      12:30 PM Lunch meeting
      Episode recorded at 10:30 AM in San Francisco.
      """
        .trimIndent()

    val chapters = extractTimestampChapters(input)

    assertTrue(chapters.isEmpty())
  }
}
