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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime

class DateTimeFormattersIosTest {

  @Test
  fun parsesRssAndAtomDatesConcurrentlyAcrossThreads() = runBlocking {
    val inputs =
      listOf(
        "Sun, 15 Nov 2026 10:00:00 +0000" to 1794736800000L,
        "2026-11-15T10:00:00Z" to 1794736800000L,
        "2026-11-15 10:00:00" to 1794736800000L,
      )

    val deferreds =
      (1..24).map { iteration ->
        async(Dispatchers.IO) {
          val (raw, expected) = inputs[iteration % inputs.size]
          raw.dateStringToEpochMillis() to expected
        }
      }

    deferreds.awaitAll().forEach { (actual, expected) -> assertEquals(expected, actual) }
  }

  @Test
  fun formatsReaderAndHomeAppBarTimestampsConsistently() {
    val instant = Instant.fromEpochMilliseconds(1794736800000L)
    val first = instant.readerDateTimestamp()
    val second = instant.readerDateTimestamp()
    assertTrue(first.contains("•"))
    assertEquals(first, second)

    val localDateTime = LocalDateTime(2026, 11, 15, 10, 0)
    val appBarFirst = localDateTime.homeAppBarTimestamp()
    val appBarSecond = localDateTime.homeAppBarTimestamp()
    assertNotNull(appBarFirst)
    assertTrue(appBarFirst.isNotBlank())
    assertEquals(appBarFirst, appBarSecond)
  }
}
