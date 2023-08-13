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
import io.github.aakira.napier.Napier
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

internal class AndroidAtomParser(private val parser: XmlPullParser, private val feedUrl: String) :
  Parser() {

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
          }
        "subtitle" -> description = readTagText("subtitle", parser)
        "entry" -> posts.add(readAtomEntry(parser))
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

  private fun readAtomEntry(parser: XmlPullParser): PostPayload {
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
          val atomContent = readAtomContent(tagName, parser)
          if (content.isNullOrBlank()) {
            content = atomContent.content
          }
          if (image.isNullOrBlank()) {
            image = atomContent.imageUrl
          }
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
          Napier.e("Parse date error: ${e.message}")
          null
        }
      }
        ?: System.currentTimeMillis()

    return PostPayload(
      title = FeedParser.cleanText(title).orEmpty(),
      link = FeedParser.cleanText(link).orEmpty(),
      description = FeedParser.cleanTextCompact(content).orEmpty(),
      imageUrl = image,
      date = dateLong
    )
  }

  private fun readAtomContent(tagName: String, parser: XmlPullParser): AtomContent {
    parser.require(XmlPullParser.START_TAG, namespace, tagName)

    val rawContent = readTagText(tagName, parser)
    val contentBuilder = StringBuilder()
    var imageUrl: String? = null

    try {
      val contentParser =
        Xml.newPullParser().apply { setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false) }
      contentParser.setInput(rawContent.reader())

      var parsedContent = false
      var currentEventType = contentParser.eventType
      var currentTag: String? = null

      while (!parsedContent && currentEventType != XmlPullParser.END_DOCUMENT) {
        when (currentEventType) {
          XmlPullParser.START_TAG -> {
            currentTag = contentParser.name
            if (currentTag == "img") {
              imageUrl = contentParser.getAttributeValue(namespace, "src")
            }
          }
          XmlPullParser.TEXT -> {
            val text = contentParser.text.trim()
            when {
              text.isNotBlank() &&
                (currentTag == "p" ||
                  currentTag == "a" ||
                  currentTag == "span" ||
                  currentTag == "em") -> {
                contentBuilder.append("$text ")
              }
            }
          }
          XmlPullParser.END_TAG -> {
            if (contentParser.name == tagName) {
              parsedContent = true
            }
          }
        }
        currentEventType = contentParser.next()
      }
    } catch (e: XmlPullParserException) {
      contentBuilder.append(rawContent)
    }

    return AtomContent(imageUrl = imageUrl, content = contentBuilder.toString())
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

private data class AtomContent(val imageUrl: String?, val content: String)
