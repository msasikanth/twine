package dev.sasikanth.rss.reader.network

import android.net.Uri
import dev.sasikanth.rss.reader.models.FeedPayload
import dev.sasikanth.rss.reader.models.PostPayload
import io.github.aakira.napier.Napier
import org.xmlpull.v1.XmlPullParser
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

internal class AndroidRssParser(
  private val parser: XmlPullParser,
  private val feedUrl: String
) : Parser() {

  private val rssDateFormat =
    DateTimeFormatterBuilder()
      .appendPattern("EEE, dd MMM yyyy HH:mm:ss ")
      .optionalStart()
      .appendPattern("z")
      .optionalEnd()
      .optionalStart()
      .appendPattern("Z")
      .optionalEnd()
      .toFormatter(Locale.US)

  override fun parse(): FeedPayload {
    parser.nextTag()
    parser.require(XmlPullParser.START_TAG, namespace, "channel")

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
        "item" -> posts.add(readRssItem(parser))
        else -> skip(parser)
      }
    }

    val domain = Uri.parse(link!!)
    val iconUrl = FeedParser.feedIcon(domain.host ?: domain.path!!)

    return FeedPayload(
      name = title!!,
      icon = iconUrl,
      description = description!!,
      homepageLink = link,
      link = feedUrl,
      posts = posts
    )
  }

  private fun readRssItem(parser: XmlPullParser): PostPayload {
    parser.require(XmlPullParser.START_TAG, namespace, "item")

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
        name == "enclosure" && link.isNullOrBlank() -> link = readAttrText("url", parser)
        name == "description" -> description = readTagText("description", parser)
        name == "pubDate" -> date = readTagText("pubDate", parser)
        FeedParser.imageTags.contains(name) && image.isNullOrBlank() ->
          image = readAttrText("url", parser)

        else -> skip(parser)
      }
    }

    val dateLong: Long =
      date?.let {
        try {
          ZonedDateTime.parse(date, this.rssDateFormat).toEpochSecond() * 1000
        } catch (e: Throwable) {
          Napier.e("Parse date error: ${e.message}")
          null
        }
      }
        ?: System.currentTimeMillis()

    return PostPayload(
      title = FeedParser.cleanText(title).orEmpty(),
      link = FeedParser.cleanText(link).orEmpty(),
      description = FeedParser.cleanTextCompact(description).orEmpty(),
      imageUrl = image,
      date = dateLong
    )
  }
}
