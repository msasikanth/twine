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
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import io.sentry.kotlin.multiplatform.Sentry
import platform.Foundation.NSDateFormatter
import platform.Foundation.timeIntervalSince1970

private val offsetTimezoneDateFormatter =
  NSDateFormatter().apply { dateFormat = "E, d MMM yyyy HH:mm:ss Z" }
private val abbrevTimezoneDateFormatter =
  NSDateFormatter().apply { dateFormat = "E, d MMM yyyy HH:mm:ss z" }

internal fun PostPayload.Companion.mapRssPost(
  rssMap: Map<String, String>,
  hostLink: String
): PostPayload {
  val pubDate = rssMap["pubDate"]
  val link = rssMap["link"]
  var description = rssMap["description"]
  val encodedContent = rssMap["content:encoded"]
  var imageUrl: String? = rssMap["imageUrl"]

  val descriptionToParse =
    if (encodedContent.isNullOrBlank()) {
      description
    } else {
      encodedContent
    }
  val contentParser =
    KsoupHtmlParser(
      handler =
        HtmlContentParser {
          if (imageUrl.isNullOrBlank()) imageUrl = it.imageUrl
          description = it.content.ifBlank { descriptionToParse?.trim() }
        },
      options = KsoupHtmlOptions(decodeEntities = false)
    )

  contentParser.parseComplete(descriptionToParse.orEmpty())

  return PostPayload(
    title = FeedParser.cleanText(rssMap["title"])!!,
    link = FeedParser.cleanText(link)!!,
    description = FeedParser.cleanTextCompact(description).orEmpty(),
    imageUrl = FeedParser.safeImageUrl(hostLink, imageUrl),
    date = pubDate.rssDateStringToEpochSeconds()
  )
}

internal fun FeedPayload.Companion.mapRssFeed(
  feedUrl: String,
  rssMap: Map<String, String>,
  posts: List<PostPayload>
): FeedPayload {
  val link = rssMap["link"]!!.trim()
  val domain = Url(link)
  val iconUrl =
    FeedParser.feedIcon(
      if (domain.host != "localhost") domain.host
      else domain.pathSegments.first().split(" ").first().trim()
    )

  return FeedPayload(
    name = FeedParser.cleanText(rssMap["title"])!!,
    homepageLink = link,
    link = feedUrl,
    description = FeedParser.cleanText(rssMap["description"])!!,
    icon = iconUrl,
    posts = posts
  )
}

private fun String?.rssDateStringToEpochSeconds(): Long {
  if (this.isNullOrBlank()) return 0L

  val date =
    try {
      offsetTimezoneDateFormatter.dateFromString(this.trim())
    } catch (e: Exception) {
      try {
        abbrevTimezoneDateFormatter.dateFromString(this.trim())
      } catch (e: Exception) {
        Sentry.captureException(e)
        Napier.e("Parse date error: ${e.message}")
        null
      }
    }

  return date?.timeIntervalSince1970?.times(1000)?.toLong() ?: 0L
}
