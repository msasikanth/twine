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
import dev.sasikanth.rss.reader.core.network.parser.xml.AtomContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.RDFContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.RSSContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser
import dev.sasikanth.rss.reader.core.network.utils.TestDispatchersProvider
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.core.network.utils.atomXmlContent
import dev.sasikanth.rss.reader.core.network.utils.feedUrl
import dev.sasikanth.rss.reader.core.network.utils.podcastAtomFeedUrl
import dev.sasikanth.rss.reader.core.network.utils.podcastAtomXmlContent
import dev.sasikanth.rss.reader.core.network.utils.podcastRssFeedUrl
import dev.sasikanth.rss.reader.core.network.utils.podcastRssXmlContent
import dev.sasikanth.rss.reader.core.network.utils.rdfXmlContent
import dev.sasikanth.rss.reader.core.network.utils.rdfXmlContentWithElementBeforeChannel
import dev.sasikanth.rss.reader.core.network.utils.rssXmlContent
import dev.sasikanth.rss.reader.core.network.utils.rssXmlContentWithElementBeforeChannel
import dev.sasikanth.rss.reader.core.network.utils.rssXmlContentWithNestedMediaInFirstItem
import dev.sasikanth.rss.reader.core.network.utils.youtubeAtomFeed
import dev.sasikanth.rss.reader.core.network.utils.youtubeChannelHtml
import dev.sasikanth.rss.reader.core.network.utils.youtubeFeedUrl
import dev.sasikanth.rss.reader.core.network.utils.youtubeUploadsPlaylistAtomFeed
import dev.sasikanth.rss.reader.core.network.utils.youtubeUploadsPlaylistFeedUrl
import dev.sasikanth.rss.reader.exceptions.XmlParsingError
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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class XmlFeedParserTest {

  private lateinit var httpClient: HttpClient
  private lateinit var xmlFeedParser: XmlFeedParser

  private suspend fun assertFeedPayloadEquals(expected: FeedPayload, actual: FeedPayload) {
    assertEquals(expected.name, actual.name)
    assertEquals(expected.icon, actual.icon)
    assertEquals(expected.coverArtUrl, actual.coverArtUrl)
    assertEquals(expected.description, actual.description)
    assertEquals(expected.homepageLink, actual.homepageLink)
    assertEquals(expected.link, actual.link)
    assertEquals(expected.posts.toList(), actual.posts.toList())
  }

  @BeforeTest
  fun setup() {
    val articleHtmlParser = ArticleHtmlParser()

    httpClient =
      HttpClient(MockEngine) {
        engine {
          dispatcher = UnconfinedTestDispatcher()
          addHandler { request ->
            if (UrlUtils.isYouTubeLink(request.url.toString())) {
              respond(
                content = ByteReadChannel(youtubeChannelHtml.toByteArray()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "html"),
              )
            } else {
              respond(
                content = ByteReadChannel("".toByteArray()),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(),
              )
            }
          }
        }
      }
    xmlFeedParser =
      XmlFeedParser(
        rdfContentParser = RDFContentParser(articleHtmlParser),
        rssContentParser = RSSContentParser(articleHtmlParser),
        atomContentParser = AtomContentParser(httpClient, articleHtmlParser),
        dispatchersProvider = TestDispatchersProvider(),
        platformPageSize = 4096L,
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
                rawContent =
                  """
                  <html>
                   <body>First post description.</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/first-post-media-url",
                date = 1685005200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Post with media thumbnail",
                link = "https://example.com/post-media-thumbnail",
                description = "Post with media thumbnail",
                rawContent =
                  """
                  <html>
                   <body>Post with media thumbnail</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/media/post-with-media-thumbnail",
                date = 1685005200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Post without image",
                link = "https://example.com/second-post",
                description = "Second post description.",
                rawContent =
                  """
                  <html>
                   <body>Second post description.</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = null,
                date = 1684999800000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Podcast post",
                link = "https://example.com/third-post",
                description = "Third post description.",
                rawContent =
                  """
                  <html>
                   <body>Third post description.</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = null,
                date = 1684924200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = "https://example.com/third-post",
              ),
              PostPayload(
                title = "Post with enclosure image",
                link = "https://example.com/fourth-post",
                description = "Fourth post description.",
                rawContent =
                  """
                  <html>
                   <body>Fourth post description.</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/enclosure-image",
                date = 1684924200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = "https://example.com/fourth-post",
              ),
              PostPayload(
                title = "Post with description and encoded content",
                link = "https://example.com/fifth-post",
                description = "Fourth post description in HTML syntax.",
                rawContent =
                  """
                  <html>
                   <body>
                    <p>Fourth post description in HTML syntax.</p>
                    <img src="https://example.com/encoded-image" alt="encoded image">
                   </body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/encoded-image",
                date = 1684924200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Post with relative path image",
                link = "https://example.com/post-with-relative-image",
                description = "Relative image post description.",
                rawContent =
                  """
                  <html>
                   <body>Relative image post description.</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "http://example.com/relative-media-url",
                date = 1685005200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Post with comments",
                link = "https://example.com/post-with-comments",
                description = "Really long post with comments.",
                rawContent =
                  """
                  <html>
                   <body>Really long post with comments.</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = null,
                date = 1685005200000,
                commentsLink = "https://example/post-with-comments/comments",
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Post with media group",
                link = "https://example.com/post-with-media-group",
                description = "Media group description",
                rawContent =
                  """
                  <html>
                   <body>Media group description</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/media/maxresdefault.jpg",
                date = 1685005200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Post with nested media content",
                link = "https://example.com/post-with-nested-media-content",
                description = "Nested media content description.",
                rawContent =
                  """
                  <html>
                   <body>Nested media content description.</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/media/nested-media-content",
                date = 1685005200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Post with media thumbnail as text",
                link = "https://example.com/post-with-media-thumbnail-text",
                description = "Media thumbnail text description.",
                rawContent =
                  """
                  <html>
                   <body>Media thumbnail text description.</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/media/thumbnail-as-text",
                date = 1685005200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Post after nested media content",
                link = "https://example.com/post-after-nested-media-content",
                description = "Post after nested media content description.",
                rawContent =
                  """
                  <html>
                   <body>Post after nested media content description.</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = null,
                date = 1685005200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
            )
            .asFlow(),
      )

    // when
    val content = ByteReadChannel(rssXmlContent.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)

    // then
    assertFeedPayloadEquals(expectedFeedPayload, payload)
  }

  @Test
  fun parsingRssFeedWithNestedMediaContentShouldNotTruncateItems() = runTest {
    // when
    val content = ByteReadChannel(rssXmlContentWithNestedMediaInFirstItem.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)
    val posts = payload.posts.toList()

    // then
    assertEquals(listOf("First post", "Second post", "Third post"), posts.map { it.title })
    assertEquals("https://example.com/media/first-post-140", posts.first().imageUrl)
  }

  @Test
  fun elementsBetweenTheFeedRootAndChannelShouldBeSkipped() = runTest {
    // when
    val content = ByteReadChannel(rssXmlContentWithElementBeforeChannel.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)
    val posts = payload.posts.toList()

    // then
    assertEquals("Feed title", payload.name)
    assertEquals(listOf("First post", "Second post"), posts.map { it.title })
  }

  @Test
  fun aFeedThatEndsBeforeItsChannelShouldFailInsteadOfHanging() = runTest {
    // given
    val truncatedXml =
      """<?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0">
        <script src="https://example.com/polyfill.js">"""

    // when
    val content = ByteReadChannel(truncatedXml.toByteArray())

    // then
    assertFailsWith<XmlParsingError> { xmlFeedParser.parse(content, feedUrl, Charsets.UTF8) }
  }

  @Test
  fun elementsBetweenTheRdfRootAndChannelShouldBeSkipped() = runTest {
    // when
    val content = ByteReadChannel(rdfXmlContentWithElementBeforeChannel.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)
    val posts = payload.posts.toList()

    // then
    assertEquals("Feed title", payload.name)
    assertEquals(listOf("Post"), posts.map { it.title })
  }

  @Test
  fun rssItemsLinkingToYouTubeShouldFallBackToTheVideoThumbnail() = runTest {
    // given
    val xmlWithYouTubeLinks =
      """<?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0"><channel>
          <title>Tom Scott's updates</title>
          <link>https://www.tomscott.com/</link>
          <description>Stuff on or around Tom Scott's web site</description>
          <item>
            <title>How do you keep 8,995 codebreakers secret?</title>
            <link>https://www.youtube.com/watch?v=tDLbO9KeddY</link>
            <description>How do you keep 8,995 codebreakers secret?</description>
            <pubDate>Mon, 24 Aug 2026 15:00:09 +0000</pubDate>
          </item>
          <item>
            <title>A post that is not a video</title>
            <link>https://www.tomscott.com/blog/post</link>
            <description>Body text</description>
            <pubDate>Mon, 17 Aug 2026 15:00:26 +0000</pubDate>
          </item>
        </channel></rss>"""

    // when
    val content = ByteReadChannel(xmlWithYouTubeLinks.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)
    val posts = payload.posts.toList()

    // then
    assertEquals("https://i.ytimg.com/vi/tDLbO9KeddY/maxresdefault.jpg", posts.first().imageUrl)
    assertEquals(null, posts.last().imageUrl)
  }

  @Test
  fun namedHtmlEntitiesShouldBeResolvedRatherThanDropped() = runTest {
    // given
    val xmlWithNamedEntities =
      """<?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0"><channel>
          <title>Feed &amp; Co &mdash; news</title>
          <link>https://example.com</link>
          <description>Desc &hellip; here</description>
          <item>
            <title>Caf&eacute; &mdash; it&rsquo;s &frac12; done &hellip;</title>
            <link>https://example.com/post</link>
            <pubDate>Wed, 12 Mar 2025 10:05:00 +0000</pubDate>
            <description>Body text</description>
          </item>
        </channel></rss>"""

    // when
    val content = ByteReadChannel(xmlWithNamedEntities.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)
    val post = payload.posts.toList().single()

    // then
    assertEquals("Feed & Co \u2014 news", payload.name)
    assertEquals("Desc \u2026 here", payload.description)
    assertEquals("Caf\u00e9 \u2014 it\u2019s \u00bd done \u2026", post.title)
  }

  @Test
  fun unknownEntitiesShouldNotBreakParsing() = runTest {
    // given
    val xmlWithUnknownEntity =
      """<?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0"><channel>
          <title>Feed</title>
          <link>https://example.com</link>
          <description>Desc</description>
          <item>
            <title>Known &amp; unknown&notarealentity; here</title>
            <link>https://example.com/post</link>
            <pubDate>Wed, 12 Mar 2025 10:05:00 +0000</pubDate>
            <description>Body text</description>
          </item>
        </channel></rss>"""

    // when
    val content = ByteReadChannel(xmlWithUnknownEntity.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)
    val post = payload.posts.toList().single()

    // then
    assertEquals("Known & unknown here", post.title)
  }

  @Test
  fun oversizedPostContentShouldStillYieldAnImageAndABoundedDescription() = runTest {
    // given
    val oversizedBody = buildString {
      append("""<figure><img src="https://example.com/hero.jpg"/></figure>""")
      while (length < 6 * 1024 * 1024) {
        append("<p>Long form paragraph text that keeps accumulating size for this post.</p>")
      }
    }
    val xml =
      """<?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">
        <channel><title>F</title><link>https://example.com</link><description>d</description>
        <item><title>Big post</title><link>https://example.com/big</link>
        <pubDate>Wed, 12 Mar 2025 10:05:00 +0000</pubDate>
        <content:encoded><![CDATA[$oversizedBody]]></content:encoded>
        </item></channel></rss>"""

    // when
    val payload = xmlFeedParser.parse(ByteReadChannel(xml.toByteArray()), feedUrl, Charsets.UTF8)
    val post = payload.posts.toList().single()

    // then
    assertEquals("https://example.com/hero.jpg", post.imageUrl)
    assertTrue(post.description.length < 16 * 1024)
    assertFalse(post.description.contains("<p>"))
    assertTrue(post.description.startsWith("Long form paragraph text"))
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
                rawContent =
                  """
                  <html>
                   <body>First post description.</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = null,
                date = 1685005200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Post with encoded description",
                link = "https://example.com/second-post",
                description = "Second post description in HTML syntax.",
                rawContent =
                  """
                  <html>
                   <body>
                    <p>Second post description in HTML syntax.</p>
                    <img src="https://example.com/encoded-image" alt="encoded image">
                   </body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/encoded-image",
                date = 1684924200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
            )
            .asFlow(),
      )

    // when
    val content = ByteReadChannel(rdfXmlContent.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)

    // then
    assertFeedPayloadEquals(expectedFeedPayload, payload)
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
                  <html>
                   <body>
                    <img alt="First Image" src="https://example.com/image.jpg">
                    <p>Post summary with an image.</p>
                   </body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/image.jpg",
                date = 1685008800000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Second post",
                link = "https://example.com/second-post",
                description = "Post summary of the second post.",
                rawContent =
                  """
                  <html>
                   <body>
                    <p>Post summary of the second post.</p>
                   </body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = null,
                date = 1684917000000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = "https://example.com/second-post-audio.mp3",
              ),
              PostPayload(
                title = "Post without image",
                link = "https://example.com/third-post",
                description = "Post summary of the third post. click here.",
                rawContent =
                  """
                  <html>
                   <body>
                    <p>Post summary of the third post. <a href="https://example.com/hyperlink">click here</a>.</p>
                   </body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = null,
                date = 1684936800000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
              PostPayload(
                title = "Post with relative image",
                link = "https://example.com/relative-image-post",
                description = "Post summary with an image.",
                rawContent =
                  """
                  <html>
                   <body>
                    <img alt="Relative Image" src="/resources/image.jpg">
                    <p>Post summary with an image.</p>
                   </body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "http://example.com/resources/image.jpg",
                date = 1685008800000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              ),
            )
            .asFlow(),
      )

    // when
    val content = ByteReadChannel(atomXmlContent.toByteArray())
    val payload = xmlFeedParser.parse(content, feedUrl, Charsets.UTF8)

    // then
    assertFeedPayloadEquals(expectedFeedPayload, payload)
  }

  @Test
  fun parsingYouTubeUploadsPlaylistFeedShouldUseTheChannelNameAndPage() = runTest {
    // given
    val expectedFeedPayload =
      FeedPayload(
        name = "Google Developers",
        icon = "https://youtube.com/img/channel.jpg",
        coverArtUrl = "https://youtube.com/img/channel.jpg",
        description = "",
        link = youtubeUploadsPlaylistFeedUrl,
        homepageLink = "https://www.youtube.com/channel/UC_x5XG1OV2P6uZZ5FSM9Ttw",
        posts =
          listOf(
              PostPayload(
                title =
                  "Android Beyond Phones: A New Way to Build with Jetpack Compose | Android Dev Summit '23",
                link = "https://www.youtube.com/watch?v=2QpWq3iQdC4",
                description = "Subscribe to watch more videos about Android development",
                rawContent = null,
                fullContent = null,
                imageUrl = "https://i.ytimg.com/vi/2QpWq3iQdC4/maxresdefault.jpg",
                date = 1698260988000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              )
            )
            .asFlow(),
      )

    // when
    val content = ByteReadChannel(youtubeUploadsPlaylistAtomFeed.toByteArray())
    val payload = xmlFeedParser.parse(content, youtubeUploadsPlaylistFeedUrl, Charsets.UTF8)

    // then
    assertFeedPayloadEquals(expectedFeedPayload, payload)
  }

  @Test
  fun parsingYouTubeAtomFeedShouldWorkCorrectly() = runTest {
    // given
    val expectedFeedPayload =
      FeedPayload(
        name = "Google Developers",
        icon = "https://youtube.com/img/channel.jpg",
        coverArtUrl = "https://youtube.com/img/channel.jpg",
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
                fullContent = null,
                imageUrl = "https://i.ytimg.com/vi/2QpWq3iQdC4/maxresdefault.jpg",
                date = 1698260988000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = null,
              )
            )
            .asFlow(),
      )

    // when
    val content = ByteReadChannel(youtubeAtomFeed.toByteArray())
    val payload = xmlFeedParser.parse(content, youtubeFeedUrl, Charsets.UTF8)

    // then
    assertFeedPayloadEquals(expectedFeedPayload, payload)
  }

  @Test
  fun parsingRssFeedWithItunesImageShouldWorkCorrectly() = runTest {
    // given
    val expectedFeedPayload =
      FeedPayload(
        name = "Podcast title",
        icon = "https://example.com/podcast-icon.jpg",
        coverArtUrl = "https://example.com/podcast-icon.jpg",
        description = "Podcast description",
        link = podcastRssFeedUrl,
        homepageLink = "https://example.com/podcast",
        posts =
          listOf(
              PostPayload(
                title = "Episode 1",
                link = "https://example.com/episode-1",
                description = "Episode 1 description",
                rawContent =
                  """
                  <html>
                   <body>Episode 1 description</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/episode-1-image.jpg",
                date = 1685005200000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = "https://example.com/episode-1.mp3",
              )
            )
            .asFlow(),
      )

    // when
    val content = ByteReadChannel(podcastRssXmlContent.toByteArray())
    val payload = xmlFeedParser.parse(content, podcastRssFeedUrl, Charsets.UTF8)

    // then
    assertFeedPayloadEquals(expectedFeedPayload, payload)
  }

  @Test
  fun parsingAtomFeedWithItunesImageShouldWorkCorrectly() = runTest {
    // given
    val expectedFeedPayload =
      FeedPayload(
        name = "Podcast title",
        icon = "https://example.com/podcast-icon.jpg",
        coverArtUrl = "https://example.com/podcast-icon.jpg",
        description = "Podcast description",
        link = podcastAtomFeedUrl,
        homepageLink = "https://example.com/podcast",
        posts =
          listOf(
              PostPayload(
                title = "Episode 1",
                link = "https://example.com/episode-1",
                description = "Episode 1 description",
                rawContent =
                  """
                  <html>
                   <body>Episode 1 description</body>
                  </html>
                  """
                    .trimIndent(),
                fullContent = null,
                imageUrl = "https://example.com/episode-1-image.jpg",
                date = 1685008800000,
                commentsLink = null,
                isDateParsedCorrectly = true,
                audioUrl = "https://example.com/episode-1.mp3",
              )
            )
            .asFlow(),
      )

    // when
    val content = ByteReadChannel(podcastAtomXmlContent.toByteArray())
    val payload = xmlFeedParser.parse(content, podcastAtomFeedUrl, Charsets.UTF8)

    // then
    assertFeedPayloadEquals(expectedFeedPayload, payload)
  }

  @Test
  fun parsingRssItemWithBothDescriptionAndContentEncodedPreservesFullDescriptionAndFallbackImage() =
    runTest {
      val longBody = "a".repeat(4095) + "\uD83D\uDE80 tail-keyword " + "a".repeat(1000)
      val rssXml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">
          <channel>
            <title>Feed</title>
            <link>https://example.com</link>
            <description>Feed desc</description>
            <item>
              <title>Long Post</title>
              <link>https://example.com/long-post</link>
              <description><![CDATA[<p>Summary</p><img src="https://example.com/fallback.jpg" />]]></description>
              <content:encoded><![CDATA[<p>$longBody</p>]]></content:encoded>
              <pubDate>Thu, 25 May 2023 09:00:00 +0000</pubDate>
            </item>
          </channel>
        </rss>
        """
          .trimIndent()

      val payload =
        xmlFeedParser.parse(ByteReadChannel(rssXml.toByteArray()), feedUrl, Charsets.UTF8)
      val posts = payload.posts.toList()

      assertEquals(1, posts.size)
      val post = posts.single()
      assertEquals(longBody, post.description)
      assertTrue((post.rawContent ?: "").contains(longBody))
      assertEquals("https://example.com/fallback.jpg", post.imageUrl)
    }

  @Test
  fun parsingAtomEntryWithSummaryAndContentPreservesFullDescriptionAndPrefersContent() = runTest {
    val longBody = "b".repeat(4095) + "\uD83D\uDE80 atom-tail-keyword " + "b".repeat(1000)
    val atomXml =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <feed xmlns="http://www.w3.org/2005/Atom">
        <title>Atom Feed</title>
        <link rel="alternate" href="https://example.com" />
        <entry>
          <title>Atom Long Post</title>
          <link rel="alternate" href="https://example.com/atom-long-post" />
          <published>2023-05-25T09:00:00Z</published>
          <summary type="html"><![CDATA[<p>Short summary</p><img src="https://example.com/summary.jpg" />]]></summary>
          <content type="html"><![CDATA[<p>$longBody</p>]]></content>
        </entry>
      </feed>
      """
        .trimIndent()

    val payload =
      xmlFeedParser.parse(ByteReadChannel(atomXml.toByteArray()), feedUrl, Charsets.UTF8)
    val posts = payload.posts.toList()

    assertEquals(1, posts.size)
    val post = posts.single()
    assertEquals(longBody, post.description)
    assertTrue((post.rawContent ?: "").contains(longBody))
    assertEquals("https://example.com/summary.jpg", post.imageUrl)
  }

  @Test
  fun resolveDeferredPostContentSkipsSecondaryParseWhenPrimaryHasHeroImageAndSecondaryLacksAudio() {
    var parseCount = 0
    val probe =
      object : XmlContentParser() {
        override val articleHtmlParser = ArticleHtmlParser()

        override suspend fun parse(
          feedUrl: String,
          parser: org.kobjects.ktxml.api.XmlPullParser,
        ): FeedPayload = error("unused")

        override fun parsePostContentText(postHtmlContent: String): PostContent {
          parseCount++
          return super.parsePostContentText(postHtmlContent)
        }

        fun resolve(primary: String?, secondary: String?): Pair<String?, String?>? {
          val result = resolveDeferredPostContent(primary, secondary) ?: return null
          return result.heroImage to result.audioUrl
        }
      }

    val noAudioSecondaryResult =
      probe.resolve(
        primary = """<p>Body</p><img src="https://example.com/primary.jpg" />""",
        secondary = """<p>Summary</p><img src="https://example.com/secondary.jpg" />""",
      )
    assertEquals(1, parseCount)
    assertEquals("https://example.com/primary.jpg" to null, noAudioSecondaryResult)

    parseCount = 0
    val audioSecondaryResult =
      probe.resolve(
        primary = """<p>Body</p><img src="https://example.com/primary.jpg" />""",
        secondary =
          """<p>Summary</p><audio><source src="https://example.com/episode.mp3" type="audio/mpeg" /></audio>""",
      )
    assertEquals(2, parseCount)
    assertEquals(
      "https://example.com/primary.jpg" to "https://example.com/episode.mp3",
      audioSecondaryResult,
    )

    parseCount = 0
    val fallbackImageResult =
      probe.resolve(
        primary = """<p>Body without image</p>""",
        secondary = """<p>Summary</p><img src="https://example.com/secondary.jpg" />""",
      )
    assertEquals(2, parseCount)
    assertEquals("https://example.com/secondary.jpg" to null, fallbackImageResult)
  }

  @Test
  fun parsingRssItemWithPrimaryHeroImageAndSecondaryAudioExtractsBoth() = runTest {
    val rssXml =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">
        <channel>
          <title>Feed</title>
          <link>https://example.com</link>
          <description>Feed desc</description>
          <item>
            <title>Audio Post</title>
            <link>https://example.com/audio-post</link>
            <description><![CDATA[<p>Summary</p><img src="https://example.com/summary.jpg" /><audio><source src="https://example.com/episode.mp3" type="audio/mpeg" /></audio>]]></description>
            <content:encoded><![CDATA[<p>Full article body</p><img src="https://example.com/content.jpg" />]]></content:encoded>
            <pubDate>Thu, 25 May 2023 09:00:00 +0000</pubDate>
          </item>
        </channel>
      </rss>
      """
        .trimIndent()

    val payload = xmlFeedParser.parse(ByteReadChannel(rssXml.toByteArray()), feedUrl, Charsets.UTF8)
    val post = payload.posts.toList().single()

    assertEquals("Full article body", post.description)
    assertEquals("https://example.com/content.jpg", post.imageUrl)
    assertEquals("https://example.com/episode.mp3", post.audioUrl)
  }

  @Test
  fun parsingAtomFeedWithXhtmlContentAndSummaryShouldWorkCorrectly() = runTest {
    val atomXml =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <feed xmlns="http://www.w3.org/2005/Atom">
        <id>https://example.com/releases#changelog</id>
        <link href="https://example.com/releases#changelog" />
        <title>Changelog</title>
        <updated>2026-11-13T00:00:00Z</updated>
        <entry>
          <id>https://example.com/releases#2026111300</id>
          <link href="https://example.com/releases#2026111300" />
          <title>2026111300</title>
          <updated>2026-11-13T00:00:00Z</updated>
          <published>2026-11-13T00:00:00Z</published>
          <content type="xhtml"><div xmlns="http://www.w3.org/1999/xhtml"><p>Tags:</p><ul><li><a href="https://example.com/tag/2026111300">2026111300</a> (Pixel &amp; generic)</li></ul><p>1 &lt; 2 &amp; 3 &gt; 0</p><br /><img src="https://example.com/release.png" alt="Release &quot;1&quot;" /></div></content>
        </entry>
        <entry>
          <id>https://example.com/releases#2026110800</id>
          <link href="https://example.com/releases#2026110800" />
          <title>2026110800</title>
          <updated>2026-11-08T00:00:00Z</updated>
          <summary type="xhtml"><div xmlns="http://www.w3.org/1999/xhtml"><p>Previous release summary</p></div></summary>
        </entry>
      </feed>
      """
        .trimIndent()

    val payload =
      xmlFeedParser.parse(ByteReadChannel(atomXml.toByteArray()), feedUrl, Charsets.UTF8)
    val posts = payload.posts.toList()

    assertEquals(2, posts.size)

    val first = posts[0]
    assertEquals("2026111300", first.title)
    assertEquals("https://example.com/releases#2026111300", first.link)
    assertTrue(first.description.contains("Tags:"))
    assertTrue(first.description.contains("2026111300 (Pixel & generic)"))
    assertTrue(first.description.contains("1 < 2 & 3 > 0"))
    assertTrue((first.rawContent ?: "").contains("<p>Tags:</p>"))
    assertTrue(
      (first.rawContent ?: "").contains(
        "<a href=\"https://example.com/tag/2026111300\">2026111300</a>"
      )
    )
    assertTrue((first.rawContent ?: "").contains("1 &lt; 2 &amp; 3 &gt; 0"))
    assertEquals("https://example.com/release.png", first.imageUrl)

    val second = posts[1]
    assertEquals("2026110800", second.title)
    assertEquals("Previous release summary", second.description)
    assertTrue((second.rawContent ?: "").contains("<p>Previous release summary</p>"))
  }

  @Test
  fun rssChannelArtworkPriorityRespectsItunesPodcastGoogleplayAndDoesNotOverwriteWithImage() =
    runTest {
      val rssXml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" xmlns:podcast="https://podcastindex.org/namespace/1.0" xmlns:googleplay="http://www.google.com/schemas/play-podcasts/1.0">
          <channel>
            <title>Show</title>
            <link>https://example.com</link>
            <description>Podcast show</description>
            <itunes:image href="https://example.com/itunes-3000.jpg" />
            <podcast:images srcset="https://example.com/small.jpg 300w, https://example.com/large.jpg 1500w" />
            <googleplay:image href="https://example.com/gplay.jpg" />
            <image>
              <url>https://example.com/channel-logo.png</url>
              <title>Show</title>
              <link>https://example.com</link>
            </image>
            <item>
              <title>Ep 1</title>
              <link>https://example.com/ep1</link>
              <description><![CDATA[<img src="https://example.com/tracker.png" /><p>Notes</p>]]></description>
              <media:content url="https://example.com/ep1.mp3" type="audio/mpeg" medium="audio" />
              <itunes:image href="https://example.com/ep1-cover.jpg" />
              <enclosure url="https://example.com/ep1-enclosure.mp3" type="audio/mpeg" />
            </item>
            <item>
              <title>Ep 2</title>
              <description>Only image enclosure and audio enclosure</description>
              <enclosure url="https://example.com/ep2-cover.png" type="image/png" />
              <enclosure url="https://example.com/ep2.mp3" type="audio/mpeg" />
            </item>
            <item>
              <title>Ep 3</title>
              <link>https://example.com/ep3</link>
              <description>Video media content</description>
              <media:content url="https://example.com/ep3.mp4" type="video/mp4" medium="video" />
              <podcast:images srcset="https://substackcdn.com/image/fetch/w_300,c_limit/a.jpg 300w, https://substackcdn.com/image/fetch/w_1400,c_limit/a.jpg 1400w" />
            </item>
            <item>
              <title>Ep 4</title>
              <link>https://example.com/ep4</link>
              <media:group>
                <media:description>Group notes</media:description>
                <media:content url="https://example.com/ep4-group.jpg" medium="image" />
                <media:content url="https://example.com/ep4-group.mp3" type="audio/mpeg" />
              </media:group>
            </item>
          </channel>
        </rss>
        """
          .trimIndent()

      val payload =
        xmlFeedParser.parse(ByteReadChannel(rssXml.toByteArray()), feedUrl, Charsets.UTF8)
      val posts = payload.posts.toList()

      assertEquals("https://example.com/itunes-3000.jpg", payload.coverArtUrl)
      assertEquals("https://example.com/itunes-3000.jpg", payload.icon)

      assertEquals("https://example.com/ep1-cover.jpg", posts[0].imageUrl)
      assertEquals("https://example.com/ep1-enclosure.mp3", posts[0].audioUrl)

      assertEquals("https://example.com/ep2-cover.png", posts[1].imageUrl)
      assertEquals("https://example.com/ep2.mp3", posts[1].link)
      assertEquals("https://example.com/ep2.mp3", posts[1].audioUrl)

      assertEquals("https://substackcdn.com/image/fetch/w_1400,c_limit/a.jpg", posts[2].imageUrl)
      assertEquals(null, posts[2].audioUrl)

      assertEquals("https://example.com/ep4-group.jpg", posts[3].imageUrl)
      assertEquals("https://example.com/ep4-group.mp3", posts[3].audioUrl)
      assertEquals("Group notes", posts[3].description)
    }

  @Test
  fun atomFeedArtworkNeverUsesIconAsCoverArtAndSupportsEnclosureImages() = runTest {
    val atomXml =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <feed xmlns="http://www.w3.org/2005/Atom" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
        <title>Atom Show</title>
        <link href="https://example.com" rel="alternate" />
        <icon>https://example.com/small-favicon.png</icon>
        <entry>
          <title>Entry 1</title>
          <link rel="alternate" href="https://example.com/e1" />
          <link rel="enclosure" type="image/webp" href="https://example.com/e1-art.webp" />
          <content type="html"><![CDATA[<img src="https://example.com/inline.jpg"/><p>Text</p>]]></content>
        </entry>
      </feed>
      """
        .trimIndent()

    val payload =
      xmlFeedParser.parse(ByteReadChannel(atomXml.toByteArray()), feedUrl, Charsets.UTF8)
    val post = payload.posts.toList().single()

    assertEquals(null, payload.coverArtUrl)
    assertEquals("https://example.com/small-favicon.png", payload.icon)
    assertEquals("https://example.com/e1-art.webp", post.imageUrl)
  }

  @Test
  fun rdfFeedImageSetsCoverArtUrl() = runTest {
    val rdfXml =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
        <channel>
          <title>RDF Feed</title>
          <link>https://example.com</link>
          <description>RDF Desc</description>
          <image rdf:resource="https://example.com/rdf-cover.png" />
        </channel>
        <item>
          <title>Item</title>
          <link>https://example.com/item</link>
          <description>Body</description>
        </item>
      </rdf:RDF>
      """
        .trimIndent()

    val payload = xmlFeedParser.parse(ByteReadChannel(rdfXml.toByteArray()), feedUrl, Charsets.UTF8)

    assertEquals("https://example.com/rdf-cover.png", payload.coverArtUrl)
    assertEquals("https://example.com/rdf-cover.png", payload.icon)
  }
}
