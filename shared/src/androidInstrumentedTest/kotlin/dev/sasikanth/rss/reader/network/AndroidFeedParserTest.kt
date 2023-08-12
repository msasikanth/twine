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
            PostPayload(
              title = "Post with enclosure image",
              link = "https://example.com/fourth-post",
              description = "Fourth post description.",
              imageUrl = "https://example.com/enclosure-image",
              date = 1684924200000
            ),
          )
      )

    // when
    val payload = feedParser.parse(rssXmlContent, feedUrl)

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
              imageUrl = "https://example.com/image.jpg",
              date = 1685008800000
            ),
            PostPayload(
              title = "Second post",
              link = "https://example.com/second-post",
              description = "Post summary of the second post.",
              imageUrl = null,
              date = 1684917000000
            ),
            PostPayload(
              title = "Post without image",
              link = "https://example.com/third-post",
              description = "Post summary of the third post.",
              imageUrl = null,
              date = 1684936800000
            ),
          )
      )

    // when
    val payload = feedParser.parse(atomXmlContent, feedUrl)

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
          <item>
            <title>Post with enclosure image</title>
            <description>Fourth post description.</description>
            <pubDate>Wed, 24 May 2023 10:30:00 +0000</pubDate>
            <enclosure url="https://example.com/fourth-post" />
            <enclosure type="image/jpeg" url="https://example.com/enclosure-image" />
          </item>
        </channel>
      </rss>
      """
    private const val atomXmlContent =
      """
        <feed xmlns="http://www.w3.org/2005/Atom">
        <title>Feed title</title>
        <subtitle>Feed description</subtitle>
        <link href="https://example.com" rel="alternate" />
        <entry>
          <title>Post with image</title>
          <link rel="alternate" href="https://example.com/first-post" />
          <published>2023-05-25T10:00:00Z</published>
          <content type="html">
          
          &lt;img alt="First Image"
            src="https://example.com/image.jpg"
            /&gt;
          
          &lt;p&gt;Post summary with an image.&lt;/p&gt;
          
          </content>
        </entry>
        <entry>
          <title>Second post</title>
          <link rel="alternate" href="https://example.com/second-post" />
          <published>2023-05-24T08:30:00Z</published>
          <content type="html">

          &lt;p&gt;Post summary of the second post.&lt;/p&gt;
          
          </content>
        </entry>
        <entry>
          <title>Post without image</title>
          <link rel="alternate" href="https://example.com/third-post" />
          <published>2023-05-24T14:00:00Z</published>
          <content type="html">

          &lt;p&gt;Post summary of the third post.&lt;/p&gt;
          
          </content>
        </entry>
        </feed>
      """
  }
}
