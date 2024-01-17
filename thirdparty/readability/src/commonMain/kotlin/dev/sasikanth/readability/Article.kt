package dev.sasikanth.readability

import com.fleeksoft.ksoup.nodes.Element

open class Article(
  /** Original uri object that was passed to constructor */
  val uri: String
) {

  /** Article title */
  var title: String? = null

  var articleContent: Element? = null

  /**
   * HTML string of processed article content in a &lt;div> element.
   *
   * Therefore, no encoding is applied, see [contentWithUtf8Encoding] or issue
   * [https://github.com/dankito/Readability4J/issues/1].
   *
   * TODO: but this removes paging information (pages in top node <div id="readability-content">)
   */
  val content: String?
    get() = articleContent?.html()

  /**
   * [content] returns a &lt;div> element.
   *
   * As the only way in HTML to set an encoding is via &lt;head>&lt;meta charset=""> tag, therefore
   * no explicit encoding is applied to it. As a result non-ASCII characters may get displayed
   * incorrectly.
   *
   * So this method wraps [content] in &lt;html>&lt;head>&lt;meta
   * charset="utf-8"/>&lt;/head>&lt;body>&lt;!-- content-->&lt;/body>&lt;/html> so that UTF-8
   * encoding gets applied.
   *
   * See [https://github.com/dankito/Readability4J/issues/1] for more info.
   */
  val contentWithUtf8Encoding: String?
    get() = getContentWithEncoding("utf-8")

  /**
   * Returns the content wrapped in an <html> element with charset set to document's charset. Or if
   * that is not set in UTF-8. See [contentWithUtf8Encoding] for more details.
   */
  val contentWithDocumentsCharsetOrUtf8: String?
    get() = getContentWithEncoding(charset ?: "utf-8")

  val textContent: String?
    get() = articleContent?.text()

  /** Length of article, in characters */
  val length: Int
    get() = textContent?.length ?: -1

  /** Article description, or short excerpt from content */
  var excerpt: String? = null

  /** Author metadata */
  var byline: String? = null

  /** Content direction */
  var dir: String? = null

  /** Article's charset */
  var charset: String? = null

  /**
   * [content] returns a &lt;div> element.
   *
   * As the only way in HTML to set an encoding is via &lt;head>&lt;meta charset=""> tag, therefore
   * no explicit encoding is applied to it. As a result non-ASCII characters may get displayed
   * incorrectly.
   *
   * So this method wraps [content] in &lt;html>&lt;head>&lt;meta
   * charset="[encoding]"/>&lt;/head>&lt;body>&lt;!-- content-->&lt;/body>&lt;/html> so that
   * encoding gets applied.
   *
   * See [https://github.com/dankito/Readability4J/issues/1] for more info.
   */
  fun getContentWithEncoding(encoding: String): String? {
    content?.let { content ->
      return "<html>\n  <head>\n    <meta charset=\"$encoding\"/>\n  </head>\n  <body>\n    " +
        "$content\n  </body>\n</html>"
    }

    return null
  }
}
