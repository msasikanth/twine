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

package dev.sasikanth.rss.reader.markdown

import coil3.size.Dimension
import coil3.size.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

class SafeMarkdownTextTest {

  private val parser = MarkdownParser(GFMFlavourDescriptor())

  @Test
  fun collectImageNodesReturnsEmptyForTextAndLinkParagraphs() {
    val content = "Hello **world** with a [link](https://example.com) and `inline code`."
    val tree = parser.buildMarkdownTreeFromString(content)

    assertTrue(collectImageNodes(tree).isEmpty())
  }

  @Test
  fun collectImageNodesFindsInlineImagesInParagraph() {
    val content =
      "Paragraph with ![first](https://example.com/1.png) and ![second](https://example.com/2.png)."
    val tree = parser.buildMarkdownTreeFromString(content)

    assertEquals(2, collectImageNodes(tree).size)
  }

  @Test
  fun markdownInlineImageUsesHighResBoundAndDistinctMemoryCacheKey() {
    val url = "https://example.com/chart.png"

    assertEquals(Size(2048, Dimension.Undefined), MarkdownImageSize)
    assertEquals("https://example.com/chart.png#reader_inline", inlineImageMemoryCacheKey(url))
    assertNotEquals(url, inlineImageMemoryCacheKey(url))
  }
}
