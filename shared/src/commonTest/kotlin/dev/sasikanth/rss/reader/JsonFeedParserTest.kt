/*
 * Copyright 2025 Sasikanth Miriyampalli
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
import dev.sasikanth.rss.reader.core.network.parser.json.JsonFeedParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class JsonFeedParserTest {

  private val parser = JsonFeedParser(TestDispatchersProvider())

  @Test
  fun parsingJsonFeedShouldWorkCorrectly() = runTest {
    // given
    val expectedFeed =
      FeedPayload(
        name = "Tech Insights Blog",
        icon = "https://example.com/icon.png",
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
                "Quantum computing has made significant strides in the past year. Recent breakthroughs at IBM and Google have pushed the boundaries of what we thought possible.\n\nThe new 1000-qubit processor announced last month represents a major milestone in the industry.",
              imageUrl = "https://example.com/images/quantum-2025.jpg",
              date = 1740734100000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "AI Ethics Frameworks: A Comparative Analysis",
              link = "https://example.com/posts/ai-ethics-frameworks",
              description =
                "Comparing different approaches to AI ethics across industry and government",
              rawContent =
                "<p>As AI becomes more integrated into our daily lives, the need for robust ethical frameworks has never been more important.</p><p>This article examines the approaches taken by major tech companies and governments around the world.</p>",
              imageUrl = null,
              date = 1739629800000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "The Rise of Edge Computing",
              link = "https://example.com/posts/edge-computing-rise",
              description =
                "Edge computing continues to grow as IoT devices proliferate. This shift is changing how we think about network architecture and data processing.\n\nIn this article, we explore the implications for businesses and consumers alike.",
              rawContent =
                "Edge computing continues to grow as IoT devices proliferate. This shift is changing how we think about network architecture and data processing.\n\nIn this article, we explore the implications for businesses and consumers alike.",
              imageUrl = "https://example.com/images/edge-computing.jpg",
              date = 1738410300000,
              commentsLink = null,
              isDateParsedCorrectly = true
            ),
            PostPayload(
              title = "Sustainable Tech: Green Innovations in Silicon Valley",
              link = "https://example.com/posts/sustainable-tech-innovations",
              description =
                "Silicon Valley companies are leading the charge in sustainable technology development.\n\nFrom carbon-neutral data centers to biodegradable electronics, we look at the most promising initiatives.",
              rawContent =
                "Silicon Valley companies are leading the charge in sustainable technology development.\n\nFrom carbon-neutral data centers to biodegradable electronics, we look at the most promising initiatives.",
              imageUrl = null,
              date = 1737388800000,
              commentsLink = null,
              isDateParsedCorrectly = true
            )
          )
      )

    // when
    val payload = parser.parse(jsonFeed, "https://example.com/feed.json")

    // then
    assertEquals(expectedFeed, payload)
  }
}
