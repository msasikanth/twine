package dev.sasikanth.rss.reader.network

import dev.sasikanth.rss.reader.models.FeedPayload
import dev.sasikanth.rss.reader.models.PostPayload
import dev.sasikanth.rss.reader.network.FeedParser
import dev.sasikanth.rss.reader.network.FeedParser.Companion.cleanText
import dev.sasikanth.rss.reader.network.FeedParser.Companion.cleanTextCompact
import dev.sasikanth.rss.reader.network.FeedParser.Companion.feedIcon
import dev.sasikanth.rss.reader.network.FeedParser.Companion.imageTags
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSXMLParser
import platform.Foundation.NSXMLParserDelegateProtocol
import platform.Foundation.dataUsingEncoding
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class IOSFeedParser : FeedParser {

  @Suppress("CAST_NEVER_SUCCEEDS")
  override suspend fun parse(xmlContent: String): FeedPayload {
    return withContext(Dispatchers.Default) {
      suspendCoroutine { continuation ->
        val data = (xmlContent as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
        NSXMLParser(data).apply {
          delegate = IOSXmlFeedParser { continuation.resume(it) }
        }.parse()
      }
    }
  }
}

private class IOSXmlFeedParser(
  private val onEnd: (FeedPayload) -> Unit
) : NSObject(), NSXMLParserDelegateProtocol {
  private val posts = mutableListOf<PostPayload>()

  private var currentChannelData: MutableMap<String, String> = mutableMapOf()
  private var currentItemData: MutableMap<String, String> = mutableMapOf()
  private var currentData: MutableMap<String, String>? = null
  private var currentElement: String? = null

  private val dateFormatter = NSDateFormatter().apply {
    dateFormat = "E, d MMM yyyy HH:mm:ss Z"
  }

  override fun parser(
    parser: NSXMLParser,
    didStartElement: String,
    namespaceURI: String?,
    qualifiedName: String?,
    attributes: Map<Any?, *>
  ) {
    currentElement = didStartElement
    currentData = when (currentElement) {
      "channel" -> currentChannelData
      "item" -> currentItemData
      else -> currentData
    }
  }

  override fun parser(parser: NSXMLParser, foundCharacters: String) {
    val currentElement = currentElement ?: return
    val currentData = currentData ?: return

    currentData[currentElement] = (currentData[currentElement] ?: "") + foundCharacters
  }

  override fun parser(
    parser: NSXMLParser,
    didEndElement: String,
    namespaceURI: String?,
    qualifiedName: String?
  ) {
    if (didEndElement == "item") {
      posts.add(PostPayload.withMap(currentItemData))
      currentItemData.clear()
    }
  }

  override fun parserDidEndDocument(parser: NSXMLParser) {
    onEnd(FeedPayload.withMap(currentChannelData, posts))
  }

  private fun PostPayload.Companion.withMap(rssMap: Map<String, String>): PostPayload {
    val pubDate = rssMap["pubDate"]
    val date =
      if (pubDate != null)
        dateFormatter.dateFromString(pubDate.trim())?.timeIntervalSince1970
      else
        null
    val link = rssMap["link"]
    val description = rssMap["description"]
    val imageUrl: String? = rssMap.firstNotNullOfOrNull {
      if (imageTags.contains(it.key)) {
        it.value
      } else {
        null
      }
    }

    return PostPayload(
      title = cleanText(rssMap["title"])!!,
      link = cleanText(link)!!,
      description = cleanTextCompact(description).orEmpty(),
      imageUrl = imageUrl,
      date = date?.toLong() ?: 0
    )
  }

  private fun FeedPayload.Companion.withMap(
    rssMap: Map<String, String>,
    posts: List<PostPayload>
  ): FeedPayload {
    val link = rssMap["link"]!!
    val domain = Url(link).host
    val iconUrl = feedIcon(domain)

    return FeedPayload(
      name = rssMap["title"]!!,
      link = link,
      description = rssMap["description"]!!,
      icon = iconUrl,
      posts = posts
    )
  }
}
