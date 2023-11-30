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
package dev.sasikanth.rss.reader.network

import android.util.Xml
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.network.FeedParser.Companion.ATOM_TAG
import dev.sasikanth.rss.reader.network.FeedParser.Companion.HTML_TAG
import dev.sasikanth.rss.reader.network.FeedParser.Companion.RSS_TAG
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.XmlParsingError
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

@Inject
class AndroidFeedParser(private val dispatchersProvider: DispatchersProvider) : FeedParser {

  override suspend fun parse(xmlContent: String, feedUrl: String): FeedPayload {
    return try {
      withContext(dispatchersProvider.io) {
        val parser =
          Xml.newPullParser().apply { setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false) }

        return@withContext xmlContent.reader().use { reader ->
          parser.setInput(reader)
          parser.nextTag()

          when (parser.name) {
            RSS_TAG -> AndroidRssParser(parser, feedUrl).parse()
            ATOM_TAG -> AndroidAtomParser(parser, feedUrl).parse()
            HTML_TAG -> throw HtmlContentException()
            else -> throw UnsupportedOperationException("Unknown feed type: ${parser.name}")
          }
        }
      }
    } catch (e: XmlPullParserException) {
      throw XmlParsingError(e.stackTraceToString())
    }
  }
}
