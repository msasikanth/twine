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
@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package dev.sasikanth.rss.reader.network

import dev.sasikanth.rss.reader.models.FeedPayload
import dev.sasikanth.rss.reader.models.PostPayload
import dev.sasikanth.rss.reader.network.FeedParser.Companion.ATOM_TAG
import dev.sasikanth.rss.reader.network.FeedParser.Companion.RSS_TAG
import dev.sasikanth.rss.reader.network.FeedParser.Companion.cleanText
import dev.sasikanth.rss.reader.network.FeedParser.Companion.cleanTextCompact
import dev.sasikanth.rss.reader.network.FeedParser.Companion.feedIcon
import dev.sasikanth.rss.reader.network.FeedParser.Companion.imageTags
import dev.sasikanth.rss.reader.network.FeedType.*
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlin.collections.set
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSXMLParser
import platform.Foundation.NSXMLParserDelegateProtocol
import platform.Foundation.dataUsingEncoding
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject

private const val RSS_CHANNEL_TAG = "channel"
private const val RSS_ITEM_TAG = "item"
private const val ATOM_FEED_TAG = "feed"
private const val ATOM_ENTRY_TAG = "entry"

@Suppress("CAST_NEVER_SUCCEEDS")
internal class IOSFeedParser(private val ioDispatcher: CoroutineDispatcher) : FeedParser {
  private var feedType: FeedType? = null
  private var feedPayload: FeedPayload? = null

