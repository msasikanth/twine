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

class StringExtTest {

  @Test
  fun splitting_and_trimming_should_work_correctly() {
    val input = "one :: two ::  :: three  :: "
    val separator = "::"
    val expected = listOf("one", "two", "three")

    val result = input.splitAndTrim(separator)

    assertEquals(expected, result)
  }

  @Test
  fun splitting_empty_string_should_return_empty_list() {
    val input = ""
    val separator = "::"
    val expected = emptyList<String>()

    val result = input.splitAndTrim(separator)

    assertEquals(expected, result)
  }

  @Test
  fun splitting_blank_string_should_return_empty_list() {
    val input = "   "
    val separator = "::"
    val expected = emptyList<String>()

    val result = input.splitAndTrim(separator)

    assertEquals(expected, result)
  }
}
