package dev.sasikanth.readability

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import dev.sasikanth.readability.model.ArticleMetadata
import dev.sasikanth.readability.model.ReadabilityOptions
import dev.sasikanth.readability.processor.ArticleGrabber
import dev.sasikanth.readability.processor.MetadataParser
import dev.sasikanth.readability.processor.Postprocessor
import dev.sasikanth.readability.processor.Preprocessor
import dev.sasikanth.readability.util.RegExUtil

open class Readability {

  protected val uri: String

  protected val content: String

  protected val options: ReadabilityOptions

  protected val regEx: RegExUtil

  protected val preprocessor: Preprocessor

  protected val metadataParser: MetadataParser

  protected val articleGrabber: ArticleGrabber

  protected val postprocessor: Postprocessor

  constructor(
    uri: String,
    content: String,
    options: ReadabilityOptions = ReadabilityOptions(),
    regExUtil: RegExUtil = RegExUtil(),
    preprocessor: Preprocessor = Preprocessor(regExUtil),
    metadataParser: MetadataParser = MetadataParser(regExUtil),
    articleGrabber: ArticleGrabber = ArticleGrabber(options, regExUtil),
    postprocessor: Postprocessor = Postprocessor()
  ) {
    this.uri = uri
    this.content = content
    this.options = options

    this.regEx = regExUtil
    this.preprocessor = preprocessor
    this.metadataParser = metadataParser
    this.articleGrabber = articleGrabber
    this.postprocessor = postprocessor
  }

  /**
   * Runs readability.
   *
   * Workflow:
   * 1. Prep the document by removing script tags, css, etc.
   * 2. Build readability's DOM tree.
   * 3. Grab the article content from the current dom tree.
   * 4. Replace the current DOM tree with the new one.
   * 5. Read peacefully.
   */
  open fun parse(): Article? {
    val document =
      try {
        Ksoup.parse(content)
      } catch (e: Exception) {
        return null
      }

    // Avoid parsing too large documents, as per configuration option
    if (options.maxElemsToParse > 0) {
      val numTags = document.getElementsByTag("*").size
      if (numTags > options.maxElemsToParse) {
        throw Exception(
          "Aborting parsing document; $numTags elements found, but ReadabilityOption.maxElemsToParse is set to ${options.maxElemsToParse}"
        )
      }
    }

    val article = Article(uri)

    preprocessor.prepareDocument(document)

    val metadata = metadataParser.getArticleMetadata(document)

    val articleContent = articleGrabber.grabArticle(document, metadata)

    // TODO: or return null if grabbing didn't work?
    articleContent?.let {
      postprocessor.postProcessContent(
        document,
        articleContent,
        uri,
        options.additionalClassesToPreserve
      )

      article.articleContent = articleContent
    }

    setArticleMetadata(article, metadata, articleContent)

    return article
  }

  protected open fun setArticleMetadata(
    article: Article,
    metadata: ArticleMetadata,
    articleContent: Element?
  ) {
    // If we haven't found an excerpt in the article's metadata, use the article's
    // first paragraph as the excerpt. This is used for displaying a preview of
    // the article's content.
    if (metadata.excerpt.isNullOrBlank()) {
      articleContent?.getElementsByTag("p")?.first()?.let { firstParagraph ->
        metadata.excerpt = firstParagraph.text().trim()
      }
    }

    article.title = metadata.title
    article.byline =
      if (metadata.byline.isNullOrBlank()) articleGrabber.articleByline else metadata.byline
    article.dir = articleGrabber.articleDir
    article.excerpt = metadata.excerpt
    article.charset = metadata.charset
  }
}
