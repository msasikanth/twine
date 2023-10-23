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

import dev.sasikanth.rss.reader.models.remote.FeedPayload
import dev.sasikanth.rss.reader.models.remote.PostPayload
import dev.sasikanth.rss.reader.network.FeedParser.Companion.ATOM_TAG
import dev.sasikanth.rss.reader.network.FeedParser.Companion.ATTR_HREF
import dev.sasikanth.rss.reader.network.FeedParser.Companion.ATTR_REL
import dev.sasikanth.rss.reader.network.FeedParser.Companion.ATTR_TYPE
import dev.sasikanth.rss.reader.network.FeedParser.Companion.ATTR_URL
import dev.sasikanth.rss.reader.network.FeedParser.Companion.ATTR_VALUE_ALTERNATE
import dev.sasikanth.rss.reader.network.FeedParser.Companion.HTML_TAG
import dev.sasikanth.rss.reader.network.FeedParser.Companion.RSS_TAG
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_ATOM_ENTRY
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_ATOM_FEED
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_ENCLOSURE
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_FEATURED_IMAGE
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_IMAGE_URL
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_LINK
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_RSS_CHANNEL
import dev.sasikanth.rss.reader.network.FeedParser.Companion.TAG_RSS_ITEM
import dev.sasikanth.rss.reader.network.FeedParser.Companion.imageTags
import dev.sasikanth.rss.reader.network.FeedType.ATOM
import dev.sasikanth.rss.reader.network.FeedType.RSS
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import dev.sasikanth.rss.reader.utils.XmlParsingError
import kotlin.collections.set
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSXMLParser
import platform.Foundation.NSXMLParserDelegateProtocol
import platform.Foundation.dataUsingEncoding
import platform.darwin.NSObject

@Inject
@Suppress("CAST_NEVER_SUCCEEDS")
class IOSFeedParser(private val dispatchersProvider: DispatchersProvider) : FeedParser {

  override suspend fun parse(
    xmlContent: String,
    feedUrl: String,
    fetchPosts: Boolean
  ): FeedPayload {
    return withContext(dispatchersProvider.io) {
      suspendCoroutine { continuation ->
        var feedPayload: FeedPayload? = null
        val data = (xmlContent as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
        val xmlFeedParser =
          IOSXmlFeedParser(feedUrl, fetchPosts) { parsedFeedPayload ->
            feedPayload = parsedFeedPayload
          }

        val parser = NSXMLParser(data).apply { delegate = xmlFeedParser }
        val parserResult = parser.parse()

        val nullableFeedPayload = feedPayload
        if (parserResult && nullableFeedPayload != null) {
          continuation.resume(nullableFeedPayload)
        } else if (!parserResult && parser.parserError() != null) {
          continuation.resumeWithException(XmlParsingError(parser.parserError()?.description))
        }
      }
    }
  }
}

private class IOSXmlFeedParser(
  private val feedUrl: String,
  private val fetchPosts: Boolean,
  private val onEnd: (FeedPayload) -> Unit
) : NSObject(), NSXMLParserDelegateProtocol {

  private val posts = mutableListOf<PostPayload?>()

  private var feedType: FeedType? = null
  private var currentChannelData: MutableMap<String, String> = mutableMapOf()
  private var currentItemData: MutableMap<String, String> = mutableMapOf()
  private var currentData: MutableMap<String, String>? = null
  private var currentElement: String? = null

  override fun parser(parser: NSXMLParser, foundCharacters: String) {
    val currentElement = currentElement ?: return
    val currentData = currentData ?: return

    when {
      !currentData.containsKey(TAG_IMAGE_URL) && currentElement == TAG_FEATURED_IMAGE -> {
        currentData[TAG_IMAGE_URL] = foundCharacters
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
          HTML_TAG -> throw HtmlContentException()
          else -> throw UnsupportedOperationException("Unknown feed type: $didStartElement")
        }
    }

    currentElement = didStartElement

    when {
      !currentItemData.containsKey(TAG_IMAGE_URL) && hasRssImageUrl(attributes) -> {
        currentItemData[TAG_IMAGE_URL] = attributes[ATTR_URL] as String
      }
      hasPodcastRssUrl() -> {
        currentItemData[TAG_LINK] = attributes[ATTR_URL] as String
      }
      hasAtomLink(attributes) -> {
        if (currentChannelData[TAG_LINK].isNullOrBlank()) {
          currentChannelData[TAG_LINK] = attributes[ATTR_HREF] as String
        }
        currentItemData[TAG_LINK] = attributes[ATTR_HREF] as String
      }
    }

    currentData =
      when (currentElement) {
        TAG_RSS_CHANNEL,
        TAG_ATOM_FEED -> currentChannelData
        TAG_RSS_ITEM,
        TAG_ATOM_ENTRY -> currentItemData
        else -> currentData
      }
  }

  override fun parser(
    parser: NSXMLParser,
    didEndElement: String,
    namespaceURI: String?,
    qualifiedName: String?
  ) {
    if (fetchPosts && (didEndElement == TAG_RSS_ITEM || didEndElement == TAG_ATOM_ENTRY)) {
      val hostLink = currentChannelData[TAG_LINK]!!
      val post =
        when (feedType) {
          RSS -> PostPayload.mapRssPost(currentItemData, hostLink)
          ATOM -> PostPayload.mapAtomPost(currentItemData, hostLink)
          null -> null
        }

      post?.let { posts.add(it) }
      currentItemData.clear()
    }
  }

  override fun parserDidEndDocument(parser: NSXMLParser) {
    val posts = posts.filterNotNull()
    val payload =
      when (feedType) {
        RSS -> FeedPayload.mapRssFeed(feedUrl, currentChannelData, posts)
        ATOM -> FeedPayload.mapAtomFeed(feedUrl, currentChannelData, posts)
        null -> null
      }

    if (payload != null) {
      onEnd(payload)
    }
  }

  private fun hasPodcastRssUrl() =
    currentElement == TAG_ENCLOSURE && currentItemData[TAG_LINK].isNullOrBlank()

  private fun hasRssImageUrl(attributes: Map<Any?, *>) =
    (imageTags.contains(currentElement) ||
      (currentElement == TAG_ENCLOSURE && attributes[ATTR_TYPE] == "image/jpeg")) &&
      attributes.containsKey(ATTR_URL)

  private fun hasAtomLink(attributes: Map<Any?, *>) =
    feedType == ATOM &&
      currentElement == TAG_LINK &&
      (attributes[ATTR_REL] == ATTR_VALUE_ALTERNATE || attributes[ATTR_REL] == null)
}
