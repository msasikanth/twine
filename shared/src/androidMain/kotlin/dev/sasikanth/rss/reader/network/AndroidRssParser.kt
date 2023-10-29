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

import android.net.Uri
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlOptions
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import dev.sasikanth.rss.reader.models.remote.FeedPayload
import dev.sasikanth.rss.reader.models.remote.PostPayload
import dev.sasikanth.rss.reader.network.FeedParser.Companion.ATTR_TYPE
import dev.sasikanth.rss.reader.network.FeedParser.Companion.ATTR_URL
import dev.sasikanth.rss.reader.network.FeedParser.Companion.ATTR_VALUE_IMAGE
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_COMMENTS
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_CONTENT_ENCODED
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_DESCRIPTION
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_ENCLOSURE
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_FEATURED_IMAGE
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_PUB_DATE
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_RSS_CHANNEL
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_RSS_ITEM
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_TITLE
import io.github.aakira.napier.Napier
import io.sentry.kotlin.multiplatform.Sentry
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale
import org.xmlpull.v1.XmlPullParser

internal class AndroidRssParser(
  private val parser: XmlPullParser,
  private val feedUrl: String,
  private val fetchPosts: Boolean
) : Parser() {

  private val rssDateFormat =
    DateTimeFormatterBuilder()
      .appendPattern("E, d MMM yyyy HH:mm:ss ")
      .optionalStart()
      .appendPattern("z")
      .optionalEnd()
      .optionalStart()
      .appendPattern("Z")
      .optionalEnd()
      .toFormatter(Locale.US)

  override fun parse(): FeedPayload {
    parser.nextTag()
    parser.require(XmlPullParser.START_TAG, namespace, TAG_RSS_CHANNEL)

    val posts = mutableListOf<PostPayload?>()

    var title: String? = null
    var link: String? = null
    var description: String? = null

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) continue
      when (val name = parser.name) {
        TAG_TITLE -> {
          title = readTagText(name, parser)
        }
        TAG_LINK -> {
          link = readTagText(name, parser)
        }
        TAG_DESCRIPTION -> {
          description = readTagText(name, parser)
        }
        TAG_RSS_ITEM -> {
          if (fetchPosts) posts.add(readRssItem(parser, link!!))
        }
        else -> skip(parser)
      }
    }

    val domain = Uri.parse(link!!)
    val iconUrl = FeedParser.feedIcon(domain.host ?: domain.path!!)

    return FeedPayload(
      name = FeedParser.cleanText(title ?: link, decodeUrlEncoding = true)!!,
      description = FeedParser.cleanText(description, decodeUrlEncoding = true).orEmpty(),
      icon = iconUrl,
      homepageLink = link,
      link = feedUrl,
      posts = posts.filterNotNull()
    )
  }

  private fun readRssItem(parser: XmlPullParser, hostLink: String): PostPayload? {
    parser.require(XmlPullParser.START_TAG, namespace, TAG_RSS_ITEM)

    var title: String? = null
    var link: String? = null
    var description: String? = null
    var date: String? = null
    var image: String? = null
    var commentsLink: String? = null

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) continue
      val name = parser.name

      when {
        name == TAG_TITLE -> {
          title = readTagText(name, parser)
        }
        name == TAG_LINK -> {
          link = readTagText(name, parser)
        }
        name == TAG_ENCLOSURE && link.isNullOrBlank() -> {
          link = readAttrText(ATTR_URL, parser)
        }
        name == TAG_DESCRIPTION || name == TAG_CONTENT_ENCODED -> {
          description = readTagText(name, parser)
        }
        name == TAG_PUB_DATE -> {
          date = readTagText(name, parser)
        }
        image.isNullOrBlank() && hasRssImageUrl(name, parser) -> {
          image = readAttrText(ATTR_URL, parser)
        }
        image.isNullOrBlank() && name == TAG_FEATURED_IMAGE -> {
          image = readTagText(name, parser)
        }
        commentsLink.isNullOrBlank() && name == TAG_COMMENTS -> {
          commentsLink = readTagText(name, parser)
        }
        else -> skip(parser)
      }
    }

    val dateLong: Long =
      date?.let {
        try {
          ZonedDateTime.parse(date, this.rssDateFormat).toEpochSecond() * 1000
        } catch (e: Throwable) {
          Sentry.captureException(e)
          Napier.e("Parse date error: ${e.message}")
          null
        }
      }
        ?: System.currentTimeMillis()

    KsoupHtmlParser(
        handler =
          HtmlContentParser {
            if (image.isNullOrBlank()) image = it.imageUrl
            description = it.content.ifBlank { description?.trim() }
          },
        options = KsoupHtmlOptions(decodeEntities = false)
      )
      .parseComplete(description.orEmpty())

    if (title.isNullOrBlank() && description.isNullOrBlank()) {
      return null
    }

    return PostPayload(
      title = FeedParser.cleanText(title, decodeUrlEncoding = true).orEmpty(),
      description = FeedParser.cleanTextCompact(description, decodeUrlEncoding = true).orEmpty(),
      link = FeedParser.cleanText(link)!!,
      imageUrl = FeedParser.safeUrl(hostLink, image),
      date = dateLong,
      commentsLink = commentsLink?.trim()
    )
  }

  private fun hasRssImageUrl(name: String, parser: XmlPullParser) =
    (FeedParser.imageTags.contains(name) ||
      (name == TAG_ENCLOSURE &&
        parser.getAttributeValue(namespace, ATTR_TYPE) == ATTR_VALUE_IMAGE)) &&
      !parser.getAttributeValue(namespace, ATTR_URL).isNullOrBlank()
}
