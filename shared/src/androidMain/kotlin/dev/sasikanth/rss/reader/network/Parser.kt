package dev.sasikanth.rss.reader.network

import dev.sasikanth.rss.reader.models.FeedPayload
import org.xmlpull.v1.XmlPullParser

internal abstract class Parser {

  val namespace: String? = null

  abstract fun parse(): FeedPayload

  fun readAttrText(attrName: String, parser: XmlPullParser): String? {
    val url = parser.getAttributeValue(namespace, attrName)
    skip(parser)
    return url
  }

  fun readTagText(tagName: String, parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, namespace, tagName)
    val title = readText(parser)
    parser.require(XmlPullParser.END_TAG, namespace, tagName)
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

  fun skip(parser: XmlPullParser) {
    parser.require(XmlPullParser.START_TAG, namespace, null)
    var depth = 1
    while (depth != 0) {
      when (parser.next()) {
        XmlPullParser.END_TAG -> depth--
        XmlPullParser.START_TAG -> depth++
      }
    }
  }
}
