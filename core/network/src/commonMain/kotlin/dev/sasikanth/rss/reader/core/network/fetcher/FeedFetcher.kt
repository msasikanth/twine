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
package dev.sasikanth.rss.reader.core.network.fetcher

import co.touchlab.kermit.Logger
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parseSource
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.parser.FeedParser
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATOM_MEDIA_TYPE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATTR_HREF
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATTR_TYPE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.RSS_MEDIA_TYPE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils.isNostrUri
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.isRelativePath
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.asSource
import korlibs.io.lang.Charset
import korlibs.io.lang.Charsets
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import rhodium.crypto.Nip19Parser
import rhodium.crypto.tlv.entity.NAddress
import rhodium.crypto.tlv.entity.NEvent
import rhodium.crypto.tlv.entity.NProfile
import rhodium.crypto.tlv.entity.NPub
import rhodium.net.NostrService
import rhodium.net.NostrUtils
import rhodium.net.UrlUtil
import rhodium.nostr.Event
import rhodium.nostr.NostrFilter
import rhodium.nostr.client.RequestMessage
import rhodium.nostr.relay.Relay

@Inject
class FeedFetcher(private val httpClient: HttpClient, private val feedParser: FeedParser) {

  companion object {
    private const val MAX_REDIRECTS_ALLOWED = 5
    // The default relays to get info from, separated by purpose.
    private val DEFAULT_FETCH_RELAYS = listOf("wss://relay.nostr.band", "wss://relay.damus.io")
    private val DEFAULT_METADATA_RELAYS = listOf("wss://purplepag.es", "wss://user.kindpag.es")
    private val DEFAULT_ARTICLE_FETCH_RELAYS = setOf("wss://nos.lol") + DEFAULT_FETCH_RELAYS
  }

  suspend fun fetch(url: String, transformUrl: Boolean = true): FeedFetchResult {
    return if (url.isNostrUri()) fetchNostrFeed(url) else fetch(url, redirectCount = 0)
  }

  private suspend fun fetch(
    url: String,
    redirectCount: Int,
  ): FeedFetchResult {
    if (redirectCount >= MAX_REDIRECTS_ALLOWED) {
      return FeedFetchResult.TooManyRedirects
    }

    return try {
      val transformedUrl = buildFeedUrl(url)
      val response = httpClient.get(transformedUrl.toString())

      when (response.status) {
        HttpStatusCode.OK -> {
          parseContent(response, transformedUrl.toString(), redirectCount)
        }
        HttpStatusCode.MultipleChoices,
        HttpStatusCode.MovedPermanently,
        HttpStatusCode.Found,
        HttpStatusCode.SeeOther,
        HttpStatusCode.TemporaryRedirect,
        HttpStatusCode.PermanentRedirect -> {
          handleHttpRedirect(response, transformedUrl, redirectCount)
        }
        else -> {
          FeedFetchResult.HttpStatusError(statusCode = response.status)
        }
      }
    } catch (e: Exception) {
      FeedFetchResult.Error(e)
    }
  }

  private fun buildFeedUrl(urlString: String): Url {
    val url = Url(urlString)

    return if (url.isRelativePath) {
      URLBuilder()
        .apply {
          host = urlString
          protocol = url.protocol
        }
        .build()
    } else {
      url
    }
  }

  private suspend fun parseContent(
    response: HttpResponse,
    url: String,
    redirectCount: Int
  ): FeedFetchResult {
    if (response.contentType()?.withoutParameters() != ContentType.Text.Html) {
      val content = response.bodyAsChannel()
      val responseCharset = response.contentType()?.parameter("charset")
      val charset = Charset.forName(responseCharset ?: Charsets.UTF8.name)

      val feedPayload = feedParser.parse(feedUrl = url, content = content, charset = charset)

      return FeedFetchResult.Success(feedPayload)
    }

    val feedUrl = fetchFeedLinkFromHtmlIfExists(response.bodyAsChannel(), url)

    if (feedUrl != url && !feedUrl.isNullOrBlank()) {
      return fetch(url = feedUrl, redirectCount = redirectCount + 1)
    }

    throw UnsupportedOperationException()
  }

  private suspend fun handleHttpRedirect(
    response: HttpResponse,
    url: Url,
    redirectCount: Int
  ): FeedFetchResult {
    val headerLocation = response.headers["Location"]
    val redirectToUrl = UrlUtils.safeUrl(host = url.host, url = headerLocation)

    if (redirectToUrl == url.toString() || redirectToUrl.isNullOrBlank()) {
      return FeedFetchResult.Error(Exception("Failed to fetch the feed"))
    }

    return fetch(url = redirectToUrl, redirectCount = redirectCount + 1)
  }

  private fun fetchFeedLinkFromHtmlIfExists(
    htmlContent: ByteReadChannel,
    originalUrl: String
  ): String? {
    val document =
      try {
        Ksoup.parseSource(htmlContent.asSource())
      } catch (t: Throwable) {
        return null
      }

    val linkElement =
      document.getElementsByTag(TAG_LINK).firstOrNull {
        val linkType = it.attr(ATTR_TYPE)
        linkType == RSS_MEDIA_TYPE || linkType == ATOM_MEDIA_TYPE
      }
        ?: return null
    val link = linkElement.attr(ATTR_HREF)
    val host = UrlUtils.extractHost(originalUrl)

    return UrlUtils.safeUrl(host, link)
  }

