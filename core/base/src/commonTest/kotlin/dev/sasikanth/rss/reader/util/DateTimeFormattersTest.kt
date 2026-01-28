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

package dev.sasikanth.rss.reader.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeFormattersTest {

  @Test
  fun parsingSupportedDateFormatsShouldWorkCorrectly() {
    // given
    val dates =
      listOf(
        "Thu, 2 Nov 2023 23:13:00 GMT",
        "Fri, 24 Nov 23 12:13:00 +0000",
        "Wed, 29 Nov 2023 16:38:00 GMT-8",
        "Thu, 30 Nov 2023 11:41 -0500",
        "Sat, 1 Jul 2023 18:52:11 -0700",
        "Fri, 27 Nov 2015 17:24:34 Pacific Standard Time",
        "Sun, 06 Aug 2023",
        "28 Nov 2023 00:00:00 GMT",
        "2019-08-02T17:00:29Z",
        "2023-12-01T08:10:15+05:30",
        "2023-11-30 09:00:00",
        "2023-08-29 07:41:22 UTC",
        "2018-10-01",
        "11-27 18:23:16",
        "Fri, 24 Nov 2023 23:13:00 GMT",
        "Tue, 05 Dec 2023 15:55:50 PST",
        "2023-12-12T11:20:18",
        "2023-12-10T06:11:00.000-08:00",
        "01 Jun 2024 12:00 +0000",
        "Thu, 26 Sep 2024 14:30:00 +0200",
      )

    val expectedEpochMillis =
      listOf(
        1698966780000,
        1700827980000,
        1701304680000,
        1701362460000,
        1688262731000,
        1448673874000,
        1691280000000,
        1701129600000,
        1564765229000,
        1701398415000,
        1701334800000,
        1693294882000,
        1538352000000,
        1701109396000,
        1700867580000,
        1701820550000,
        1702380018000,
        1702217460000,
        1717243200000,
        1727353800000
      )

    // when
    val epochMillis =
      dates.map {
        try {
          it.dateStringToEpochMillis(clock = TestClock)
        } catch (e: Exception) {
          // no-op
        }
      }

    // then
    assertEquals(expectedEpochMillis, epochMillis)
  }

  @Test
  fun parsingAnUnknownOrInvalidDateFormatShouldReturnNull() {
    // given
    val invalidDate = "Invalid date"

    // when
    val epochMillis = invalidDate.dateStringToEpochMillis(clock = TestClock)

    // then
    assertEquals(null, epochMillis)
  }
}
