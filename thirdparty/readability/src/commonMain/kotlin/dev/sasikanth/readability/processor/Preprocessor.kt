package dev.sasikanth.readability.processor

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import dev.sasikanth.readability.util.RegExUtil

/** Performs basic sanitization before starting the extraction process. */
open class Preprocessor(protected val regEx: RegExUtil = RegExUtil()) : ProcessorBase() {

  /**
   * Prepare the HTML document for readability to scrape it. This includes things like stripping
   * javascript, CSS, and handling terrible markup.
   */
  open fun prepareDocument(document: Document) {
    removeScripts(document)
    removeNoScripts(document)

    removeStyles(document)

    removeForms(document) // TODO: this is not in Mozilla's Readability

    removeComments(document) // TODO: this is not in Mozilla's Readability

    replaceBrs(document, regEx)

    replaceNodes(document, "font", "span")
  }

  protected open fun removeScripts(document: Document) {
    removeNodes(document, "script") { scriptNode ->
      scriptNode.value("") // TODO: what is this good for?
      scriptNode.removeAttr("src")
      true
    }
  }

  protected open fun removeNoScripts(document: Document) {
    document.getElementsByTag("noscript").forEach { noscript ->
      if (
        shouldKeepImageInNoscriptElement(document, noscript)
      ) { // TODO: this is not in Mozilla's Readability
        noscript.unwrap()
      } else {
        printAndRemove(noscript, "removeScripts('noscript')")
      }
    }
  }

  protected open fun shouldKeepImageInNoscriptElement(
    document: Document,
    noscript: Element
  ): Boolean {
    val images = noscript.select("img")
    if (images.size > 0) {
      val imagesToKeep = ArrayList(images)

      images.forEach { image ->
        // thanks to swuqi (https://github.com/swuqi) for reporting this bug.
        // see https://github.com/dankito/Readability4J/issues/4
        val source = image.attr("src")
        if (source.isNotBlank() && document.select("img[src=$source]").size > 0) {
          imagesToKeep.remove(image)
        }
      }

      return imagesToKeep.size > 0
    }

    return false
  }

  protected open fun removeStyles(document: Document) {
    removeNodes(document, "style")
  }

  protected open fun removeForms(document: Document) {
    removeNodes(document, "form")
  }

  protected open fun removeComments(node: Node) {
    var i = 0
    while (i < node.childNodeSize()) {
      val child = node.childNode(i)
      if (child.nodeName() == "#comment") {
        printAndRemove(child, "removeComments")
      } else {
        removeComments(child)
        i++
      }
    }
  }

  /**
   * Replaces 2 or more successive <br> elements with a single <p>. Whitespace between <br> elements
   * are ignored. For example:
   * <div>foo<br>bar<br> <br><br>abc</div> will become:
   * <div>foo<br>bar<p>abc</p></div>
   */
  protected open fun replaceBrs(document: Document, regEx: RegExUtil) {
    document.body().select("br").forEach { br ->
      var next: Node? = br.nextSibling()

      // Whether 2 or more <br> elements have been found and replaced with a
      // <p> block.
      var replaced = false

      // If we find a <br> chain, remove the <br>s until we hit another element
      // or non-whitespace. This leaves behind the first <br> in the chain
      // (which will be replaced with a <p> later).
      next = nextElement(next, regEx)
      while (next != null && next.nodeName() == "br") {
        replaced = true
        val brSibling = (next as? Element)?.nextSibling()
        printAndRemove(next, "replaceBrs")
        next = nextElement(brSibling, regEx)
      }

      // If we removed a <br> chain, replace the remaining <br> with a <p>. Add
      // all sibling nodes as children of the <p> until we hit another <br>
      // chain.
      if (replaced) {
        val p = br.ownerDocument()?.createElement("p")
        if (p != null) {
          br.replaceWith(p)

          next = p.nextSibling()
          while (next != null) {
            // If we've hit another <br><br>, we're done adding children to this <p>.
            if (next.nodeName() == "br") {
              val nextElem = this.nextElement(next, regEx)
              if (nextElem != null && nextElem.tagName() == "br") break
            }

            // Otherwise, make this node a child of the new <p>.
            val sibling = next.nextSibling()
            p.appendChild(next)
            next = sibling
          }
        }
      }
    }
  }
}
