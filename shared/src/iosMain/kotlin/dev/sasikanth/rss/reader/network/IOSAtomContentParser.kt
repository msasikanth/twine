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
