/*
 * Copyright 2025 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.core.network.parser.json

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.HtmlContentParser
import dev.sasikanth.rss.reader.core.network.parser.XmlFeedParser
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.dateStringToEpochMillis
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
class JsonFeedParser(private val dispatchersProvider: DispatchersProvider) {

  private val json = Json { ignoreUnknownKeys = true }

  suspend fun parse(content: String, feedUrl: String): FeedPayload {
    return try {
      withContext(dispatchersProvider.io) {
        val jsonFeedPayload = json.decodeFromString<JsonFeedPayload>(content)

        val host =
          UrlUtils.extractHost(
            urlString = jsonFeedPayload.homePageUrl ?: jsonFeedPayload.url ?: feedUrl
          )
        val feedPayload =
          FeedPayload(
            name = jsonFeedPayload.title,
            icon = jsonFeedPayload.icon
                ?: jsonFeedPayload.favIcon ?: XmlFeedParser.fallbackFeedIcon(host),
            description = jsonFeedPayload.description.orEmpty(),
            homepageLink = jsonFeedPayload.homePageUrl ?: feedUrl,
            link = jsonFeedPayload.url ?: feedUrl,
            posts =
              jsonFeedPayload.items.map { jsonFeedPost ->
                val postPublishedAt = jsonFeedPost.publishedAt?.dateStringToEpochMillis()

                val htmlContent = HtmlContentParser.parse(jsonFeedPost.contentHtml.orEmpty())
                val image = htmlContent?.leadImage

                val description =
                  jsonFeedPost.summary
                    .let {
                      if (it.isNullOrBlank()) {
                        jsonFeedPost.contentText ?: htmlContent?.content
                      } else {
                        it
                      }
                    }
                    .orEmpty()
                val rawContent = jsonFeedPost.contentText ?: jsonFeedPost.contentHtml

                PostPayload(
                  title = jsonFeedPost.title.orEmpty(),
                  link = jsonFeedPost.url.orEmpty(),
                  description = description,
                  rawContent = rawContent,
                  imageUrl = jsonFeedPost.imageUrl ?: image,
                  date = postPublishedAt ?: Clock.System.now().toEpochMilliseconds(),
                  commentsLink = null,
                  isDateParsedCorrectly = postPublishedAt != null
                )
              }
          )

        return@withContext feedPayload
      }
    } catch (e: Exception) {
      Logger.e(throwable = e) { "Failed to parse the feed" }
      throw IllegalStateException(e.stackTraceToString())
    }
  }
}

@Serializable
private data class JsonFeedPayload(
  val title: String,
  val description: String? = null,
  @SerialName("home_page_url") val homePageUrl: String? = null,
  @SerialName("feed_url") val url: String? = null,
  val icon: String? = null,
  val favIcon: String? = null,
  val items: List<Post> = emptyList(),
) {

  @Serializable
  data class Post(
    val id: String,
    val title: String? = null,
    @SerialName("content_text") val contentText: String? = null,
    @SerialName("content_html") val contentHtml: String? = null,
    val summary: String? = null,
    @SerialName("image") val imageUrl: String? = null,
    @SerialName("date_published") val publishedAt: String? = null,
    val url: String? = null,
  )
}
