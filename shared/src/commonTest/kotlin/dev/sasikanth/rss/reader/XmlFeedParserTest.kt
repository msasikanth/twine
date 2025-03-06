/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader

import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.xml.AtomContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.RDFContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.RSSContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import korlibs.io.lang.Charsets
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class XmlFeedParserTest {

  private lateinit var httpClient: HttpClient
  private lateinit var xmlFeedParser: XmlFeedParser

  @BeforeTest
  fun setup() {
    httpClient =
      HttpClient(MockEngine) {
        engine {
          dispatcher = UnconfinedTestDispatcher()
          addHandler { request ->
            if (UrlUtils.isYouTubeLink(request.url.toString())) {
              respond(
                content = ByteReadChannel(youtubeChannelHtml.toByteArray()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "html")
              )
            } else {
              respond(
                content = ByteReadChannel("".toByteArray()),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf()
              )
            }
          }
        }
      }
    xmlFeedParser =
      XmlFeedParser(
        rdfContentParser = RDFContentParser(),
        rssContentParser = RSSContentParser(),
        atomContentParser = AtomContentParser(httpClient),
        dispatchersProvider = TestDispatchersProvider()
      )
  }

  @Test
  fun parsingRssFeedShouldWorkCorrectly() = runTest {
    // given
    val expectedFeedPayload =
      FeedPayload(
        name = "Feed title",
        icon = "https://icon.horse/icon/example.com",
        description = "Feed description",
        link = feedUrl,
        homepageLink = "https://example.com",
        posts =
          listOf(
            PostPayload(
              title = "Post with image",
              link = "https://example.com/first-post",
              description = "First post description.",
              rawContent = "First post description.",
              imageUrl = "https://example.com/first-post-media-url",
              date = 1685005200000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "Post without image",
              link = "https://example.com/second-post",
              description = "Second post description.",
              rawContent = "Second post description.",
              imageUrl = null,
              date = 1684999800000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "Podcast post",
              link = "https://example.com/third-post",
              description = "Third post description.",
              rawContent = "Third post description.",
              imageUrl = null,
              date = 1684924200000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "Post with enclosure image",
              link = "https://example.com/fourth-post",
              description = "Fourth post description.",
              rawContent = "Fourth post description.",
              imageUrl = "https://example.com/enclosure-image",
              date = 1684924200000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "Post with description and encoded content",
              link = "https://example.com/fifth-post",
              description = "Fourth post description in HTML syntax.",
              rawContent =
                """
                  <p>Fourth post description in HTML syntax.</p>
                  <img src="https://example.com/encoded-image" alt="encoded image" />
                """
                  .trimIndent(),
              imageUrl = "https://example.com/encoded-image",
              date = 1684924200000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "Post with relative path image",
              link = "https://example.com/post-with-relative-image",
              description = "Relative image post description.",
              rawContent = "Relative image post description.",
              imageUrl = "http://example.com/relative-media-url",
              date = 1685005200000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "Post with comments",
              link = "https://example.com/post-with-comments",
              description = "Really long post with comments.",
              rawContent = "Really long post with comments.",
              imageUrl = null,
              date = 1685005200000,
              commentsLink = "https://example/post-with-comments/comments",
              isDateParsedCorrectly = true
            ),
          )
      )

    // when
    val content = ByteReadChannel(rssXmlContent.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)

    // then
    assertEquals(expectedFeedPayload, payload)
  }

  @Test
  fun parsingRDFFeedShouldWorkCorrectly() = runTest {
    // given
    val expectedFeedPayload =
      FeedPayload(
        name = "Feed title",
        icon = "https://icon.horse/icon/example.com",
        description = "Feed description",
        link = feedUrl,
        homepageLink = "https://example.com",
        posts =
          listOf(
            PostPayload(
              title = "Post",
              link = "https://example.com/first-post",
              description = "First post description.",
              rawContent = "First post description.",
              imageUrl = null,
              date = 1685005200000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "Post with encoded description",
              link = "https://example.com/second-post",
              description = "Second post description in HTML syntax.",
              rawContent =
                """
                  <p>Second post description in HTML syntax.</p>
                  <img src="https://example.com/encoded-image" alt="encoded image" />
                """
                  .trimIndent(),
              imageUrl = "https://example.com/encoded-image",
              date = 1684924200000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
          )
      )

    // when
    val content = ByteReadChannel(rdfXmlContent.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)

    // then
    assertEquals(expectedFeedPayload, payload)
  }

  @Test
  fun parsingAtomFeedShouldWorkCorrectly() = runTest {
    // given
    val expectedFeedPayload =
      FeedPayload(
        name = "Feed title",
        icon = "https://icon.horse/icon/example.com",
        description = "Feed description",
        link = feedUrl,
        homepageLink = "https://example.com",
        posts =
          listOf(
            PostPayload(
              title = "Post with image",
              link = "https://example.com/first-post",
              description = "Post summary with an image.",
              rawContent =
                """
                  <img alt="First Image" src="https://example.com/image.jpg" />
                  <p>Post summary with an image.</p>
                """
                  .trimIndent(),
              imageUrl = "https://example.com/image.jpg",
              date = 1685008800000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "Second post",
              link = "https://example.com/second-post",
              description = "Post summary of the second post.",
              rawContent =
                """
                  <p>Post summary of the second post.</p>
                """
                  .trimIndent(),
              imageUrl = null,
              date = 1684917000000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "Post without image",
              link = "https://example.com/third-post",
              description = "Post summary of the third post. click here.",
              rawContent =
                """
                  <p>Post summary of the third post. <a href="https://example.com/hyperlink" >click here</a>.</p>
                """
                  .trimIndent(),
              imageUrl = null,
              date = 1684936800000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "Post with relative image",
              link = "https://example.com/relative-image-post",
              description = "Post summary with an image.",
              rawContent =
                """
                  <img alt="Relative Image" src="/resources/image.jpg" />
                  <p>Post summary with an image.</p>
                """
                  .trimIndent(),
              imageUrl = "http://example.com/resources/image.jpg",
              date = 1685008800000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
          )
      )

    // when
    val content = ByteReadChannel(atomXmlContent.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)

    // then
    assertEquals(expectedFeedPayload, payload)
  }

  @Test
  fun parsingYouTubeAtomFeedShouldWorkCorrectly() = runTest {
    // given
    val expectedFeedPayload =
      FeedPayload(
        name = "Google Developers",
        icon = "https://youtube.com/img/channel.jpg",
        description = "",
        link = youtubeFeedUrl,
        homepageLink = "https://www.youtube.com/channel/UC_x5XG1OV2P6uZZ5FSM9Ttw",
        posts =
          listOf(
            PostPayload(
              title =
                "Android Beyond Phones: A New Way to Build with Jetpack Compose | Android Dev Summit '23",
              link = "https://www.youtube.com/watch?v=2QpWq3iQdC4",
              description = "Subscribe to watch more videos about Android development",
              rawContent = null,
              imageUrl = "https://i.ytimg.com/vi/2QpWq3iQdC4/maxresdefault.jpg",
              date = 1698260988000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
          )
      )

    // when
    val content = ByteReadChannel(youtubeAtomFeed.toByteArray())
    val payload = xmlFeedParser.parse(content, youtubeFeedUrl, Charsets.UTF8)

    // then
    assertEquals(expectedFeedPayload, payload)
  }
}
