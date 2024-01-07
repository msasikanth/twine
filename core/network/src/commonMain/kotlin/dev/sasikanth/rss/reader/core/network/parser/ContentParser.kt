/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.core.network.parser

import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser

abstract class ContentParser {

  abstract fun parse(): FeedPayload

  fun readAttrText(attrName: String, parser: XmlPullParser): String? {
    val url = parser.getAttributeValue(parser.namespace, attrName)
    skip(parser)
    return url
  }

  fun readTagText(tagName: String, parser: XmlPullParser): String {
    parser.require(EventType.START_TAG, parser.namespace, tagName)
    val title = readText(parser)
    parser.require(EventType.END_TAG, parser.namespace, tagName)
    return title
  }

  private fun readText(parser: XmlPullParser): String {
    var result = ""
    if (parser.next() == EventType.TEXT) {
      result = parser.text
      parser.nextTag()
    }
    return result
  }

  fun skip(parser: XmlPullParser) {
    parser.require(EventType.START_TAG, parser.namespace, null)
    var depth = 1
    while (depth != 0) {
      when (parser.next()) {
        EventType.END_TAG -> depth--
        EventType.START_TAG -> depth++
        else -> {
          // no-op
        }
      }
    }
  }
}
