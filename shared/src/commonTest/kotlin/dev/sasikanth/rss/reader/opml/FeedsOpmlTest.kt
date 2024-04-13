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

package dev.sasikanth.rss.reader.opml

import dev.sasikanth.rss.reader.core.model.local.Feed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.Instant

class FeedsOpmlTest {

  private val feedsOpml = FeedsOpml()

  @Test
  fun encodingFeedsToOpmlShouldWorkCorrectly() {
    // given
    val feeds =
      listOf(
        Feed(
          id = "e8d31cec-2893-54d0-bcae-7f134713e532",
          name = "The Verge",
          icon = "https://icon.horse/icon/theverge.com",
          description = "The Verge",
          homepageLink = "https://theverge.com",
          createdAt = Instant.parse("2018-01-01T00:00:00Z"),
          link = "https://www.theverge.com/rss/index.xml",
          pinnedAt = null
        ),
        Feed(
          id = "c90003bd-b1e6-5545-ba59-3d2128d658a7",
          name = "Hacker News",
          icon = "https://icon.horse/icon/news.ycombinator.com",
          description = "Hacker News",
          homepageLink = "https://news.ycombinator.com",
          createdAt = Instant.parse("2018-01-01T00:00:00Z"),
          link = "https://news.ycombinator.com/rss",
          pinnedAt = null
        ),
      )

    // when
    val opmlXml = feedsOpml.encode(feeds)

    // then
    val expected =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <opml version="2.0">
        <head>
          <title>Twine RSS Feeds</title>
        </head>
        <body>
          <outline title="The Verge" text="The Verge" type="rss" xmlUrl="https://www.theverge.com/rss/index.xml" />
          <outline title="Hacker News" text="Hacker News" type="rss" xmlUrl="https://news.ycombinator.com/rss" />
        </body>
      </opml>

    """
        .trimIndent()

    assertEquals(expected, opmlXml)
  }

  @Test
  fun decodingOpmlXmlToOpmlFeedsShouldWorkCorrectly() {
    // given
    val xml =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <opml version="2.0">
        <head>
          <title>Twine RSS Feeds</title>
        </head>
        <body>
          <outline title="Tech">
            <outline title="The Verge" text="The Verge" type="rss" xmlUrl="https://www.theverge.com/rss/index.xml" />
            <outline title="Hacker News" text="Hacker News" type="rss" xmlUrl="https://news.ycombinator.com/rss" />
          </outline>
          <outline title="NYT" text="NYT" type="rss" xmlUrl="https://www.nytimes.com/svc/collections/v1/publish/https://www.nytimes.com/section/world/rss.xml" />
        </body>
      </opml>

    """
        .trimIndent()

    // when
    val opmlFeeds = feedsOpml.decode(xml)

    // then
    val expected =
      listOf(
        OpmlFeed(title = "The Verge", link = "https://www.theverge.com/rss/index.xml"),
        OpmlFeed(title = "Hacker News", link = "https://news.ycombinator.com/rss"),
        OpmlFeed(
          title = "NYT",
          link =
            "https://www.nytimes.com/svc/collections/v1/publish/https://www.nytimes.com/section/world/rss.xml"
        ),
      )

    assertEquals(expected, opmlFeeds)
  }
}
