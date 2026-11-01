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

package dev.sasikanth.rss.reader.reader.redability

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackReadabilityResultTest {

  @Test
  fun fallbackReadabilityResult_stripsHtmlAndPreservesParagraphs() {
    val html =
      """
      <html>
        <head><title>Ignored Title</title><style>body { color: red; }</style></head>
        <body>
          <script>alert('xss');</script>
          <div class="article">
            <h1>Heading</h1>
            <p>First <b>paragraph</b> with inline formatting.</p>
            <p>Second paragraph<br>with line break.</p>
          </div>
        </body>
      </html>
      """
        .trimIndent()

    val result = fallbackReadabilityResult(html)

    assertEquals(
      """
      Heading

      First paragraph with inline formatting.

      Second paragraph
      with line break.
      """
        .trimIndent(),
      result.content,
    )
  }

  @Test
  fun fallbackReadabilityResult_withBlankContent_returnsOriginal() {
    assertEquals("", fallbackReadabilityResult("").content)
    assertEquals(null, fallbackReadabilityResult(null).content)
  }
}
