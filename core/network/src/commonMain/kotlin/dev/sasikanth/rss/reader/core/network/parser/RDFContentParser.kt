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
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATTR_RDF_RESOURCE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_CONTENT_ENCODED
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_DC_DATE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_DESCRIPTION
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_FEED_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_PUB_DATE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_RSS_CHANNEL
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_RSS_ITEM
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_TITLE
import dev.sasikanth.rss.reader.util.dateStringToEpochMillis
import dev.sasikanth.rss.reader.util.decodeHTMLString
import kotlinx.datetime.Clock
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser

internal object RDFContentParser : ContentParser() {

  override fun parse(feedUrl: String, parser: XmlPullParser): FeedPayload {
    parser.nextTag()
    parser.require(EventType.START_TAG, parser.namespace, TAG_RSS_CHANNEL)

    val posts = mutableListOf<PostPayload?>()

    var title: String? = null
    var link: String? = null
    var description: String? = null
    var iconUrl: String? = null

    // Parse channel
    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue

      when (val name = parser.name) {
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
        TAG_FEED_IMAGE -> {
          iconUrl = readRdfLink(name, parser)
        }
        else -> parser.skip()
      }
    }

    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue

      when (parser.name) {
        TAG_RSS_ITEM -> {
          posts.add(readRssItem(parser, link))
        }
        else -> parser.skip()
      }
    }

    if (link.isNullOrBlank()) {
      link = feedUrl
    }

    return FeedPayload(
      name = FeedParser.cleanText(title ?: link)!!.decodeHTMLString(),
      description = FeedParser.cleanText(description).orEmpty().decodeHTMLString(),
      icon = iconUrl ?: link,
      homepageLink = link,
      link = feedUrl,
      posts = posts.filterNotNull()
    )
  }

  private fun readRssItem(parser: XmlPullParser, hostLink: String?): PostPayload? {
    parser.require(EventType.START_TAG, parser.namespace, TAG_RSS_ITEM)

    var title: String? = null
    var link: String? = null
    var description: String? = null
    var rawContent: String? = null
    var date: String? = null
    var image: String? = null
    val commentsLink: String? = null

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
          rawContent = parser.nextText().trimIndent()

          val htmlContent = HtmlContentParser.parse(htmlContent = rawContent)
          image = htmlContent?.leadImage ?: image
          description = htmlContent?.content?.ifBlank { null } ?: rawContent.trim()
        }
        name == TAG_PUB_DATE || name == TAG_DC_DATE -> {
          date = parser.nextText()
        }
        else -> parser.skip()
      }
    }

    val postPubDateInMillis = date?.let { dateString -> dateString.dateStringToEpochMillis() }

    if (title.isNullOrBlank() && description.isNullOrBlank()) {
      return null
    }

    return PostPayload(
      link = FeedParser.cleanText(link)!!,
      title = FeedParser.cleanText(title).orEmpty().decodeHTMLString(),
      description = description.orEmpty().decodeHTMLString(),
      rawContent = rawContent,
      imageUrl = FeedParser.safeUrl(hostLink, image),
      date = postPubDateInMillis ?: Clock.System.now().toEpochMilliseconds(),
      commentsLink = commentsLink?.trim()
    )
  }

  private fun readRdfLink(tagName: String, parser: XmlPullParser): String? {
    parser.require(EventType.START_TAG, parser.namespace, tagName)
    val link = parser.getAttributeValue(parser.namespace, ATTR_RDF_RESOURCE)
    parser.nextTag()
    parser.require(EventType.END_TAG, parser.namespace, tagName)
    return link
  }
}
