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

import platform.Foundation.NSXMLParser
import platform.Foundation.NSXMLParserDelegateProtocol
import platform.darwin.NSObject

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class IOSAtomContentParser(private val onEnd: (AtomContent) -> Unit) :
  NSObject(), NSXMLParserDelegateProtocol {

  private var currentData: MutableMap<String, String> = mutableMapOf()
  private var currentElement: String? = null

  override fun parser(parser: NSXMLParser, foundCharacters: String) {
    if (currentElement == "p") {
      currentData["content"] = (currentData["content"] ?: "") + foundCharacters.trim()
    }
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
      currentElement == "img" && attributes.containsKey("src") -> {
        currentData["imageUrl"] = attributes["src"].toString()
      }
    }
  }

  override fun parserDidEndDocument(parser: NSXMLParser) {
    onEnd(
      AtomContent(imageUrl = currentData["imageUrl"], content = currentData["content"].orEmpty())
    )
    currentData.clear()
  }
}

data class AtomContent(val imageUrl: String?, val content: String)
