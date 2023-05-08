package dev.sasikanth.rss.reader.network

import android.net.Uri
import android.util.Xml
import dev.sasikanth.rss.reader.models.FeedPayload
import dev.sasikanth.rss.reader.models.PostPayload
import dev.sasikanth.rss.reader.network.FeedParser.Companion.cleanText
import dev.sasikanth.rss.reader.network.FeedParser.Companion.cleanTextCompact
import dev.sasikanth.rss.reader.network.FeedParser.Companion.feedIcon
import dev.sasikanth.rss.reader.network.FeedParser.Companion.imageTags
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal class AndroidFeedParser(
  private val ioDispatcher: CoroutineDispatcher
) : FeedParser {

  private val dateFormat = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)

  override suspend fun parse(xmlContent: String): FeedPayload {
    return withContext(ioDispatcher) {
      val parser = Xml.newPullParser().apply {
        setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
      }

      return@withContext xmlContent.reader().use { reader ->
        parser.setInput(reader)

        var tag = parser.nextTag()
        while (tag != XmlPullParser.START_TAG && parser.name != "rss") {
          skip(parser)
          tag = parser.next()
        }
        parser.nextTag()

        readFeed(parser)
      }
    }
  }

  private fun readFeed(parser: XmlPullParser): FeedPayload {
    parser.require(XmlPullParser.START_TAG, null, "channel")

    val posts = mutableListOf<PostPayload>()

    var title: String? = null
    var link: String? = null
    var description: String? = null

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) continue
      when (parser.name) {
        "title" -> title = readTagText("title", parser)
        "link" -> link = readTagText("link", parser)
        "description" -> description = readTagText("description", parser)
        "item" -> posts.add(readPost(parser))
        else -> skip(parser)
      }
    }

    val domain = Uri.parse(link!!).host!!
    val iconUrl = feedIcon(domain)

    return FeedPayload(title!!, link, description!!, iconUrl, posts)
  }

  private fun readPost(parser: XmlPullParser): PostPayload {
    parser.require(XmlPullParser.START_TAG, null, "item")

    var title: String? = null
    var link: String? = null
    var description: String? = null
    var date: String? = null
    var image: String? = null

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.eventType != XmlPullParser.START_TAG) continue

      val name = parser.name
      when {
        name == "title" -> title = readTagText("title", parser)
        name == "link" -> link = readTagText("link", parser)
        name == "description" -> description = readTagText("description", parser)
        name == "pubDate" -> date = readTagText("pubDate", parser)
        imageTags.contains(name) && image.isNullOrBlank() -> image =
          readPostImageUrl(parser)

        else -> skip(parser)
      }
    }

    val dateLong: Long = date?.let {
      try {
        ZonedDateTime.parse(date, dateFormat).toEpochSecond() * 1000
      } catch (e: Throwable) {
        Napier.e("Parse date error: ${e.message}")
        null
      }
    } ?: System.currentTimeMillis()

    return PostPayload(
      cleanText(title).orEmpty(),
      cleanText(link).orEmpty(),
      cleanTextCompact(description).orEmpty(),
      image,
      dateLong
    )
  }

  private fun readPostImageUrl(parser: XmlPullParser): String? {
    val url = parser.getAttributeValue(null, "url")
    skip(parser)
    return url
  }

  private fun readTagText(tagName: String, parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, null, tagName)
    val title = readText(parser)
    parser.require(XmlPullParser.END_TAG, null, tagName)
    return title
  }

  private fun readText(parser: XmlPullParser): String {
    var result = ""
    if (parser.next() == XmlPullParser.TEXT) {
      result = parser.text
      parser.nextTag()
    }
    return result
  }

  private fun skip(parser: XmlPullParser) {
    parser.require(XmlPullParser.START_TAG, null, null)
    var depth = 1
    while (depth != 0) {
      when (parser.next()) {
        XmlPullParser.END_TAG -> depth--
        XmlPullParser.START_TAG -> depth++
      }
    }
  }
}
