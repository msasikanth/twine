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
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale
import org.xmlpull.v1.XmlPullParser

internal class AndroidRssParser(private val parser: XmlPullParser, private val feedUrl: String) :
  Parser() {

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
    parser.require(XmlPullParser.START_TAG, namespace, "channel")

    val posts = mutableListOf<PostPayload>()

    var title: String? = null
    var link: String? = null
    var description: String? = null

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) continue
      when (parser.name) {
        "title" -> title = readTagText("title", parser)
        "link" -> link = readTagText("link", parser)
        "description" -> description = readTagText("description", parser)
        "item" -> posts.add(readRssItem(parser, link!!))
        else -> skip(parser)
      }
    }

    val domain = Uri.parse(link!!)
    val iconUrl = FeedParser.feedIcon(domain.host ?: domain.path!!)

    return FeedPayload(
      name = title!!,
      icon = iconUrl,
      description = description!!,
      homepageLink = link,
      link = feedUrl,
      posts = posts
    )
  }

  private fun readRssItem(parser: XmlPullParser, hostLink: String): PostPayload {
    parser.require(XmlPullParser.START_TAG, namespace, "item")

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
        name == "title" -> title = readTagText("title", parser)
        name == "link" -> link = readTagText("link", parser)
        name == "enclosure" && link.isNullOrBlank() -> link = readAttrText("url", parser)
        name == "description" || name == "content:encoded" ->
          description = readTagText(name, parser)
        name == "pubDate" -> date = readTagText("pubDate", parser)
        image.isNullOrBlank() && hasRssImageUrl(name, parser) -> image = readAttrText("url", parser)
        image.isNullOrBlank() && name == "featuredImage" -> image = readTagText(name, parser)
        commentsLink.isNullOrBlank() && name == "comments" ->
          commentsLink = readTagText(name, parser)
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

    val contentParser =
      KsoupHtmlParser(
        handler =
          HtmlContentParser {
            if (image.isNullOrBlank()) image = it.imageUrl
            description = it.content.ifBlank { description?.trim() }
          },
        options = KsoupHtmlOptions(decodeEntities = false)
      )

    contentParser.parseComplete(description.orEmpty())

    return PostPayload(
      title = FeedParser.cleanText(title).orEmpty(),
      link = FeedParser.cleanText(link).orEmpty(),
      description = FeedParser.cleanTextCompact(description).orEmpty(),
      imageUrl = FeedParser.safeImageUrl(hostLink, image),
      date = dateLong,
      commentsLink = commentsLink?.trim()
    )
  }

  private fun hasRssImageUrl(name: String, parser: XmlPullParser) =
    (FeedParser.imageTags.contains(name) ||
      (name == "enclosure" && parser.getAttributeValue(namespace, "type") == "image/jpeg")) &&
      !parser.getAttributeValue(namespace, "url").isNullOrBlank()
}
