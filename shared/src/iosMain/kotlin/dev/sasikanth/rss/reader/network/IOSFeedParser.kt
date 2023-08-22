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
import dev.sasikanth.rss.reader.network.FeedParser.Companion.imageTags
import dev.sasikanth.rss.reader.network.FeedType.*
import dev.sasikanth.rss.reader.utils.XmlParsingError
import kotlin.collections.set
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSXMLParser
import platform.Foundation.NSXMLParserDelegateProtocol
import platform.Foundation.dataUsingEncoding
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

        val parser = NSXMLParser(data).apply { delegate = xmlFeedParser }
        val parserResult = parser.parse()

        val feedPayload = feedPayload
        if (parserResult && feedPayload != null) {
          continuation.resume(feedPayload)
        } else if (!parserResult && parser.parserError() != null) {
          continuation.resumeWithException(XmlParsingError(parser.parserError()?.description))
        }
      }
    }
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

  override fun parser(parser: NSXMLParser, foundCharacters: String) {
    val currentElement = currentElement ?: return
    val currentData = currentData ?: return

    when {
      !currentData.containsKey("imageUrl") && currentElement == "featuredImage" -> {
        currentData["imageUrl"] = foundCharacters
      }
      else -> {
        currentData[currentElement] = (currentData[currentElement] ?: "") + foundCharacters
      }
    }
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
          else -> throw UnsupportedOperationException("Unknown feed type: $didStartElement")
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
      feedType == ATOM &&
        currentElement == "link" &&
        (attributes["rel"] == "alternate" || attributes["rel"] == null) -> {
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
          null -> null
        }

      post?.let { posts.add(it) }
      currentItemData.clear()
    }
  }

  override fun parserDidEndDocument(parser: NSXMLParser) {
    val payload =
      when (feedType) {
        RSS -> FeedPayload.mapRssFeed(feedUrl, currentChannelData, posts)
        ATOM -> FeedPayload.mapAtomFeed(feedUrl, currentChannelData, posts)
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
}
