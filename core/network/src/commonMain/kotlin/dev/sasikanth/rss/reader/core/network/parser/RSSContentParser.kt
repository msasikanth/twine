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

import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.ATTR_TYPE
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.ATTR_URL
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.ATTR_VALUE_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_COMMENTS
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_CONTENT_ENCODED
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_DESCRIPTION
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_ENCLOSURE
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_FEATURED_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_FEED_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_PUB_DATE
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_RSS_CHANNEL
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_RSS_ITEM
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_TITLE
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser.Companion.TAG_URL
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.util.dateStringToEpochMillis
import dev.sasikanth.rss.reader.util.decodeHTMLString
import kotlinx.datetime.Clock
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser

internal object RSSContentParser : XmlContentParser() {

  override fun parse(feedUrl: String, parser: XmlPullParser): FeedPayload {
    parser.nextTag()
    parser.require(EventType.START_TAG, parser.namespace, TAG_RSS_CHANNEL)

    val posts = mutableListOf<PostPayload?>()

    var title: String? = null
    var link: String? = null
    var description: String? = null
    var iconUrl: String? = null

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
            parser.skip()
          }
        }
        TAG_DESCRIPTION -> {
          description = parser.nextText()
        }
        TAG_RSS_ITEM -> {
          val host = UrlUtils.extractHost(link ?: feedUrl)
          posts.add(readRssItem(parser, host))
        }
        TAG_FEED_IMAGE -> {
          iconUrl = readFeedIcon(parser)
        }
        else -> parser.skip()
      }
    }

    val host = UrlUtils.extractHost(link ?: feedUrl)
    if (iconUrl.isNullOrBlank()) {
      iconUrl = XmlFeedParser.fallbackFeedIcon(host)
    }

    return FeedPayload(
      name = XmlFeedParser.cleanText(title ?: link)!!.decodeHTMLString(),
      description = XmlFeedParser.cleanText(description).orEmpty().decodeHTMLString(),
      icon = iconUrl,
      homepageLink = link ?: feedUrl,
      link = feedUrl,
      posts = posts.filterNotNull()
    )
  }

  private fun readFeedIcon(parser: XmlPullParser): String? {
    parser.require(EventType.START_TAG, parser.namespace, TAG_FEED_IMAGE)

    var imageUrl: String? = null

    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue
      if (parser.name == TAG_URL) {
        imageUrl = parser.nextText()
      } else {
        parser.skip()
      }
    }

    return imageUrl
  }

  private fun readRssItem(parser: XmlPullParser, hostLink: String?): PostPayload? {
    parser.require(EventType.START_TAG, parser.namespace, TAG_RSS_ITEM)

    var title: String? = null
    var link: String? = null
    var description: String? = null
    var rawContent: String? = null
    var date: String? = null
    var image: String? = null
    var commentsLink: String? = null

    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue
      val name = parser.name

      when {
        name == TAG_TITLE -> {
          title = parser.nextText()
        }
        link.isNullOrBlank() && (name == TAG_LINK || name == TAG_URL) -> {
          link = parser.nextText()
        }
        name == TAG_ENCLOSURE && link.isNullOrBlank() -> {
          link = parser.attrText(ATTR_URL)
        }
        name == TAG_DESCRIPTION || name == TAG_CONTENT_ENCODED -> {
          rawContent = parser.nextText().trimIndent()

          val htmlContent = HtmlContentParser.parse(htmlContent = rawContent)
          image = htmlContent?.leadImage ?: image
          description = htmlContent?.content?.ifBlank { null } ?: rawContent.trim()
        }
        name == TAG_PUB_DATE -> {
          date = parser.nextText()
        }
        image.isNullOrBlank() && hasRssImageUrl(name, parser) -> {
          image = parser.attrText(ATTR_URL)
        }
        image.isNullOrBlank() && name == TAG_FEATURED_IMAGE -> {
          image = parser.nextText()
        }
        commentsLink.isNullOrBlank() && name == TAG_COMMENTS -> {
          commentsLink = parser.nextText()
        }
        else -> parser.skip()
      }
    }

    val postPubDateInMillis = date?.dateStringToEpochMillis()

    if (link.isNullOrBlank() || (title.isNullOrBlank() && description.isNullOrBlank())) {
      return null
    }

    return PostPayload(
      title = XmlFeedParser.cleanText(title).orEmpty().decodeHTMLString(),
      link = XmlFeedParser.cleanText(link)!!,
      description = description.orEmpty().decodeHTMLString(),
      rawContent = rawContent,
      imageUrl = UrlUtils.safeUrl(hostLink, image),
      date = postPubDateInMillis ?: Clock.System.now().toEpochMilliseconds(),
      commentsLink = commentsLink?.trim(),
      isDateParsedCorrectly = postPubDateInMillis != null
    )
  }

  private fun hasRssImageUrl(name: String, parser: XmlPullParser) =
    (XmlFeedParser.imageTags.contains(name) ||
      (name == TAG_ENCLOSURE &&
        parser.getAttributeValue(parser.namespace, ATTR_TYPE) == ATTR_VALUE_IMAGE)) &&
      !parser.getAttributeValue(parser.namespace, ATTR_URL).isNullOrBlank()
}