  override suspend fun parse(xmlContent: String, feedUrl: String): FeedPayload {
    return withContext(ioDispatcher) {
      suspendCoroutine { continuation ->
        val data = (xmlContent as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
        val xmlFeedParser =
          IOSXmlFeedParser(feedUrl) { feedType, feedPayload ->
            this@IOSFeedParser.feedType = feedType
            this@IOSFeedParser.feedPayload = feedPayload
          }

        val parserResult = NSXMLParser(data).apply { delegate = xmlFeedParser }.parse()

        val feedPayload = feedPayload
        if (parserResult && feedPayload != null) {
          when (feedType) {
            RSS -> continuation.resume(feedPayload)
            ATOM -> continuation.resume(expandAtomContent(feedPayload))
            Unknown,
            null -> throw UnsupportedOperationException("Unsupported feed type")
          }
        }
      }
    }
  }

  private fun expandAtomContent(feedPayload: FeedPayload): FeedPayload {
    return feedPayload.copy(posts = feedPayload.posts.mapNotNull(::parseAtomContent))
  }

  private fun parseAtomContent(postPayload: PostPayload): PostPayload? {
    var expandedPostPayload: PostPayload? = null

    val wrappedDescription = "<content>${postPayload.description}</content>"
    val data = (wrappedDescription as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
    val parserResult =
      NSXMLParser(data)
        .apply {
          delegate =
            IOSAtomContentParser(
              onEnd = {
                expandedPostPayload =
                  postPayload.copy(description = it.content, imageUrl = it.imageUrl)
              }
            )
        }
        .parse()

    if (parserResult) {
      return expandedPostPayload
    }

    return postPayload
  }
}

private class IOSXmlFeedParser(
  private val feedUrl: String,
  private val onEnd: (FeedType, FeedPayload) -> Unit
) : NSObject(), NSXMLParserDelegateProtocol {

  private val posts = mutableListOf<PostPayload>()

  var feedType: FeedType? = null
  private var currentChannelData: MutableMap<String, String> = mutableMapOf()
  private var currentItemData: MutableMap<String, String> = mutableMapOf()
  private var currentData: MutableMap<String, String>? = null
  private var currentElement: String? = null

  private val offsetTimezoneDateFormatter =
    NSDateFormatter().apply { dateFormat = "E, d MMM yyyy HH:mm:ss Z" }
  private val abbrevTimezoneDateFormatter =
    NSDateFormatter().apply { dateFormat = "E, d MMM yyyy HH:mm:ss z" }
  private val atomDateFormatter = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ" }

  override fun parser(parser: NSXMLParser, foundCharacters: String) {
    val currentElement = currentElement ?: return
    val currentData = currentData ?: return

    currentData[currentElement] = (currentData[currentElement] ?: "") + foundCharacters
  }

  override fun parser(
    parser: NSXMLParser,
    didStartElement: String,
    namespaceURI: String?,
    qualifiedName: String?,
    attributes: Map<Any?, *>
  ) {
    if (feedType == null) {
      feedType =
        when (didStartElement) {
          RSS_TAG -> RSS
          ATOM_TAG -> ATOM
          else -> Unknown
        }
    }

    currentElement = didStartElement

    when {
      !currentItemData.containsKey("imageUrl") && hasRssImageUrl(attributes) -> {
        currentItemData["imageUrl"] = attributes["url"] as String
      }
      hasPodcastRssUrl() -> {
        currentItemData["link"] = attributes["url"] as String
      }
      currentElement == "link" && attributes["rel"] == "alternate" -> {
        if (currentChannelData["link"].isNullOrBlank()) {
          currentChannelData["link"] = attributes["href"] as String
        }
        currentItemData["link"] = attributes["href"] as String
      }
    }

    currentData =
      when (currentElement) {
        RSS_CHANNEL_TAG,
        ATOM_FEED_TAG -> currentChannelData
        RSS_ITEM_TAG,
        ATOM_ENTRY_TAG -> currentItemData
        else -> currentData
      }
  }

  override fun parser(
    parser: NSXMLParser,
    didEndElement: String,
    namespaceURI: String?,
    qualifiedName: String?
  ) {
    if (didEndElement == RSS_ITEM_TAG || didEndElement == ATOM_ENTRY_TAG) {
      val post =
        when (feedType) {
          RSS -> PostPayload.mapRssPost(currentItemData)
          ATOM -> PostPayload.mapAtomPost(currentItemData)
          Unknown,
          null -> null
        }

      post?.let { posts.add(it) }
      currentItemData.clear()
    }
  }

  override fun parserDidEndDocument(parser: NSXMLParser) {
    val payload =
      when (feedType) {
        RSS -> FeedPayload.mapRssFeed(currentChannelData, posts)
        ATOM -> FeedPayload.mapAtomFeed(currentChannelData, posts)
        Unknown,
        null -> null
      }

    val feedType = feedType
    if (feedType != null && payload != null) {
      onEnd(feedType, payload)
    }
  }

  private fun hasPodcastRssUrl() =
    currentElement == "enclosure" && currentItemData["link"].isNullOrBlank()

  private fun hasRssImageUrl(attributes: Map<Any?, *>) =
    (imageTags.contains(currentElement) ||
      (currentElement == "enclosure" && attributes["type"] == "image/jpeg")) &&
      attributes.containsKey("url")

  private fun PostPayload.Companion.mapRssPost(rssMap: Map<String, String>): PostPayload {
    val pubDate = rssMap["pubDate"]
    val link = rssMap["link"]
    val description = rssMap["description"]
    val imageUrl: String? = rssMap["imageUrl"]

    return PostPayload(
      title = cleanText(rssMap["title"])!!,
      link = cleanText(link)!!,
      description = cleanTextCompact(description).orEmpty(),
      imageUrl = imageUrl,
      date = pubDate.rssDateStringToEpochSeconds()
    )
  }

  private fun FeedPayload.Companion.mapRssFeed(
    rssMap: Map<String, String>,
    posts: List<PostPayload>
  ): FeedPayload {
    val link = rssMap["link"]!!.trim()
    val domain = Url(link)
    val iconUrl =
      feedIcon(
        if (domain.host != "localhost") domain.host
        else domain.pathSegments.first().split(" ").first().trim()
      )

    return FeedPayload(
      name = cleanText(rssMap["title"])!!,
      homepageLink = link,
      link = feedUrl,
      description = cleanText(rssMap["description"])!!,
      icon = iconUrl,
      posts = posts
    )
  }

  private fun PostPayload.Companion.mapAtomPost(atomMap: Map<String, String>): PostPayload {
    val pubDate = atomMap["published"]
    val link = atomMap["link"]?.trim()
    val data = atomMap["content"]

    return PostPayload(
      title = cleanText(atomMap["title"])!!,
      link = link!!,
      description = data.orEmpty(),
      imageUrl = atomMap["imageUrl"],
      date = pubDate.atomDateStringToEpochSeconds()
    )
  }

  private fun FeedPayload.Companion.mapAtomFeed(
    atomMap: Map<String, String>,
    posts: List<PostPayload>
  ): FeedPayload {
    val link = atomMap["link"]!!.trim()
    val domain = Url(link)
    val iconUrl =
      feedIcon(
        if (domain.host != "localhost") domain.host
        else domain.pathSegments.first().split(" ").first().trim()
      )

    return FeedPayload(
      name = cleanText(atomMap["title"])!!,
      homepageLink = link,
      link = feedUrl,
      description = cleanText(atomMap["subtitle"])!!,
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
          Napier.e("Parse date error: ${e.message}")
          null
        }
      }

    return date?.timeIntervalSince1970?.times(1000)?.toLong() ?: 0L
  }

  private fun String?.atomDateStringToEpochSeconds(): Long {
    if (this.isNullOrBlank()) return 0L

    val date =
      try {
        atomDateFormatter.dateFromString(this.trim())
      } catch (e: Exception) {
        Napier.e("Parse date error: ${e.message}")
        null
      }

    return date?.timeIntervalSince1970?.times(1000)?.toLong() ?: 0L
  }
}