  private suspend fun fetchNostrFeed(nostrUri: String): FeedFetchResult {
    val rawNostrAddress = nostrUri.removePrefix("nostr:")
    val nostrService = NostrService(client = httpClient.config { install(WebSockets) {} })
    if (
      rawNostrAddress.contains("@") || UrlUtil.isValidUrl(rawNostrAddress)
    ) { // It is a NIP05 address
      val profileInfo = NostrUtils.getProfileInfoFromAddress(nip05 = rawNostrAddress, httpClient)
      val profileIdentifier = profileInfo[0]
      val potentialRelays = profileInfo.drop(1)

      return innerFetchNostrFeed(nostrUri, profileIdentifier, potentialRelays, nostrService)
    } else {
      val parsedProfile = Nip19Parser.parse(rawNostrAddress)?.entity
      return if (parsedProfile == null) {
        FeedFetchResult.Error(Exception("Could not parse the input, as it is null"))
      } else {
        when (parsedProfile) {
          is NPub -> {
            innerFetchNostrFeed(nostrUri, parsedProfile.hex, DEFAULT_METADATA_RELAYS, nostrService)
          }
          is NProfile -> {
            innerFetchNostrFeed(nostrUri, parsedProfile.hex, parsedProfile.relay, nostrService)
          }
          else ->
            FeedFetchResult.Error(
              Exception("Could not find any profile from the input : $parsedProfile")
            )
        }
      }
    }
  }

  private suspend fun innerFetchNostrFeed(
    nostrUri: String,
    profilePubKey: String,
    profileRelays: List<String>,
    nostrService: NostrService
  ): FeedFetchResult {
    val authorInfoEvent =
      try {
        nostrService.getMetadataFor(
          profileHex = profilePubKey,
          preferredRelays = profileRelays.ifEmpty { DEFAULT_FETCH_RELAYS }
        )
      } catch (e: Exception) {
        Logger.e("NostrFetcher", e)
        nostrService.getMetadataFor(
          profileHex = profilePubKey,
          preferredRelays = DEFAULT_FETCH_RELAYS
        )
      }

    if (authorInfoEvent.content.isBlank()) {
      return FeedFetchResult.Error(
        Exception("No corresponding author profile found for this Nostr address.")
      )
    } else {
      val authorInfo = authorInfoEvent.userInfo()
      Logger.i(
        "NostrFetcher",
      ) {
        "UserInfo: $authorInfo"
      }

      val userPublishRelays =
        try {
          nostrService
            .fetchRelayListFor(
              profileHex = profilePubKey,
              fetchRelays = profileRelays.ifEmpty { DEFAULT_METADATA_RELAYS }
            )
            .filter { relay -> relay.writePolicy }
        } catch (e: Exception) {
          Logger.e("NostrFetcher", e)
          nostrService.fetchRelayListFor(
            profileHex = profilePubKey,
            fetchRelays = DEFAULT_METADATA_RELAYS
          )
        }

      val userArticlesRequest =
        RequestMessage.singleFilterRequest(
          filter = NostrFilter.newFilter().authors(profilePubKey).kinds(30023).build()
        )

      val articleEvents =
        nostrService.requestWithResult(
          userArticlesRequest,
          userPublishRelays.ifEmpty { DEFAULT_ARTICLE_FETCH_RELAYS.map { Relay(it) } }
        )

      return if (articleEvents.isEmpty())
        FeedFetchResult.Error(Exception("No articles found for ${authorInfo.name}"))
      else {
        FeedFetchResult.Success(
          FeedPayload(
            name = authorInfo.name,
            icon = authorInfo.picture ?: "",
            description = authorInfo.about ?: "",
            homepageLink = authorInfo.website ?: "",
            link = nostrUri,
            articleEvents.map { mapEventToPost(it) }
          )
        )
      }
    }
  }

  private fun mapEventToPost(event: Event): PostPayload {
    val postTitle = event.tags.find { tag -> tag.identifier == "title" }
    val image = event.tags.find { it.identifier == "image" }
    val summary = event.tags.find { it.identifier == "summary" }
    val publishDate = event.tags.find { it.identifier == "published_at" }
    val articleIdentifier = event.tags.find { it.identifier == "d" }
    val articleLink =
      event.tags
        .find { it.identifier == "a" }
        .run {
          if (this != null) {
            val tagElements = this.description.split(":")
            val address =
              NAddress.create(
                kind = tagElements[0].toInt(),
                pubKeyHex = tagElements[1],
                dTag = tagElements[2],
                relay = articleIdentifier?.description
              )

            "nostr:$address"
          } else if (articleIdentifier != null) {
            val articleAddress =
              NAddress.create(
                event.eventKind,
                event.pubkey,
                articleIdentifier.description,
                this?.customContent
              )

            "nostr:$articleAddress"
          } else "nostr:${NEvent.create(event.id, event.pubkey, event.eventKind, null)}"
        }

    val articleContent = event.content
    Logger.i("NostrFetcher") {
      "Tag date is ${publishDate?.description?.toLong()}, and Event date is ${event.creationDate}"
    }

    return PostPayload(
      title = postTitle?.description ?: "",
      link = articleLink,
      description = summary?.description ?: "",
      rawContent = articleContent,
      imageUrl = image?.description,
      date = toActualMillis(publishDate?.description?.toLong() ?: event.creationDate),
      commentsLink = null,
      isDateParsedCorrectly = true
    )
  }

  // Funny hack to determine if a timestamp is in millis or seconds.
  private fun toActualMillis(timeStamp: Long): Long {
    fun isTimestampInMilliseconds(timestamp: Long): Boolean {
      val generatedMillis = Instant.fromEpochMilliseconds(timestamp).toEpochMilliseconds()
      println("Converted timestamp : $generatedMillis")
      return generatedMillis.toString().length ==
        Clock.System.now().toEpochMilliseconds().toString().length
    }

    return if (isTimestampInMilliseconds(timeStamp)) timeStamp
    else Instant.fromEpochSeconds(timeStamp).toEpochMilliseconds()
  }
}
