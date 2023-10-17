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

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlOptions
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import dev.sasikanth.rss.reader.models.remote.FeedPayload
import dev.sasikanth.rss.reader.models.remote.PostPayload
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_COMMENTS
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_CONTENT_ENCODED
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_DESCRIPTION
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_IMAGE_URL
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_PUB_DATE
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_TITLE
import io.ktor.http.Url
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.timeIntervalSince1970

private val offsetTimezoneDateFormatter =
  NSDateFormatter().apply {
    dateFormat = "E, d MMM yyyy HH:mm:ss Z"
    locale = NSLocale("en_US_POSIX")
  }
private val abbrevTimezoneDateFormatter =
  NSDateFormatter().apply {
    dateFormat = "E, d MMM yyyy HH:mm:ss z"
    locale = NSLocale("en_US_POSIX")
  }

internal fun PostPayload.Companion.mapRssPost(
  rssMap: Map<String, String>,
  hostLink: String
): PostPayload? {
  val title = rssMap[TAG_TITLE]
  val pubDate = rssMap[TAG_PUB_DATE]
  val link = rssMap[TAG_LINK]
  var description = rssMap[TAG_DESCRIPTION]
  val encodedContent = rssMap[TAG_CONTENT_ENCODED]
  var imageUrl: String? = rssMap[TAG_IMAGE_URL]
  val commentsLink: String? = rssMap[TAG_COMMENTS]

  val descriptionToParse =
    if (encodedContent.isNullOrBlank()) {
      description
    } else {
      encodedContent
    }

  KsoupHtmlParser(
      handler =
        HtmlContentParser {
          if (imageUrl.isNullOrBlank()) imageUrl = it.imageUrl
          description = it.content.ifBlank { descriptionToParse?.trim() }
        },
      options = KsoupHtmlOptions(decodeEntities = false)
    )
    .parseComplete(descriptionToParse.orEmpty())

  if (title.isNullOrBlank() && description.isNullOrBlank()) {
    return null
  }

  return PostPayload(
    title = FeedParser.cleanText(title).orEmpty(),
    link = FeedParser.cleanText(link)!!,
    description = FeedParser.cleanTextCompact(description).orEmpty(),
    imageUrl = FeedParser.safeUrl(hostLink, imageUrl),
    date = pubDate.rssDateStringToEpochSeconds(),
    commentsLink = commentsLink?.trim()
  )
}

internal fun FeedPayload.Companion.mapRssFeed(
  feedUrl: String,
  rssMap: Map<String, String>,
  posts: List<PostPayload>
): FeedPayload {
  val link = rssMap[TAG_LINK]!!.trim()
  val domain = Url(link)
  val iconUrl =
    FeedParser.feedIcon(
      if (domain.host != "localhost") domain.host
      else domain.pathSegments.first().split(" ").first().trim()
    )

  return FeedPayload(
    name = FeedParser.cleanText(rssMap[TAG_TITLE] ?: link)!!,
    homepageLink = link,
    link = feedUrl,
    description = FeedParser.cleanText(rssMap[TAG_DESCRIPTION]).orEmpty(),
    icon = iconUrl,
    posts = posts
  )
}

private fun String?.rssDateStringToEpochSeconds(): Long {
  if (this.isNullOrBlank()) return 0L

  val dateString = this.trim()
  val date =
    try {
      offsetTimezoneDateFormatter.dateFromString(dateString)
        ?: abbrevTimezoneDateFormatter.dateFromString(dateString)
    } catch (e: Exception) {
      null
    }

  return date?.timeIntervalSince1970?.times(1000)?.toLong() ?: 0L
}
