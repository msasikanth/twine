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

class DurationExtTest {

  @Test
  fun formatting_duration_less_than_an_hour_should_work_correctly() {
    // given
    val duration = 150000L // 2 minutes 30 seconds

    // when
    val result = formatDuration(duration)

    // then
    assertEquals("02:30", result)
  }

  @Test
  fun formatting_duration_more_than_an_hour_should_work_correctly() {
    // given
    val duration = 3900000L // 1 hour 5 minutes 0 seconds

    // when
    val result = formatDuration(duration)

    // then
    assertEquals("01:05:00", result)
  }

  @Test
  fun formatting_duration_with_single_digit_values_should_work_correctly() {
     // given
    val duration = 3661000L // 1 hour 1 minute 1 second

    // when
    val result = formatDuration(duration)

    // then
    assertEquals("01:01:01", result)
  }
}
