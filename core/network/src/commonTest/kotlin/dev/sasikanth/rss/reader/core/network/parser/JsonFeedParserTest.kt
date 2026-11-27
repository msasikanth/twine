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

package dev.sasikanth.rss.reader.core.network.parser

import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.core.network.parser.json.JsonFeedParser
import dev.sasikanth.rss.reader.core.network.utils.TestDispatchersProvider
import dev.sasikanth.rss.reader.core.network.utils.jsonFeed
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class JsonFeedParserTest {

  private suspend fun assertFeedPayloadEquals(expected: FeedPayload, actual: FeedPayload) {
    assertEquals(expected.name, actual.name)
    assertEquals(expected.icon, actual.icon)
    assertEquals(expected.coverArtUrl, actual.coverArtUrl)
    assertEquals(expected.description, actual.description)
    assertEquals(expected.homepageLink, actual.homepageLink)
    assertEquals(expected.link, actual.link)
    assertEquals(expected.posts.toList(), actual.posts.toList())
  }

  private val parser =
    JsonFeedParser(
      dispatchersProvider = TestDispatchersProvider(),
      articleHtmlParser = ArticleHtmlParser(),
    )

  @Test
  fun parsingJsonFeedShouldWorkCorrectly() = runTest {
    // given
    val expectedFeed =
      FeedPayload(
        name = "Tech Insights Blog",
        icon = "https://example.com/icon.png",
        coverArtUrl = "https://example.com/icon.png",
        description = "The latest insights and news from the tech world",
        homepageLink = "https://example.com",
        link = "https://example.com/feed.json",
        posts =
          listOf(
              PostPayload(
                title = "The Future of Quantum Computing in 2025",
                link = "https://example.com/posts/quantum-computing-2025",
                description =
                  "An overview of recent quantum computing advances and what they mean for the tech industry",
                rawContent =
                  """
                  <html>
                   <body>
                    <p>Quantum computing has made significant strides in the past year. Recent breakthroughs at IBM and Google have pushed the boundaries of what we thought possible.</p>
                    <p>The new 1000-qubit processor announced last month represents a major milestone in the industry.</p>
                   </body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/images/quantum-2025.jpg",
                date = 1740734100000,
                commentsLink = null,
                isDateParsedCorrectly = true,
              ),
              PostPayload(
                title = "AI Ethics Frameworks: A Comparative Analysis",
                link = "https://example.com/posts/ai-ethics-frameworks",
                description =
                  "Comparing different approaches to AI ethics across industry and government",
                rawContent =
                  """
                  <html>
                   <body>
                    <p>As AI becomes more integrated into our daily lives, the need for robust ethical frameworks has never been more important.</p>
                    <p>This article examines the approaches taken by major tech companies and governments around the world.</p>
                   </body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = null,
                date = 1739629800000,
                commentsLink = null,
                isDateParsedCorrectly = true,
              ),
              PostPayload(
                title = "The Rise of Edge Computing",
                link = "https://example.com/posts/edge-computing-rise",
                description =
                  "Edge computing continues to grow as IoT devices proliferate. This shift is changing how we think about network architecture and data processing.\n\nIn this article, we explore the implications for businesses and consumers alike.",
                rawContent =
                  "Edge computing continues to grow as IoT devices proliferate. This shift is changing how we think about network architecture and data processing.\n\nIn this article, we explore the implications for businesses and consumers alike.",
                fullContent = null,
                imageUrl = "https://example.com/images/edge-computing.jpg",
                date = 1738410300000,
                commentsLink = null,
                isDateParsedCorrectly = true,
              ),
              PostPayload(
                title = "Sustainable Tech: Green Innovations in Silicon Valley",
                link = "https://example.com/posts/sustainable-tech-innovations",
                description =
                  "Silicon Valley companies are leading the charge in sustainable technology development.\n\nFrom carbon-neutral data centers to biodegradable electronics, we look at the most promising initiatives.",
                rawContent =
                  """
                  <html>
                   <body>
                    <p>Silicon Valley companies are leading the charge in sustainable technology development.</p>
                    <p>From carbon-neutral data centers to biodegradable electronics, we look at the most promising initiatives.</p>
                   </body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = null,
                date = 1737388800000,
                commentsLink = null,
                isDateParsedCorrectly = true,
              ),
            )
            .asFlow(),
      )
    val jsonFeed = ByteReadChannel(jsonFeed).readBuffer()

    // when
    val payload = parser.parse(jsonFeed, "https://example.com/feed.json")

    // then
    assertFeedPayloadEquals(expectedFeed, payload)
  }

  @Test
  fun parsingJsonFeedWithLongContentPreservesFullDescriptionAndRawContent() = runTest {
    val longBody = "c".repeat(4095) + "\uD83D\uDE80 json-tail-keyword " + "c".repeat(1000)
    val rawJson =
      """
      {
        "version": "https://jsonfeed.org/version/1.1",
        "title": "Long JSON Feed",
        "home_page_url": "https://example.com",
        "feed_url": "https://example.com/feed.json",
        "items": [
          {
            "id": "1",
            "url": "https://example.com/1",
            "title": "Long JSON Item",
            "content_html": "<p>$longBody</p>",
            "date_published": "2025-02-28T09:15:00Z"
          }
        ]
      }
      """
        .trimIndent()

    val payload =
      parser.parse(ByteReadChannel(rawJson).readBuffer(), "https://example.com/feed.json")
    val posts = payload.posts.toList()

    assertEquals(1, posts.size)
    val post = posts.single()
    assertEquals(longBody, post.description)
    assertTrue((post.rawContent ?: "").contains(longBody))
  }

  @Test
  fun parsingJsonFeedSupportsLowercaseFaviconBannerImageAndImageAttachments() = runTest {
    val rawJson =
      """
      {
        "version": "https://jsonfeed.org/version/1.1",
        "title": "Podcast JSON",
        "home_page_url": "https://example.com",
        "feed_url": "https://example.com/feed.json",
        "favicon": "https://example.com/favicon.png",
        "items": [
          {
            "id": "1",
            "url": "https://example.com/1",
            "title": "Banner Item",
            "banner_image": "https://example.com/banner.jpg",
            "content_html": "<p>Body</p><img src=\"https://example.com/inline.jpg\"/>",
            "attachments": [
              { "url": "https://example.com/attach.jpg", "mime_type": "image/jpeg" },
              { "url": "https://example.com/ep1.mp3", "mime_type": "audio/mpeg" }
            ]
          },
          {
            "id": "2",
            "url": "https://example.com/2",
            "title": "Attachment Image Item",
            "content_html": "<p>Body</p><img src=\"https://example.com/inline2.jpg\"/>",
            "attachments": [
              { "url": "https://example.com/attach2.png", "mime_type": "image/png" }
            ]
          }
        ]
      }
      """
        .trimIndent()

    val payload =
      parser.parse(ByteReadChannel(rawJson).readBuffer(), "https://example.com/feed.json")
    val posts = payload.posts.toList()

    assertEquals("https://example.com/favicon.png", payload.icon)
    assertEquals(null, payload.coverArtUrl)
    assertEquals("https://example.com/banner.jpg", posts[0].imageUrl)
    assertEquals("https://example.com/ep1.mp3", posts[0].audioUrl)
    assertEquals("https://example.com/attach2.png", posts[1].imageUrl)
  }
}
