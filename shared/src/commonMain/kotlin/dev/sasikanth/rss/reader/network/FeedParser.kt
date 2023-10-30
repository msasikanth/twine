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

import dev.sasikanth.rss.reader.models.remote.FeedPayload
import dev.sasikanth.rss.reader.utils.decodeUrlEncodedString
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.set

interface FeedParser {

  companion object {
    const val RSS_TAG = "rss"
    const val ATOM_TAG = "feed"
    const val HTML_TAG = "html"

    const val RSS_MEDIA_TYPE = "application/rss+xml"
    const val ATOM_MEDIA_TYPE = "application/atom+xml"

    private val htmlTag = Regex("<.+?>")
    private val blankLine = Regex("(?m)^[ \t]*\r?\n")

    internal val imageTags = setOf("media:content", "media:thumbnail")

    internal const val TAG_RSS_CHANNEL = "channel"
    internal const val TAG_ATOM_FEED = "feed"
    internal const val TAG_RSS_ITEM = "item"
    internal const val TAG_ATOM_ENTRY = "entry"

    internal const val TAG_TITLE = "title"
    internal const val TAG_LINK = "link"
    internal const val TAG_DESCRIPTION = "description"
    internal const val TAG_ENCLOSURE = "enclosure"
    internal const val TAG_CONTENT_ENCODED = "content:encoded"
    internal const val TAG_CONTENT = "content"
    internal const val TAG_SUBTITLE = "subtitle"
    internal const val TAG_PUB_DATE = "pubDate"
    internal const val TAG_PUBLISHED = "published"
    internal const val TAG_FEATURED_IMAGE = "featuredImage"
    internal const val TAG_COMMENTS = "comments"
    internal const val TAG_IMAGE_URL = "imageUrl"
    internal const val TAG_FEED_IMAGE = "image"

    internal const val ATTR_URL = "url"
    internal const val ATTR_TYPE = "type"
    internal const val ATTR_REL = "rel"
    internal const val ATTR_HREF = "href"

    internal const val ATTR_VALUE_ALTERNATE = "alternate"
    internal const val ATTR_VALUE_IMAGE = "image/jpeg"

    fun cleanText(text: String?, decodeUrlEncoding: Boolean = false): String? {
      var sanitizedString = text?.replace(htmlTag, "")?.replace(blankLine, "")?.trim()

      if (decodeUrlEncoding) {
        sanitizedString = sanitizedString?.decodeUrlEncodedString()
      }

      return sanitizedString
    }

    fun cleanTextCompact(text: String?, decodeUrlEncoding: Boolean = false) =
      cleanText(text, decodeUrlEncoding)?.take(300)

    fun feedIcon(host: String): String {
      return "https://icon.horse/icon/$host"
    }

    fun safeUrl(host: String, url: String?): String? {
      return if (!url.isNullOrBlank()) {
        if (isAbsoluteUrl(url)) {
          URLBuilder(url).apply { protocol = URLProtocol.HTTPS }.buildString()
        } else {
          URLBuilder(host)
            .apply {
              set(path = url)
              protocol = URLProtocol.HTTPS
            }
            .buildString()
        }
      } else {
        null
      }
    }

    private fun isAbsoluteUrl(url: String): Boolean {
      val pattern = """^(?:\w+:)?//""".toRegex()
      return pattern.containsMatchIn(url)
    }
  }

  suspend fun parse(xmlContent: String, feedUrl: String): FeedPayload
}

internal class HtmlContentException : Exception()
