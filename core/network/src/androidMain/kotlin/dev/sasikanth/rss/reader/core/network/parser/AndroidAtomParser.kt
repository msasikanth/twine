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

package dev.sasikanth.rss.reader.core.network.parser

import android.net.Uri
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlOptions
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATTR_HREF
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATTR_REL
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATTR_VALUE_ALTERNATE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_ATOM_ENTRY
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_ATOM_FEED
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_CONTENT
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_PUBLISHED
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_SUBTITLE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_TITLE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_UPDATED
import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.datetime.Clock
import org.xmlpull.v1.XmlPullParser

internal class AndroidAtomParser(private val parser: XmlPullParser, private val feedUrl: String) :
  Parser() {

  override fun parse(): FeedPayload {
    parser.require(XmlPullParser.START_TAG, namespace, TAG_ATOM_FEED)

    val posts = mutableListOf<PostPayload?>()

    var title: String? = null
    var description: String? = null
    var link: String? = null

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) continue
      when (val name = parser.name) {
        TAG_TITLE -> {
          title = readTagText(name, parser)
        }
        TAG_LINK -> {
          if (link.isNullOrBlank()) {
            link = readAtomLink(name, parser)
          } else {
            skip(parser)
          }
        }
        TAG_SUBTITLE -> {
          description = readTagText(name, parser)
        }
        TAG_ATOM_ENTRY -> {
          posts.add(readAtomEntry(parser, link!!))
        }
        else -> skip(parser)
      }
    }

    val domain = Uri.parse(link).host!!
    val iconUrl = FeedParser.feedIcon(domain)

    return FeedPayload(
      name = FeedParser.cleanText(title ?: link, decodeUrlEncoding = true)!!,
      description = FeedParser.cleanText(description, decodeUrlEncoding = true).orEmpty(),
      icon = iconUrl,
      homepageLink = link!!,
      link = feedUrl,
      posts = posts.filterNotNull()
    )
  }

  private fun readAtomEntry(parser: XmlPullParser, hostLink: String): PostPayload? {
    parser.require(XmlPullParser.START_TAG, null, "entry")

    var title: String? = null
    var link: String? = null
    var content: String? = null
    var date: String? = null
    var image: String? = null

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) continue

      when (val tagName = parser.name) {
        TAG_TITLE -> {
          title = readTagText(tagName, parser)
        }
        TAG_LINK -> {
          if (link.isNullOrBlank()) {
            link = readAtomLink(tagName, parser)
          } else {
            skip(parser)
          }
        }
        TAG_CONTENT -> {
          val rawContent = readTagText(tagName, parser)
          KsoupHtmlParser(
              handler =
                HtmlContentParser {
                  if (image.isNullOrBlank()) image = it.imageUrl
                  content = it.content.ifBlank { rawContent.trim() }
                },
              options = KsoupHtmlOptions(decodeEntities = false)
            )
            .parseComplete(rawContent)
        }
        TAG_PUBLISHED,
        TAG_UPDATED -> {
          if (date.isNullOrBlank()) {
            date = readTagText(tagName, parser)
          }
        }
        else -> skip(parser)
      }
    }

    val postPubDateInMillis =
      date?.let { dateString -> dateString.dateStringToEpochMillis() }
        ?: run {
          Sentry.captureMessage("Failed to parse date: $date")
          null
        }

    if (title.isNullOrBlank() && content.isNullOrBlank()) {
      return null
    }

    return PostPayload(
      title = FeedParser.cleanText(title, decodeUrlEncoding = true).orEmpty(),
      description = FeedParser.cleanTextCompact(content, decodeUrlEncoding = true).orEmpty(),
      link = FeedParser.cleanText(link)!!,
      imageUrl = FeedParser.safeUrl(hostLink, image),
      date = postPubDateInMillis ?: Clock.System.now().toEpochMilliseconds(),
      commentsLink = null
    )
  }

  private fun readAtomLink(tagName: String, parser: XmlPullParser): String? {
    var link: String? = null
    parser.require(XmlPullParser.START_TAG, namespace, tagName)
    val relType = parser.getAttributeValue(namespace, ATTR_REL)
    if (relType == ATTR_VALUE_ALTERNATE || relType.isNullOrBlank()) {
      link = parser.getAttributeValue(namespace, ATTR_HREF)
    }
    parser.nextTag()
    parser.require(XmlPullParser.END_TAG, namespace, tagName)
    return link
  }
}
