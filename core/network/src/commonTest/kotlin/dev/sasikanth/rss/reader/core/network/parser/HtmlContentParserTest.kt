/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.core.network.parser

import dev.sasikanth.rss.reader.core.network.parser.common.HtmlContentParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HtmlContentParserTest {

  companion object {
    private const val TEST_HTML =
      """
      <figure>
        <img alt="A screenshot from DOOM + DOOM II." src="https://cdn.vox-cdn.com/thumbor/LJt9a0BM9fnTyZtP68Ba1Mr1YDY=/150x0:1770x1080/1310x873/cdn.vox-cdn.com/uploads/chorus_image/image/73510530/ss_c5781b8f9a8181e6c989869b86d0b455ccca344a.0.jpg"/>
        <figcaption>Image: Bethesda</figcaption> 
      </figure>
      <p id="2Z0e9a">If you haven’t played <em>Doom</em> or <em>Doom II</em> for a while — or ever — a new re-release that Bethesda <a href="https://slayersclub.bethesda.net/en-US/article/doom-doomii-release-notes?linkId=100000279162898">surprise-dropped</a> (<a href="https://x.com/Wario64/status/1821578978462699748">sorta</a>) on Thursday might be the perfect excuse to jump in to the classic games. The re-release, which combines both games into one package called <em>Doom + Doom II</em> and is a free update for anyone who already owns <em>Doom (1993)</em> or <em>Doom II</em>, offers a long list of great new features — including a brand new single-player episode and online, cross-platform deathmatch multiplayer.</p> 
      <p id="Pm12nB">With <em>Doom + Doom II, </em>you’ll have access to both of those two games as well as extra single-player content like John Romero’s <em>Sigil</em> episode <a href="https://romero.com/sigil">released in 2019</a> and <em>Legacy of Rust</em>, which is a new <em>Doom</em> episode created by “individuals from id Software, Nightdive Studios...</p> 
      <p><a href="https://www.theverge.com/2024/8/8/24216379/doom-doom-ii-definitive-re-release">Continue reading&hellip;</a> </p> 
    """
  }

  @Test
  fun parsingLeadImageAndContentFromHtmlShouldWorkCorrectly() {
    // when
    val result = HtmlContentParser.parse(TEST_HTML)

    // then
    assertEquals(
      "https://cdn.vox-cdn.com/thumbor/LJt9a0BM9fnTyZtP68Ba1Mr1YDY=/150x0:1770x1080/1310x873/cdn.vox-cdn.com/uploads/chorus_image/image/73510530/ss_c5781b8f9a8181e6c989869b86d0b455ccca344a.0.jpg",
      result?.leadImage,
    )
    assertEquals(
      "If you haven’t played Doom or Doom II for a while — or ever — a new re-release that Bethesda surprise-dropped (sorta) on Thursday might be the perfect excuse to jump in to the classic games. The re-release, which combines both games into one package called Doom + Doom II and is a free update for anyone who already owns Doom (1993) or Doom II, offers a long list of great new features — including a brand new single-player episode and online, cross-platform deathmatch multiplayer. With Doom + Doom II, you’ll have access to both of those two games as well as extra single-player content like John Romero’s Sigil episode released in 2019 and Legacy of Rust, which is a new Doom episode created by “individuals from id Software, Nightdive Studios... Continue reading…",
      result?.content,
    )
  }

  @Test
  fun parsingContentFromTextShouldWorkCorrectly() {
    // when
    val result = HtmlContentParser.parse("This is a normal text")

    // then
    assertNull(result?.leadImage)
    assertEquals("This is a normal text", result?.content)
  }
}
