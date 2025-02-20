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

import com.fleeksoft.ksoup.Ksoup
import dev.sasikanth.rss.reader.core.network.parser.FeedParser
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATOM_MEDIA_TYPE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATTR_HREF
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.ATTR_TYPE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.RSS_MEDIA_TYPE
import dev.sasikanth.rss.reader.core.network.parser.FeedParser.Companion.TAG_LINK
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.contentType
import korlibs.io.lang.Charset
import korlibs.io.lang.Charsets
import me.tatarka.inject.annotations.Inject
import rhodium.crypto.Nip19Parser
import rhodium.crypto.tlv.entity.NProfile
import rhodium.crypto.tlv.entity.NPub
import rhodium.net.NostrService
import rhodium.net.NostrUtils
import rhodium.net.UrlUtil
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
    return fetch(url, transformUrl, redirectCount = 0)
  }

  private suspend fun fetch(
    url: String,
    transformUrl: Boolean,
    redirectCount: Int,
  ): FeedFetchResult {
    if (redirectCount >= MAX_REDIRECTS_ALLOWED) {
      return FeedFetchResult.TooManyRedirects
    }

    return try {
      // We are mainly doing this to avoid creating duplicates while refreshing feeds
      // after the app update
      val transformedUrl = transformUrl(url, transformUrl)
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
          handleHttpRedirect(response, transformedUrl.toString(), redirectCount)
        }
        else -> {
          FeedFetchResult.HttpStatusError(statusCode = response.status)
        }
      }
    } catch (e: Exception) {
      FeedFetchResult.Error(e)
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

    val feedUrl = fetchFeedLinkFromHtmlIfExists(response.bodyAsText(), url)

    if (feedUrl != url && !feedUrl.isNullOrBlank()) {
      return fetch(url = feedUrl, transformUrl = false, redirectCount = redirectCount + 1)
    }

    throw UnsupportedOperationException()
  }

  private suspend fun handleHttpRedirect(
    response: HttpResponse,
    url: String,
    redirectCount: Int
  ): FeedFetchResult {
    val newUrl = response.headers["Location"]
    return if (newUrl != url && !newUrl.isNullOrBlank()) {
      fetch(url = newUrl, transformUrl = false, redirectCount = redirectCount + 1)
    } else {
      FeedFetchResult.Error(Exception("Failed to fetch the feed"))
    }
  }

  private fun transformUrl(url: String, transformUrl: Boolean): Url {
    return if (transformUrl) {
      // Currently Ktor Url parses relative URLs,
      // if it fails to properly parse the given URL, it
      // default to localhost.
      //
      // This will cause the network call to fail,
      // so we are setting the host manually
      // https://youtrack.jetbrains.com/issue/KTOR-360
      URLBuilder()
        .apply {
          protocol = URLProtocol.HTTPS
          host = url.replace(Regex("^https?://"), "")
        }
        .build()
    } else {
      URLBuilder(url).apply { protocol = URLProtocol.HTTPS }.build()
    }
  }

  private fun fetchFeedLinkFromHtmlIfExists(htmlContent: String, originalUrl: String): String? {
    val document =
      try {
        Ksoup.parse(htmlContent)
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
    val host = URLBuilder(originalUrl).build().host
    val rootUrl = "https://$host"

    return FeedParser.safeUrl(rootUrl, link)
  }

  private suspend fun fetchNostrFeed(nostrUri: String): FeedFetchResult {
    val rawNostrAddress = nostrUri.removePrefix("nostr:")
    val nostrService = NostrService(client = httpClient.config { install(WebSockets){ } })
    if (rawNostrAddress.contains("@") || UrlUtil.isValidUrl(rawNostrAddress)) { // It is a NIP05 address
      val profileInfo = NostrUtils.getProfileInfoFromAddress(nip05 = rawNostrAddress, httpClient)
      val profileIdentifier = profileInfo[0]
      val potentialRelays = profileInfo.drop(1)

      return innerFetchNostrFeed(profileIdentifier, potentialRelays, nostrService)
    }
    else {
      val parsedProfile = Nip19Parser.parse(rawNostrAddress)?.entity
      return if (parsedProfile == null){
        FeedFetchResult.Error(Exception("Could not parse the input, as it is null"))
      } else {
        when(parsedProfile){
          is NPub -> {
            innerFetchNostrFeed(parsedProfile.hex, DEFAULT_METADATA_RELAYS, nostrService)
          }

          is NProfile -> {
            innerFetchNostrFeed(parsedProfile.hex, parsedProfile.relay, nostrService)
          }

          else -> FeedFetchResult.Error(Exception("Could not find any profile from the input : $parsedProfile"))
          }
      }
    }
  }

  private suspend fun innerFetchNostrFeed(
    profilePubKey: String,
    profileRelays: List<String>,
    nostrService: NostrService
  ): FeedFetchResult {
    val authorInfo = nostrService.getMetadataFor(
      profileHex = profilePubKey,
      preferredRelays = profileRelays.ifEmpty { DEFAULT_METADATA_RELAYS }
    )

    val userPublishRelays = nostrService.fetchRelayListFor(
      profileHex = profilePubKey,
      fetchRelays = profileRelays.ifEmpty { DEFAULT_FETCH_RELAYS }
    ).filter { relay -> relay.writePolicy }

    val userArticlesRequest = RequestMessage.singleFilterRequest(
      filter = NostrFilter.newFilter()
        .authors(profilePubKey)
        .kinds(30023)
        .build()
    )

    val articleEvents = nostrService.requestWithResult(
      userArticlesRequest,
      userPublishRelays.ifEmpty { DEFAULT_ARTICLE_FETCH_RELAYS.map { Relay(it) } }
    )

  }


}
