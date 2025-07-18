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

import com.fleeksoft.io.kotlinx.asInputStream
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parseMetaData
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.common.HtmlContentParser
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_HREF
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_REL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.ATTR_VALUE_ALTERNATE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ATOM_ENTRY
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ATOM_FEED
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_CONTENT
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_ICON
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_CONTENT
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_GROUP
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_MEDIA_THUMBNAIL
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_PUBLISHED
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_SUBTITLE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_SUMMARY
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_TITLE
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_UPDATED
import dev.sasikanth.rss.reader.core.network.parser.xml.XmlFeedParser.Companion.TAG_URL
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.util.dateStringToEpochMillis
import dev.sasikanth.rss.reader.util.decodeHTMLString
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.asSource
import me.tatarka.inject.annotations.Inject
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser
import kotlin.time.Clock

@Inject
class AtomContentParser(
  httpClient: HttpClient,
  private val htmlContentParser: HtmlContentParser,
) : XmlContentParser() {

  private val youTubeIconHttpClient = httpClient.config { followRedirects = true }

  override suspend fun parse(feedUrl: String, parser: XmlPullParser): FeedPayload {
    parser.require(EventType.START_TAG, parser.namespace, TAG_ATOM_FEED)

    val posts = mutableListOf<PostPayload?>()

    var title: String? = null
    var description: String? = null
    var link: String? = null
    var iconUrl: String? = null

    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue
      when (val name = parser.name) {
        TAG_TITLE -> {
          title = parser.nextText()
        }
        TAG_LINK -> {
          if (link.isNullOrBlank()) {
            link = readAtomLink(name, parser)
          } else {
            parser.skip()
          }
        }
        TAG_SUBTITLE -> {
          description = parser.nextText()
        }
        TAG_ATOM_ENTRY -> {
          val host = UrlUtils.extractHost(link ?: feedUrl)
          posts.add(readAtomEntry(parser, host))
        }
        TAG_ICON -> {
          iconUrl = parser.nextText()
        }
        else -> parser.skip()
      }
    }

    iconUrl =
      if (UrlUtils.isYouTubeLink(feedUrl)) {
        youtubeChannelImage(link!!)
      } else {
        feedDefaultFallbackIcon(link, feedUrl, iconUrl)
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

  private suspend fun youtubeChannelImage(link: String): String {
    val response = youTubeIconHttpClient.get(urlString = link)
    return Ksoup.parseMetaData(response.bodyAsChannel().asSource().asInputStream(), baseUri = link)
      .ogImage!!
  }

  private fun feedDefaultFallbackIcon(link: String?, feedUrl: String, iconUrl: String?): String {
    var iconUrl1 = iconUrl
    val host = UrlUtils.extractHost(link ?: feedUrl)
    if (iconUrl1.isNullOrBlank()) {
      iconUrl1 = UrlUtils.fallbackFeedIcon(host)
    }
    return iconUrl1
  }

  private fun readAtomEntry(parser: XmlPullParser, hostLink: String?): PostPayload? {
    parser.require(EventType.START_TAG, null, "entry")

    var title: String? = null
    var link: String? = null
    var content: String? = null
    var rawContent: String? = null
    var date: String? = null
    var image: String? = null

    while (parser.next() != EventType.END_TAG) {
      if (parser.eventType != EventType.START_TAG) continue

      when (val tagName = parser.name) {
        TAG_TITLE -> {
          title = parser.nextText()
        }
        TAG_LINK -> {
          if (link.isNullOrBlank()) {
            link = readAtomLink(tagName, parser)
          } else {
            parser.skip()
          }
        }
        TAG_CONTENT,
        TAG_SUMMARY -> {
          rawContent = parser.nextText().trimIndent()

          val htmlContent = htmlContentParser.parse(htmlContent = rawContent)
          image = htmlContent?.leadImage ?: image
          content = htmlContent?.content?.ifBlank { null } ?: rawContent.trim()
        }
        TAG_PUBLISHED,
        TAG_UPDATED -> {
          if (date.isNullOrBlank()) {
            date = parser.nextText()
          } else {
            parser.skip()
          }
        }
        TAG_MEDIA_GROUP -> {
          while (parser.next() != EventType.END_TAG) {
            if (parser.eventType != EventType.START_TAG) continue

            when (parser.name) {
              TAG_MEDIA_THUMBNAIL -> {
                image = parser.getAttributeValue(parser.namespace, TAG_URL)
                parser.nextTag()
              }
              TAG_MEDIA_CONTENT -> {
                content = content.orEmpty().ifBlank { parser.nextText() }
              }
              else -> parser.skip()
            }
          }
        }
        else -> parser.skip()
      }
    }

    val postPubDateInMillis = date?.dateStringToEpochMillis()

    if (link.isNullOrBlank() || (title.isNullOrBlank() && content.isNullOrBlank())) {
      return null
    }

    return PostPayload(
      title = XmlFeedParser.cleanText(title).orEmpty().decodeHTMLString(),
      link = XmlFeedParser.cleanText(link)!!,
      description = content.orEmpty().decodeHTMLString(),
      rawContent = rawContent,
      imageUrl = UrlUtils.safeUrl(hostLink, image),
      date = postPubDateInMillis ?: Clock.System.now().toEpochMilliseconds(),
      commentsLink = null,
      isDateParsedCorrectly = postPubDateInMillis != null
    )
  }

  private fun readAtomLink(tagName: String, parser: XmlPullParser): String? {
    var link: String? = null
    parser.require(EventType.START_TAG, parser.namespace, tagName)
    val relType = parser.getAttributeValue(parser.namespace, ATTR_REL)
    if (relType == ATTR_VALUE_ALTERNATE || relType.isNullOrBlank()) {
      link = parser.getAttributeValue(parser.namespace, ATTR_HREF)
    }
    parser.nextTag()
    parser.require(EventType.END_TAG, parser.namespace, tagName)
    return link
  }
}
