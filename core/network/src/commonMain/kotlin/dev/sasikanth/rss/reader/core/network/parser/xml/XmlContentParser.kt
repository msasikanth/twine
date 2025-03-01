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

package dev.sasikanth.rss.reader.core.network.parser.xml

import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser

abstract class XmlContentParser {

  abstract fun parse(feedUrl: String, parser: XmlPullParser): FeedPayload

  fun XmlPullParser.attrText(attrName: String): String? {
    return getAttributeValue(namespace, attrName).also { skip() }
  }

  fun XmlPullParser.skip() {
    require(EventType.START_TAG, namespace, null)
    var depth = 1
    while (depth != 0) {
      when (next()) {
        EventType.END_TAG -> depth--
        EventType.START_TAG -> depth++
        else -> {
          // no-op
        }
      }
    }
  }
}
