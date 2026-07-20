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

import dev.sasikanth.rss.reader.reader.readability.HtmlReadabilityRunner
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest

/**
 * HtmlUnit's DOMParser.parseFromString builds the document as an HtmlPage whose enclosing window is
 * explicitly set to null. Parsing article HTML that contains an <iframe> made HtmlUnit construct a
 * FrameWindow against that null parent window and crash with an NPE, so any article with an
 * embedded video used to take down the whole readability pass on desktop.
 */
class IFrameContentTest {

  private val dispatchersProvider =
    object : DispatchersProvider {
      override val main = Dispatchers.Default
      override val io = Dispatchers.Default
      override val default = Dispatchers.Default
      override val databaseRead = Dispatchers.Default
      override val databaseWrite = Dispatchers.Default
    }

  private val runner = HtmlReadabilityRunner(dispatchersProvider)

  @Test
  fun parseHtml_withYouTubeIframeEmbed_parsesAndKeepsVideoLink() = runTest {
    val html =
      """
      <div>
        <p>Choosing the best walking pad for a home office setup takes real research, and
        we spent six weeks testing more than a dozen models before settling on our top
        picks for quiet operation, foldability, and price.</p>
        <iframe src="https://www.youtube.com/embed/dQw4w9WgXcQ" width="560" height="315"
          frameborder="0" allowfullscreen></iframe>
        <p>The Urevo CyberPad impressed us with how quiet it ran even at higher speeds, and
        its low profile means it slides easily under a standing desk when it's not in use.</p>
      </div>
      """
        .trimIndent()

    val result = runner.parseHtml(link = "https://example.com/article", content = html)

    assertTrue(
      result.content.orEmpty().contains("we spent six weeks testing"),
      "expected article text to survive parsing, got: ${result.content}",
    )
    assertTrue(
      result.content
        .orEmpty()
        .contains("[YouTube Video](https://www.youtube.com/embed/dQw4w9WgXcQ)"),
      "expected the iframe embed to become a video link, got: ${result.content}",
    )
  }

  @Test
  fun parseHtml_withLazyLoadedIframe_parsesAndKeepsVideoLink() = runTest {
    val html =
      """
      <div>
        <p>Choosing the best walking pad for a home office setup takes real research, and
        we spent six weeks testing more than a dozen models before settling on our top
        picks for quiet operation, foldability, and price.</p>
        <iframe data-src="https://player.vimeo.com/video/123456" width="560"
          height="315"></iframe>
        <p>The Urevo CyberPad impressed us with how quiet it ran even at higher speeds, and
        its low profile means it slides easily under a standing desk when it's not in use.</p>
      </div>
      """
        .trimIndent()

    val result = runner.parseHtml(link = "https://example.com/article", content = html)

    assertTrue(
      result.content.orEmpty().contains("[Video](https://player.vimeo.com/video/123456)"),
      "expected the lazy iframe embed to become a video link, got: ${result.content}",
    )
  }
}
