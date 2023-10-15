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
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_CONTENT
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_IMAGE_URL
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_PUBLISHED
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_SUBTITLE
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_TITLE
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import io.sentry.kotlin.multiplatform.Sentry
import platform.Foundation.NSDateFormatter
import platform.Foundation.timeIntervalSince1970

private val atomDateFormatter = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ" }

internal fun PostPayload.Companion.mapAtomPost(
  atomMap: Map<String, String>,
  hostLink: String
): PostPayload {
  val title = atomMap[TAG_TITLE]
  val pubDate = atomMap[TAG_PUBLISHED]
  val link = atomMap[TAG_LINK]?.trim()
  val data = atomMap[TAG_CONTENT]
  var imageUrl = atomMap[TAG_IMAGE_URL]
  var content: String? = null

  KsoupHtmlParser(
      handler =
        HtmlContentParser {
          if (imageUrl.isNullOrBlank()) imageUrl = it.imageUrl
          content = it.content.ifBlank { data?.trim() }
        },
      options = KsoupHtmlOptions(decodeEntities = false)
    )
    .parseComplete(data.orEmpty())

  return PostPayload(
    title = FeedParser.cleanText(title)!!,
    link = link!!,
    description = content.orEmpty(),
    imageUrl = FeedParser.safeUrl(hostLink, imageUrl),
    date = pubDate.atomDateStringToEpochSeconds(),
    commentsLink = null
  )
}

internal fun FeedPayload.Companion.mapAtomFeed(
  feedUrl: String,
  atomMap: Map<String, String>,
  posts: List<PostPayload>
): FeedPayload {
  val link = atomMap[TAG_LINK]!!.trim()
  val domain = Url(link)
  val iconUrl =
    FeedParser.feedIcon(
      if (domain.host != "localhost") domain.host
      else domain.pathSegments.first().split(" ").first().trim()
    )

  return FeedPayload(
    name = FeedParser.cleanText(atomMap[TAG_TITLE])!!,
    homepageLink = link,
    link = feedUrl,
    description = FeedParser.cleanText(atomMap[TAG_SUBTITLE]).orEmpty(),
    icon = iconUrl,
    posts = posts
  )
}

private fun String?.atomDateStringToEpochSeconds(): Long {
  if (this.isNullOrBlank()) return 0L

  val date =
    try {
      atomDateFormatter.dateFromString(this.trim())
    } catch (e: Exception) {
      Sentry.captureException(e)
      Napier.e("Parse date error: ${e.message}")
      null
    }

  return date?.timeIntervalSince1970?.times(1000)?.toLong() ?: 0L
}
