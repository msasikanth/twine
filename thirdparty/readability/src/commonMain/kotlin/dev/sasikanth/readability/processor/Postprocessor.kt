package dev.sasikanth.readability.processor

import com.eygraber.uri.Uri
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.TextNode

open class Postprocessor {

  companion object {
    val ABSOLUTE_URI_PATTERN = Regex("^[a-zA-Z][a-zA-Z0-9\\+\\-\\.]*:")

    // These are the classes that readability sets itself.
    val CLASSES_TO_PRESERVE = listOf("readability-styled", "page")
  }

  open fun postProcessContent(
    originalDocument: Document,
    articleContent: Element,
    articleUri: String,
    additionalClassesToPreserve: List<String> = emptyList()
  ) {
    // Readability cannot open relative uris, so we convert them to absolute uris.
    fixRelativeUris(originalDocument, articleContent, articleUri)

    // Remove IDs and classes.
    // Remove classes.
    val classesToPreserve =
      listOf(CLASSES_TO_PRESERVE, additionalClassesToPreserve).flatten().toSet()
    cleanClasses(articleContent, classesToPreserve)
  }

  /**
   * Converts each <a> and <img> uri in the given element to an absolute URI, ignoring #ref URIs.
   */
  protected open fun fixRelativeUris(
    originalDocument: Document,
    element: Element,
    articleUri: String
  ) {
    try {
      val uri = Uri.parseOrNull(articleUri)
      val scheme = uri?.scheme
      val path = uri?.path

      if (scheme != null && path != null) {
        val prePath = scheme + "://" + uri.host
        val pathBase = scheme + "://" + uri.host + path.substring(0, path.lastIndexOf("/") + 1)

        fixRelativeUris(originalDocument, element, scheme, prePath, pathBase)
      }
    } catch (e: Exception) {
      // TODO: Log error
    }
  }

  protected open fun fixRelativeUris(
    originalDocument: Document,
    element: Element,
    scheme: String,
    prePath: String,
    pathBase: String
  ) {

    fixRelativeAnchorUris(element, scheme, prePath, pathBase)

    fixRelativeImageUris(element, scheme, prePath, pathBase)
  }

  protected open fun fixRelativeAnchorUris(
    element: Element,
    scheme: String,
    prePath: String,
    pathBase: String
  ) {
    element.getElementsByTag("a").forEach { link ->
      val href = link.attr("href")
      if (href.isNotBlank()) {
        // Replace links with javascript: URIs with text content, since
        // they won't work after scripts have been removed from the page.
        if (href.indexOf("javascript:") == 0) {
          val text = TextNode(link.wholeText())
          link.replaceWith(text)
        } else {
          link.attr("href", toAbsoluteURI(href, scheme, prePath, pathBase))
        }
      }
    }
  }

  protected open fun fixRelativeImageUris(
    element: Element,
    scheme: String,
    prePath: String,
    pathBase: String
  ) {
    element.getElementsByTag("img").forEach { img ->
      fixRelativeImageUri(img, scheme, prePath, pathBase)
    }
  }

  protected open fun fixRelativeImageUri(
    img: Element,
    scheme: String,
    prePath: String,
    pathBase: String
  ) {
    val src = img.attr("src")

    if (src.isNotBlank()) {
      img.attr("src", toAbsoluteURI(src, scheme, prePath, pathBase))
    }
  }

  protected open fun toAbsoluteURI(
    uri: String,
    scheme: String,
    prePath: String,
    pathBase: String
  ): String {
    // If this is already an absolute URI, return it.
    if (isAbsoluteUri(uri) || uri.length <= 2) {
      return uri
    }

    // Scheme-rooted relative URI.
    if (uri.substring(0, 2) == "//") {
      return scheme + "://" + uri.substring(2)
    }

    // Pre path-rooted relative URI.
    if (uri[0] == '/') {
      return prePath + uri
    }

    // Dots lash relative URI.
    if (uri.indexOf("./") == 0) {
      return pathBase + uri.substring(2)
    }

    // Ignore hash URIs:
    if (uri[0] == '#') {
      return uri
    }

    // Standard relative URI add entire path. pathBase already includes a
    // trailing "/".
    return pathBase + uri
  }

  protected open fun isAbsoluteUri(uri: String): Boolean {
    return ABSOLUTE_URI_PATTERN.containsMatchIn(uri)
  }

  /**
   * Removes the class="" attribute from every element in the given subtree, except those that match
   * CLASSES_TO_PRESERVE and the classesToPreserve array from the options object.
   */
  protected open fun cleanClasses(node: Element, classesToPreserve: Set<String>) {
    val classNames = node.classNames().filter { classesToPreserve.contains(it) }

    if (classNames.isNotEmpty()) {
      node.classNames(classNames.toMutableSet())
    } else {
      node.removeAttr("class")
    }

    node.children().forEach { child -> cleanClasses(child, classesToPreserve) }
  }
}
