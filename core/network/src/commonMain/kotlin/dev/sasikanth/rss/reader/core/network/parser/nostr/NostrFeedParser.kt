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

package dev.sasikanth.rss.reader.core.network.parser.nostr

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetchResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
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
import rhodium.nostr.deserializedEvent
import rhodium.nostr.relay.Relay

object NostrFeedParser {

    private val json = Json { ignoreUnknownKeys = true }

    // The default relays to get info from, separated by purpose.
    private val DEFAULT_FETCH_RELAYS = listOf("wss://relay.nostr.band", "wss://relay.damus.io")
    private val DEFAULT_METADATA_RELAYS = listOf("wss://purplepag.es", "wss://user.kindpag.es")
    private val DEFAULT_ARTICLE_FETCH_RELAYS = setOf("wss://nos.lol") + DEFAULT_FETCH_RELAYS

    suspend fun fetchFeed(nostrUri: String, httpClient: HttpClient): FeedFetchResult {
        return fetchNostrFeed(nostrUri, httpClient)
    }

    private suspend fun fetchNostrFeed(nostrUri: String, httpClient: HttpClient): FeedFetchResult {
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

    fun parsePost(content: String): PostPayload {
        val articleEvent = deserializedEvent(content)
        return mapEventToPost(articleEvent)
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