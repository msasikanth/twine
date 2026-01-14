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

package dev.sasikanth.rss.reader.core.network.parser.xml

import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_RDF_RESOURCE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_CONTENT_ENCODED
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_DC_DATE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_DESCRIPTION
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_FEED_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_PUB_DATE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_RSS_CHANNEL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_RSS_ITEM
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_TITLE
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import me.tatarka.inject.annotations.Inject
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser

@Inject
class RDFContentParser(
  override val articleHtmlParser: ArticleHtmlParser,
) : XmlContentParser() {

  override suspend fun parse(feedUrl: String, parser: XmlPullParser): FeedPayload {
    parser.nextTag()
    parser.require(EventType.START_TAG, parser.namespace, TAG_RSS_CHANNEL)

    var title: String? = null
    var link: String? = null
    var description: String? = null
    var iconUrl: String? = null

    // Parse channel
    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue

      when (parser.name) {
        TAG_TITLE -> {
          title = parser.nextText()
        }
        TAG_LINK -> {
          if (link.isNullOrBlank()) {
            link = parser.nextText()
          } else {
            parser.skipSubTree()
          }
        }
        TAG_DESCRIPTION -> {
          description = parser.nextText()
        }
        TAG_FEED_IMAGE -> {
          iconUrl = readFeedIcon(parser)
        }
        else -> parser.skipSubTree()
      }
    }

    return createFeedPayload(
      name = title,
      description = description,
      icon = iconUrl,
      homepageLink = link,
      link = feedUrl,
      posts =
        postsFlow(
          parser = parser,
          itemTag = TAG_RSS_ITEM,
          readItem = { readRssItem(it, UrlUtils.extractHost(link ?: feedUrl)) }
        )
    )
  }

  private fun readFeedIcon(parser: XmlPullParser): String? {
    parser.require(EventType.START_TAG, parser.namespace, TAG_FEED_IMAGE)
    val link = parser.getAttributeValue(parser.namespace, ATTR_RDF_RESOURCE)
    parser.nextTag()
    parser.require(EventType.END_TAG, parser.namespace, TAG_FEED_IMAGE)
    return link
  }

  private fun readRssItem(parser: XmlPullParser, hostLink: String?): PostPayload? {
    parser.require(EventType.START_TAG, parser.namespace, TAG_RSS_ITEM)

    var title: String? = null
    var link: String? = null
    var description: String? = null
    var rawContent: String? = null
    var date: String? = null
    var image: String? = null

    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue
      val name = parser.name

      when {
        name == TAG_TITLE -> {
          title = parser.nextText()
        }
        link.isNullOrBlank() && name == TAG_LINK -> {
          link = parser.nextText()
        }
        name == TAG_DESCRIPTION || name == TAG_CONTENT_ENCODED -> {
          val postContent = parsePostContent(parser)

          rawContent = postContent.rawContent
          image = postContent.heroImage ?: image
          description = postContent.textContent
        }
        name == TAG_PUB_DATE || name == TAG_DC_DATE -> {
          date = parser.nextText()
        }
        else -> parser.skipSubTree()
      }
    }

    return createPostPayload(
      title = title,
      link = link,
      description = description,
      rawContent = rawContent,
      imageUrl = image,
      date = date,
      hostLink = hostLink
    )
  }
}
