package dev.sasikanth.rss.reader.network

import dev.sasikanth.rss.reader.models.FeedPayload
import dev.sasikanth.rss.reader.models.PostPayload
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidFeedParserTest {

  private val feedParser = AndroidFeedParser(ioDispatcher = UnconfinedTestDispatcher())

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
              imageUrl = "https://example.com/first-post-media-url",
              date = 1685005200000
            ),
            PostPayload(
              title = "Post without image",
              link = "https://example.com/second-post",
              description = "Second post description.",
              imageUrl = null,
              date = 1684999800000
            ),
            PostPayload(
              title = "Podcast post",
              link = "https://example.com/third-post",
              description = "Third post description.",
              imageUrl = null,
              date = 1684924200000
            ),
          )
      )

    // when
    val payload = feedParser.parse(rssXmlContent, feedUrl)

    // then
    assertEquals(expectedFeedPayload, payload)
  }

  companion object {

    private const val feedUrl = "https://example.com"
    private const val rssXmlContent =
      """
      <rss version="2.0">
        <channel>
          <title>Feed title</title>
          <link>https://example.com</link>
          <description>Feed description</description>
          <item>
            <title>Post with image</title>
            <link>https://example.com/first-post</link>
            <description>First post description.</description>
            <pubDate>Thu, 25 May 2023 09:00:00 +0000</pubDate>
            <media:content url="https://example.com/first-post-media-url" />
          </item>
          <item>
            <title>Post without image</title>
            <link>https://example.com/second-post</link>
            <description>Second post description.</description>
            <pubDate>Thu, 25 May 2023 07:30:00 +0000</pubDate>
          </item>
          <item>
            <title>Podcast post</title>
            <description>Third post description.</description>
            <pubDate>Wed, 24 May 2023 10:30:00 +0000</pubDate>
            <enclosure url="https://example.com/third-post" />
          </item>
        </channel>
      </rss>
      """
  }
}
