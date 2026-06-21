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

package dev.sasikanth.rss.reader.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class StringExtTest {

  @Test
  fun removeLineBreaks_withNoLineBreaks_shouldReturnSameString() {
    val input = "Hello World"
    val result = input.removeLineBreaks()
    assertEquals("Hello World", result)
  }

  @Test
  fun removeLineBreaks_withNewlines_shouldReplaceWithSpaces() {
    val input = "Hello\nWorld\nFrom\nKotlin"
    val result = input.removeLineBreaks()
    assertEquals("Hello World From Kotlin", result)
  }

  @Test
  fun removeLineBreaks_withCarriageReturns_shouldReplaceWithSpaces() {
    val input = "Hello\rWorld\r\nFrom\r\nKotlin"
    val result = input.removeLineBreaks()
    assertEquals("Hello World From Kotlin", result)
  }

  @Test
  fun removeLineBreaks_withMultipleConsecutiveLineBreaks_shouldReplaceWithSingleSpace() {
    val input = "Hello\n\n\nWorld\r\n\r\nFrom\r\rKotlin"
    val result = input.removeLineBreaks()
    assertEquals("Hello World From Kotlin", result)
  }
}
