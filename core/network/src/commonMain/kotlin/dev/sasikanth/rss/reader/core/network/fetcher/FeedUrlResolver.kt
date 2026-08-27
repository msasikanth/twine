/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.sasikanth.rss.reader.core.network.fetcher

import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Inject

/**
 * Handles the sources where feed auto discovery cannot work: Reddit serves a script only shell to
 * non browser clients, Mastodon handles are not URLs, and YouTube video pages do not link the
 * channel feed.
 */
@Inject
class FeedUrlResolver(private val httpClient: HttpClient) {

  companion object {
    private const val YOUTUBE_OEMBED_URL = "https://www.youtube.com/oembed"

    private val redditRegex =
      Regex(
        "^(?:https?://)?(?:(?:www|old|new|np|m)\\.)?reddit\\.com" +
          "/(r|user|u)/([A-Za-z0-9_\\-]+)/?$",
        RegexOption.IGNORE_CASE,
      )

    private val mastodonHandleRegex =
      Regex("^@([A-Za-z0-9_.\\-]+)@([A-Za-z0-9.\\-]+\\.[A-Za-z]{2,})$")

    private val youtubeChannelIdRegex =
      Regex(
        "^(?:https?://)?(?:(?:www|m|music)\\.)?youtube\\.com/channel/(UC[\\w\\-]{22})(?:[/?#].*)?$",
        RegexOption.IGNORE_CASE,
      )
  }

  suspend fun resolve(input: String): String {
    val url = input.trim()
    if (url.isBlank()) return input

    return resolveReddit(url)
      ?: resolveMastodonHandle(url)
      ?: resolveYouTubeChannel(url)
      ?: resolveYouTubeVideo(url)
      ?: url
  }

  private fun resolveReddit(url: String): String? {
    val (type, name) = redditRegex.find(url)?.destructured ?: return null
    val normalisedType = if (type.equals("u", ignoreCase = true)) "user" else type.lowercase()

    return "https://www.reddit.com/$normalisedType/$name/.rss"
  }

  private fun resolveMastodonHandle(url: String): String? {
    val (user, instance) = mastodonHandleRegex.find(url)?.destructured ?: return null

    return "https://$instance/@$user.rss"
  }

  private fun resolveYouTubeChannel(url: String): String? {
    val channelId = youtubeChannelIdRegex.find(url)?.groupValues?.getOrNull(1) ?: return null

    return youTubeChannelFeed(channelId)
  }

  private suspend fun resolveYouTubeVideo(url: String): String? {
    if (UrlUtils.youTubeVideoId(url) == null) return null

    val authorUrl =
      try {
        val response =
          httpClient.get(YOUTUBE_OEMBED_URL) {
            parameter("url", url)
            parameter("format", "json")
          }

        if (response.status != HttpStatusCode.OK) return null
        response.body<YouTubeOEmbed>().authorUrl
      } catch (e: Exception) {
        null
      } ?: return null

    val channelId = youtubeChannelIdRegex.find(authorUrl)?.groupValues?.getOrNull(1)

    return if (channelId != null) youTubeChannelFeed(channelId) else authorUrl
  }

  private fun youTubeChannelFeed(channelId: String) =
    "https://www.youtube.com/feeds/videos.xml?channel_id=${channelId.encodeURLParameter()}"
}

@Serializable
private data class YouTubeOEmbed(@SerialName("author_url") val authorUrl: String? = null)
