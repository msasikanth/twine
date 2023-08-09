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
import dev.sasikanth.rss.reader.network.FeedParser.Companion.cleanText
import dev.sasikanth.rss.reader.network.FeedParser.Companion.cleanTextCompact
import dev.sasikanth.rss.reader.network.FeedParser.Companion.feedIcon
import dev.sasikanth.rss.reader.network.FeedParser.Companion.imageTags
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

internal class IOSFeedParser(private val ioDispatcher: CoroutineDispatcher) : FeedParser {

  @Suppress("CAST_NEVER_SUCCEEDS")
  override suspend fun parse(xmlContent: String, feedUrl: String): FeedPayload {
    return withContext(ioDispatcher) {
      suspendCoroutine { continuation ->
        val data = (xmlContent as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
        NSXMLParser(data)
          .apply { delegate = IOSXmlFeedParser(feedUrl) { continuation.resume(it) } }
          .parse()
      }
    }
  }
}

private class IOSXmlFeedParser(
  private val feedUrl: String,
  private val onEnd: (FeedPayload) -> Unit
) : NSObject(), NSXMLParserDelegateProtocol {
  private val posts = mutableListOf<PostPayload>()

  private var currentChannelData: MutableMap<String, String> = mutableMapOf()
  private var currentItemData: MutableMap<String, String> = mutableMapOf()
  private var currentData: MutableMap<String, String>? = null
  private var currentElement: String? = null

  private val offsetTimezoneDateFormatter =
    NSDateFormatter().apply { dateFormat = "E, d MMM yyyy HH:mm:ss Z" }
  private val abbrevTimezoneDateFormatter =
    NSDateFormatter().apply { dateFormat = "E, d MMM yyyy HH:mm:ss z" }

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
    currentElement = didStartElement

    when {
      hasRssImageUrl(attributes) -> {
        currentItemData["imageUrl"] = attributes["url"] as String
      }
      hasPodcastRssUrl() -> {
        currentItemData["link"] = attributes["url"] as String
      }
    }

    currentData =
      when (currentElement) {
        "channel" -> currentChannelData
        "item" -> currentItemData
        else -> currentData
      }
  }

  override fun parser(
    parser: NSXMLParser,
    didEndElement: String,
    namespaceURI: String?,
    qualifiedName: String?
  ) {
    if (didEndElement == "item") {
      posts.add(PostPayload.mapRssPost(currentItemData))
      currentItemData.clear()
    }
  }

  override fun parserDidEndDocument(parser: NSXMLParser) {
    onEnd(FeedPayload.mapRssFeed(currentChannelData, posts))
  }

  private fun hasPodcastRssUrl() =
    currentElement == "enclosure" && currentItemData["link"].isNullOrBlank()

  private fun hasRssImageUrl(attributes: Map<Any?, *>) =
    imageTags.contains(currentElement) &&
      !currentItemData.containsKey("imageUrl") &&
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
      date = pubDate.dateStringToEpochSeconds()
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

  private fun String?.dateStringToEpochSeconds(): Long {
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
}
