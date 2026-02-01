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

package dev.sasikanth.rss.reader.core.network.parser.xml

import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_TYPE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_URL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_VALUE_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_COMMENTS
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_CONTENT_ENCODED
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_DESCRIPTION
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ENCLOSURE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_FEATURED_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_FEED_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ITUNES_IMAGE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_GROUP
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_PUB_DATE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_RSS_CHANNEL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_RSS_ITEM
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_TITLE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_URL
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import me.tatarka.inject.annotations.Inject
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser

@Inject
class RSSContentParser(override val articleHtmlParser: ArticleHtmlParser) : XmlContentParser() {

  override suspend fun parse(feedUrl: String, parser: XmlPullParser): FeedPayload {
    parser.nextTag()
    parser.require(EventType.START_TAG, parser.namespace, TAG_RSS_CHANNEL)

    var title: String? = null
    var link: String? = null
    var description: String? = null
    var iconUrl: String? = null
    var firstPost: PostPayload? = null

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
        TAG_FEED_IMAGE,
        TAG_ITUNES_IMAGE -> {
          iconUrl = readFeedIcon(parser)
        }
        TAG_RSS_ITEM -> {
          val host = UrlUtils.extractHost(link ?: feedUrl)
          firstPost = readRssItem(parser, host)
          if (firstPost != null) {
            break
          }
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
          firstPost = firstPost,
          itemTag = TAG_RSS_ITEM,
          readItem = { readRssItem(it, UrlUtils.extractHost(link ?: feedUrl)) }
        )
    )
  }

  private fun readFeedIcon(parser: XmlPullParser): String? {
    parser.require(EventType.START_TAG, parser.namespace, parser.name)

    var imageUrl: String? = null

    if (parser.name == TAG_ITUNES_IMAGE) {
      imageUrl = parser.getAttributeValue(parser.namespace, XmlFeedParser.ATTR_HREF)
      parser.nextTag()
    } else {
      while (parser.next() != EventType.END_TAG) {
        if (parser.eventType != EventType.START_TAG) continue
        if (parser.name == TAG_URL) {
          imageUrl = parser.nextText()
        } else {
          parser.skipSubTree()
        }
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
    var audioUrl: String? = null
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
        name == TAG_ENCLOSURE -> {
          val enclosureType = parser.getAttributeValue(parser.namespace, ATTR_TYPE)
          val enclosureUrl = parser.getAttributeValue(parser.namespace, ATTR_URL)

          if (enclosureType?.startsWith("audio/") == true) {
            audioUrl = enclosureUrl
          }

          if (link.isNullOrBlank() && enclosureType != ATTR_VALUE_IMAGE) {
            link = enclosureUrl
          }
          parser.nextTag()
        }
        name == TAG_DESCRIPTION || name == TAG_CONTENT_ENCODED -> {
          val postContent = parsePostContent(parser)

          rawContent = postContent.rawContent
          image = postContent.heroImage ?: image
          description = postContent.textContent
        }
        name == TAG_PUB_DATE -> {
          date = parser.nextText()
        }
        image.isNullOrBlank() && name == TAG_ITUNES_IMAGE -> {
          image = parser.getAttributeValue(parser.namespace, XmlFeedParser.ATTR_HREF)
          parser.nextTag()
        }
        image.isNullOrBlank() && hasRssImageUrl(name, parser) -> {
          image = parser.getAttributeValue(parser.namespace, ATTR_URL)
          parser.nextTag()
        }
        image.isNullOrBlank() && name == TAG_FEATURED_IMAGE -> {
          image = parser.nextText()
        }
        commentsLink.isNullOrBlank() && name == TAG_COMMENTS -> {
          commentsLink = parser.nextText()
        }
        name == TAG_MEDIA_GROUP -> {
          val mediaGroupResult = readMediaGroup(parser)
          image = mediaGroupResult.image ?: image
          description = description.orEmpty().ifBlank { mediaGroupResult.description }
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
      audioUrl = audioUrl,
      date = date,
      commentsLink = commentsLink,
      hostLink = hostLink
    )
  }

  private fun hasRssImageUrl(name: String, parser: XmlPullParser) =
    (XmlFeedParser.imageTags.contains(name) ||
      (name == TAG_ENCLOSURE &&
        parser.getAttributeValue(parser.namespace, ATTR_TYPE) == ATTR_VALUE_IMAGE)) &&
      !parser.getAttributeValue(parser.namespace, ATTR_URL).isNullOrBlank()
}
