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
import kotlin.test.assertSame

class PlatformExtTest {

  @Test
  fun plainTextReturnsSameInstanceWithoutDecoding() {
    val plain = "Swift 6.2 and Kotlin Multiplatform 2.4 release notes"
    assertSame(plain, plain.decodeHTMLString())
    assertEquals("", "".decodeHTMLString())
  }

  @Test
  fun decodesStandardXmlAndNamedHtmlEntities() {
    assertEquals(
      "Tom & Jerry's \"Best\" <Episodes> — 2026 © ® €",
      "Tom &amp; Jerry&apos;s &quot;Best&quot; &lt;Episodes&gt; &mdash; 2026 &copy; &reg; &euro;"
        .decodeHTMLString(),
    )
    assertEquals(
      "Wait\u2026 \u201Csmart quotes\u201D & \u2018apostrophes\u2019 \u00A0•\u00A0 50\u00B0",
      "Wait&hellip; &ldquo;smart quotes&rdquo; &AMP; &lsquo;apostrophes&rsquo; &nbsp;&bull;&NBSP; 50&deg;"
        .decodeHTMLString(),
    )
  }

  @Test
  fun decodesExtendedHtml5EntitiesAndEmojis() {
    assertEquals(
      "Ratio: 1:2 (✓)\u200Adone \uD835\uDD69",
      "Ratio&colon; 1&colon;2 &lpar;&check;&rpar;&hairsp;done &xopf;".decodeHTMLString(),
    )
    assertEquals(
      "Literal emojis 🚀✨👨\u200D💻 in <p>HTML ✓</p> &check;",
      "<p>Literal emojis 🚀✨👨\u200D💻 in &lt;p&gt;HTML &check;&lt;/p&gt; &amp;check;</p>"
        .decodeHTMLString(),
    )
  }

  @Test
  fun decodesDecimalAndHexNumericEntitiesIncludingAstralAndWin1252() {
    assertEquals("It’s — great!", "It&#8217;s &#8212; great!".decodeHTMLString())
    assertEquals("It’s — hex!", "It&#x2019;s &#X2014; hex!".decodeHTMLString())
    assertEquals(
      "Rocket 🚀 & Smile 😀",
      "Rocket &#128640; &amp; Smile &#x1F600;".decodeHTMLString(),
    )
    assertEquals("Legacy ’ — … €", "Legacy &#146; &#151; &#133; &#128;".decodeHTMLString())
    assertEquals("\uFFFD\uFFFD", "&#0;&#x110000;".decodeHTMLString())
  }

  @Test
  fun stripsInlineAndBlockHtmlTagsWhilePreservingLineBreaksAndTableCellSpacing() {
    assertEquals(
      "Bold and italic link",
      "<p><b>Bold</b> and <i>italic</i> <a href=\"https://example.com?a=1&b=2\" title=\"x > y\">link</a></p>"
        .decodeHTMLString(),
    )
    assertEquals(
      "First paragraph\nSecond paragraph",
      "<p>First paragraph</p><p>Second paragraph</p>".decodeHTMLString(),
    )
    assertEquals("Line 1\nLine 2\nLine 3", "Line 1<br>Line 2<br/>Line 3".decodeHTMLString())
    assertEquals(
      "Item one\nItem two",
      "<ul><li>Item one</li><li>Item two</li></ul>".decodeHTMLString(),
    )
    assertEquals("Col1 Col2", "<td>Col1</td><td>Col2</td>".decodeHTMLString())
    assertEquals("Head1 Head2", "<th>Head1</th><th>Head2</th>".decodeHTMLString())
    assertEquals(
      "H1 H2\nCol1 Col2",
      "<table><tr><th>H1</th><th>H2</th></tr><tr><td>Col1</td><td>Col2</td></tr></table>"
        .decodeHTMLString(),
    )
    assertEquals("Term\nDefinition", "<dl><dt>Term</dt><dd>Definition</dd></dl>".decodeHTMLString())
  }

  @Test
  fun stripsCommentsScriptsStylesAndUnwrapsCdata() {
    assertEquals(
      "Visible text",
      "<!-- hidden comment --><style>body { color: red; }</style><p>Visible <script type=\"text/javascript\">if (a < b && c > d) alert(1);</script>text</p>"
        .decodeHTMLString(),
    )
    assertEquals(
      "Raw <b>CDATA</b> & text",
      "<![CDATA[Raw <b>CDATA</b> &amp; text]]>".decodeHTMLString(),
    )
  }

  @Test
  fun preservesNonTagLessThanWithLettersApostrophesAndGenerics() {
    val mathWithApostrophe = "a<b when it's > 0 and x' > 1"
    assertSame(mathWithApostrophe, mathWithApostrophe.decodeHTMLString())
    assertEquals("a<b when it's > 0", "a<b when it's > 0".decodeHTMLString())
    assertEquals(
      "Use List<T> and Map<String, Int>",
      "Use List<T> and Map<String, Int>".decodeHTMLString(),
    )
    assertEquals(
      "Condition: a<b when it's > 0 in List<T>",
      "<p>Condition&colon; a<b when it's > 0 in List<T></p>".decodeHTMLString(),
    )
  }

  @Test
  fun preservesEscapedAngleBracketsAndHandlesMalformedInputsGracefully() {
    assertEquals(
      "Use <Compose> in Kotlin",
      "<p>Use &lt;Compose&gt; in Kotlin</p>".decodeHTMLString(),
    )
    assertEquals("5 < 10 & 20 > 15", "5 < 10 & 20 > 15".decodeHTMLString())
    assertEquals(
      "Unclosed <tag and &unknown; and &#; and &#xz; and &amp",
      "Unclosed <tag and &unknown; and &#; and &#xz; and &amp".decodeHTMLString(),
    )
  }
}
