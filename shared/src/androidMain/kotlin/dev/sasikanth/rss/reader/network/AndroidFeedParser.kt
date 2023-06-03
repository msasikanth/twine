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
import android.util.Xml
import dev.sasikanth.rss.reader.models.FeedPayload
import dev.sasikanth.rss.reader.models.PostPayload
import dev.sasikanth.rss.reader.network.FeedParser.Companion.cleanText
import dev.sasikanth.rss.reader.network.FeedParser.Companion.cleanTextCompact
import dev.sasikanth.rss.reader.network.FeedParser.Companion.feedIcon
import dev.sasikanth.rss.reader.network.FeedParser.Companion.imageTags
import io.github.aakira.napier.Napier
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser

internal class AndroidFeedParser(private val ioDispatcher: CoroutineDispatcher) : FeedParser {

  private val dateFormat =
    DateTimeFormatterBuilder()
      .appendPattern("EEE, dd MMM yyyy HH:mm:ss ")
      .optionalStart()
      .appendPattern("z")
      .optionalEnd()
      .optionalStart()
      .appendPattern("Z")
      .optionalEnd()
      .toFormatter(Locale.US)

  override suspend fun parse(xmlContent: String, feedUrl: String): FeedPayload {
    return withContext(ioDispatcher) {
      val parser =
        Xml.newPullParser().apply { setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false) }

      return@withContext xmlContent.reader().use { reader ->
        parser.setInput(reader)

        var tag = parser.nextTag()
        while (tag != XmlPullParser.START_TAG && parser.name != "rss") {
          skip(parser)
          tag = parser.next()
        }
        parser.nextTag()

        readFeed(parser, feedUrl)
      }
    }
  }

  private fun readFeed(parser: XmlPullParser, feedUrl: String): FeedPayload {
    parser.require(XmlPullParser.START_TAG, null, "channel")

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
        "item" -> posts.add(readPost(parser))
        else -> skip(parser)
      }
    }

    val domain = Uri.parse(link!!)
    val iconUrl = feedIcon(domain.host ?: domain.path!!)

    return FeedPayload(
      name = title!!,
      icon = iconUrl,
      description = description!!,
      homepageLink = link,
      link = feedUrl,
      posts = posts
    )
  }

  private fun readPost(parser: XmlPullParser): PostPayload {
    parser.require(XmlPullParser.START_TAG, null, "item")

    var title: String? = null
    var link: String? = null
    var description: String? = null
    var date: String? = null
    var image: String? = null

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) continue

      val name = parser.name
      when {
        name == "title" -> title = readTagText("title", parser)
        name == "link" -> link = readTagText("link", parser)
        name == "enclosure" && link.isNullOrBlank() -> link = readAttrText("url", parser)
        name == "description" -> description = readTagText("description", parser)
        name == "pubDate" -> date = readTagText("pubDate", parser)
        imageTags.contains(name) && image.isNullOrBlank() -> image = readAttrText("url", parser)
        else -> skip(parser)
      }
    }

    val dateLong: Long =
      date?.let {
        try {
          ZonedDateTime.parse(date, dateFormat).toEpochSecond() * 1000
        } catch (e: Throwable) {
          Napier.e("Parse date error: ${e.message}")
          null
        }
      }
        ?: System.currentTimeMillis()

    return PostPayload(
      title = cleanText(title).orEmpty(),
      link = cleanText(link).orEmpty(),
      description = cleanTextCompact(description).orEmpty(),
      imageUrl = image,
      date = dateLong
    )
  }

  private fun readAttrText(attrName: String, parser: XmlPullParser): String? {
    val url = parser.getAttributeValue(null, attrName)
    skip(parser)
    return url
  }

  private fun readTagText(tagName: String, parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, null, tagName)
    val title = readText(parser)
    parser.require(XmlPullParser.END_TAG, null, tagName)
    return title
  }

  private fun readText(parser: XmlPullParser): String {
    var result = ""
    if (parser.next() == XmlPullParser.TEXT) {
      result = parser.text
      parser.nextTag()
    }
    return result
  }

  private fun skip(parser: XmlPullParser) {
    parser.require(XmlPullParser.START_TAG, null, null)
    var depth = 1
    while (depth != 0) {
      when (parser.next()) {
        XmlPullParser.END_TAG -> depth--
        XmlPullParser.START_TAG -> depth++
      }
    }
  }
}
