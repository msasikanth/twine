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
import io.github.aakira.napier.Napier
import io.sentry.kotlin.multiplatform.Sentry
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.xmlpull.v1.XmlPullParser

internal class AndroidAtomParser(
  private val parser: XmlPullParser,
  private val feedUrl: String,
  private val fetchPosts: Boolean
) : Parser() {

  private val atomDateFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  override fun parse(): FeedPayload {
    return readAtomFeed(parser, feedUrl)
  }

  private fun readAtomFeed(parser: XmlPullParser, feedUrl: String): FeedPayload {
    parser.require(XmlPullParser.START_TAG, namespace, "feed")

    val posts = mutableListOf<PostPayload>()

    var title: String? = null
    var description: String? = null
    var link: String? = null

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) continue
      when (parser.name) {
        "title" -> title = readTagText("title", parser)
        "link" ->
          if (link.isNullOrBlank()) {
            link = readAtomLink(parser)
          } else {
            skip(parser)
          }
        "subtitle" -> description = readTagText("subtitle", parser)
        "entry" -> if (fetchPosts) posts.add(readAtomEntry(parser, link!!))
        else -> skip(parser)
      }
    }

    val domain = Uri.parse(link).host!!
    val iconUrl = FeedParser.feedIcon(domain)

    return FeedPayload(
      name = title!!,
      icon = iconUrl,
      description = FeedParser.cleanText(description).orEmpty(),
      homepageLink = link!!,
      link = feedUrl,
      posts = posts
    )
  }

  private fun readAtomEntry(parser: XmlPullParser, hostLink: String): PostPayload {
    parser.require(XmlPullParser.START_TAG, null, "entry")

    var title: String? = null
    var link: String? = null
    var content: String? = null
    var date: String? = null
    var image: String? = null

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) continue
      when (val tagName = parser.name) {
        "title" -> title = readTagText(tagName, parser)
        "link" -> link = readAtomLink(parser)
        "content" -> {
          val rawContent = readTagText(tagName, parser)
          val contentParser =
            KsoupHtmlParser(
              handler =
                HtmlContentParser {
                  if (image.isNullOrBlank()) image = it.imageUrl
                  content = it.content.ifBlank { rawContent.trim() }
                },
              options = KsoupHtmlOptions(decodeEntities = false)
            )
          contentParser.parseComplete(rawContent)
        }
        "published" -> date = readTagText(tagName, parser)
        else -> skip(parser)
      }
    }

    val dateLong: Long =
      date?.let {
        try {
          ZonedDateTime.parse(date, atomDateFormat).toEpochSecond() * 1000
        } catch (e: Throwable) {
          Sentry.captureException(e)
          Napier.e("Parse date error: ${e.message}")
          null
        }
      }
        ?: System.currentTimeMillis()

    return PostPayload(
      title = FeedParser.cleanText(title).orEmpty(),
      link = FeedParser.cleanText(link).orEmpty(),
      description = FeedParser.cleanTextCompact(content).orEmpty(),
      imageUrl = FeedParser.safeUrl(hostLink, image),
      date = dateLong,
      commentsLink = null
    )
  }

  private fun readAtomLink(parser: XmlPullParser): String? {
    var link: String? = null
    parser.require(XmlPullParser.START_TAG, namespace, "link")
    val relType = parser.getAttributeValue(namespace, "rel")
    if (relType == "alternate" || relType.isNullOrBlank()) {
      link = parser.getAttributeValue(namespace, "href")
    }
    parser.nextTag()
    parser.require(XmlPullParser.END_TAG, namespace, "link")
    return link
  }
}
