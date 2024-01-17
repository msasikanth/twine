package dev.sasikanth.readability.processor

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.TextNode
import com.fleeksoft.ksoup.select.Elements
import dev.sasikanth.readability.model.ArticleGrabberOptions
import dev.sasikanth.readability.model.ArticleMetadata
import dev.sasikanth.readability.model.ReadabilityObject
import dev.sasikanth.readability.model.ReadabilityOptions
import dev.sasikanth.readability.util.RegExUtil
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

open class ArticleGrabber(
  protected val options: ReadabilityOptions,
  protected val regEx: RegExUtil = RegExUtil()
) : ProcessorBase() {

  companion object {
    // Element tags to score by default.
    private val DEFAULT_TAGS_TO_SCORE =
      listOf("section", "h2", "h3", "h4", "h5", "h6", "p", "td", "pre")

    private val DIV_TO_P_ELEMS =
      listOf("a", "blockquote", "dl", "div", "img", "ol", "p", "pre", "table", "ul", "select")

    private val ALTER_TO_DIV_EXCEPTIONS = listOf("div", "article", "section", "p")

    private val PRESENTATIONAL_ATTRIBUTES =
      listOf(
        "align",
        "background",
        "bgcolor",
        "border",
        "cellpadding",
        "cellspacing",
        "frame",
        "hspace",
        "rules",
        "style",
        "valign",
        "vspace"
      )

    private val DEPRECATED_SIZE_ATTRIBUTE_ELEMS = listOf("table", "th", "td", "hr", "pre")

    private val EMBEDDED_NODES = listOf("object", "embed", "iframe")

    private val DATA_TABLE_DESCENDANTS = listOf("col", "colgroup", "tfoot", "thead", "th")

    private const val MINIMUM_TOP_CANDIDATES = 3
  }

  var articleByline: String? = null
    protected set

  var articleDir: String? = null
    protected set

  protected val nbTopCandidates = options.nbTopCandidates
  protected val wordThreshold = options.wordThreshold

  protected val readabilityObjects = HashMap<Element, ReadabilityObject>()
  protected val readabilityDataTable = HashMap<Element, Boolean>()

  open fun grabArticle(
    doc: Document,
    metadata: ArticleMetadata,
    options: ArticleGrabberOptions = ArticleGrabberOptions(),
    pageElement: Element? = null
  ): Element? {
    val isPaging = pageElement != null
    val page = pageElement ?: doc.body()

    val pageCacheHtml = doc.html()

    while (true) {
      // First, node prepping. Trash nodes that look cruddy (like ones with the
      // class name "comment", etc.), and turn divs into P tags where they have been
      // used inappropriately (as in, where they contain no other block level elements.)
      val elementsToScore = prepareNodes(doc, options)

      /**
       * Loop through all paragraphs, and assign a score to them based on how content-y they look.
       * Then add their score to their parent node.
       *
       * A score is determined by things like number of commas, class names, etc. Maybe eventually
       * link density.
       */
      val candidates = scoreElements(elementsToScore, options)

      // After we've calculated scores, loop through all the possible
      // candidate nodes we found and find the one with the highest score.
      val topCandidateResult = getTopCandidate(page, candidates, options)
      val topCandidate = topCandidateResult.first
      val neededToCreateTopCandidate = topCandidateResult.second

      // Now that we have the top candidate, look through its siblings for content
      // that might also be related. Things like preambles, content split by ads
      // that we removed, etc.
      var articleContent = createArticleContent(doc, topCandidate, isPaging)

      // So we have all the content that we need. Now we clean it up for presentation.
      prepArticle(articleContent, options, metadata)

      if (neededToCreateTopCandidate) {
        // We already created a fake div thing, and there wouldn't have been any siblings left
        // for the previous loop, so there's no point trying to create a new div, and then
        // move all the children over. Just assign IDs and class names here. No need to append
        // because that already happened anyway.
        topCandidate.attr("id", "readability-page-1")
        topCandidate.addClass("page")
      } else {
        val div = doc.createElement("div")
        div.attr("id", "readability-page-1")
        div.addClass("page")

        articleContent.childNodes().forEach { child ->
          child.remove()
          div.appendChild(child)
        }

        articleContent.appendChild(div)
      }

      var parseSuccessful = true
      val attempts = ArrayList<Pair<Element, Int>>()

      // Now that we've gone through the full algorithm, check to see if
      // we got any meaningful content. If we didn't, we may need to re-run
      // grabArticle with different flags set. This gives us a higher likelihood of
      // finding the content, and the sieve approach gives us a higher likelihood of
      // finding the -right- content.
      val textLength = getInnerText(articleContent, regEx, true).length
      if (textLength < this.wordThreshold) {
        parseSuccessful = false
        page.html(pageCacheHtml)

        if (options.stripUnlikelyCandidates) {
          options.stripUnlikelyCandidates = false
          attempts.add(Pair(articleContent, textLength))
        } else if (options.weightClasses) {
          options.weightClasses = false
          attempts.add(Pair(articleContent, textLength))
        } else if (options.cleanConditionally) {
          options.cleanConditionally = false
          attempts.add(Pair(articleContent, textLength))
        } else {
          attempts.add(Pair(articleContent, textLength))
          // No luck after removing flags, just return the longest text we found during the
          // different loops
          attempts.sortBy { it.second }

          // But first check if we actually have something
          if (attempts.isEmpty() || attempts[0].second <= 0) {
            return null
          }

          articleContent = attempts[0].first
          parseSuccessful = true
        }
      }

      if (parseSuccessful) {
        // Find out text direction from ancestors of final top candidate.
        getTextDirection(topCandidate, doc)

        return articleContent
      }
    }
  }

  protected open fun prepareNodes(doc: Document, options: ArticleGrabberOptions): List<Element> {
    val elementsToScore = ArrayList<Element>()
    var node: Element? = doc

    while (node != null) {
      val matchString = node.className() + " " + node.id()

      // Check to see if this node is a byline, and remove it if it is.
      if (checkByline(node, matchString)) {
        node = removeAndGetNext(node, "byline")
        continue
      }

      // Remove unlikely candidates
      if (options.stripUnlikelyCandidates) {
        if (
          regEx.isUnlikelyCandidate(matchString) &&
            !regEx.okMaybeItsACandidate(matchString) &&
            node.tagName() != "body" &&
            node.tagName() != "a"
        ) {
          node = this.removeAndGetNext(node, "Removing unlikely candidate")
          continue
        }
      }

      // Remove DIV, SECTION, and HEADER nodes without any content(e.g. text, image, video, or
      // iframe).
      if (
        (node.tagName() == "div" ||
          node.tagName() == "section" ||
          node.tagName() == "header" ||
          node.tagName() == "h1" ||
          node.tagName() == "h2" ||
          node.tagName() == "h3" ||
          node.tagName() == "h4" ||
          node.tagName() == "h5" ||
          node.tagName() == "h6") && this.isElementWithoutContent(node)
      ) {
        node = this.removeAndGetNext(node, "node without content")
        continue
      }

      if (DEFAULT_TAGS_TO_SCORE.contains(node.tagName())) {
        elementsToScore.add(node)
      }

      // Turn all divs that don't have children block level elements into p's
      if (node.tagName() == "div") {
        // Sites like http://mobile.slate.com encloses each paragraph with a DIV
        // element. DIVs with only a P element inside and no text content can be
        // safely converted into plain P elements to avoid confusing the scoring
        // algorithm with DIVs with are, in practice, paragraphs.
        if (this.hasSinglePInsideElement(node)) {
          val newNode = node.child(0)
          node.replaceWith(newNode)
          node = newNode
          elementsToScore.add(node)
        } else if (!this.hasChildBlockElement(node)) {
          setNodeTag(node, "p")
          elementsToScore.add(node)
        } else {
          // EXPERIMENTAL
          node.childNodes().forEach { childNode ->
            if (childNode is TextNode && childNode.text().trim().isNotEmpty()) {
              val p = doc.createElement("p")
              p.text(childNode.text())
              p.attr("style", "display: inline;")
              p.addClass("readability-styled")
              childNode.replaceWith(p)
            }
          }
        }
      }

      node = this.getNextNode(node)
    }

    return elementsToScore
  }

  protected open fun checkByline(node: Element, matchString: String): Boolean {
    if (this.articleByline != null) {
      return false
    }

    val rel = node.attr("rel")

    if ((rel == "author" || regEx.isByline(matchString)) && isValidByline(node.wholeText())) {
      this.articleByline = node.text().trim()
      return true
    }

    return false
  }

  /**
   * Check whether the input string could be a byline. This verifies that the input is a string, and
   * that the length is less than 100 chars.
   */
  protected open fun isValidByline(text: String): Boolean {
    val byline = text.trim()

    return (byline.isNotEmpty()) && (byline.length < 100)
  }

  protected open fun isElementWithoutContent(node: Element): Boolean {
    return node.text().isBlank() &&
      (node.children().size == 0 ||
        node.children().size == node.getElementsByTag("br").size + node.getElementsByTag("hr").size)
  }

  /**
   * Check if this node has only whitespace and a single P element Returns false if the DIV node
   * contains non-empty text nodes or if it contains no P or more than 1 element.
   */
  protected open fun hasSinglePInsideElement(element: Element): Boolean {
    // There should be exactly 1 element child which is a P:
    if (element.children().size != 1 || element.child(0).tagName() != "p") {
      return false
    }

    // And there should be no text nodes with real content
    element.childNodes().forEach { node ->
      if (node is TextNode && regEx.hasContent(node.text())) {
        return false
      }
    }

    return true
  }

  /** Determine whether element has any children block level elements. */
  protected open fun hasChildBlockElement(element: Element): Boolean {
    element.children().forEach { node ->
      if (DIV_TO_P_ELEMS.contains(node.tagName()) || hasChildBlockElement(node)) {
        return true
      }
    }

    return false
  }

  protected open fun setNodeTag(node: Element, tagName: String) {
    node.tagName(tagName)
  }

  protected open fun scoreElements(
    elementsToScore: List<Element>,
    options: ArticleGrabberOptions
  ): List<Element> {
    val candidates = ArrayList<Element>()

    elementsToScore.forEach { elementToScore ->
      if (elementToScore.parentNode() == null) {
        return@forEach
      }

      // If this paragraph is less than 25 characters, don't even count it.
      val innerText = this.getInnerText(elementToScore, regEx)
      if (innerText.length < 25) {
        return@forEach
      }

      // Exclude nodes with no ancestor.
      val ancestors = this.getNodeAncestors(elementToScore, 3)
      if (ancestors.isEmpty()) {
        return@forEach
      }

      var contentScore = 0.0

      // Add a point for the paragraph itself as a base.
      contentScore += 1

      // Add points for any commas within this paragraph.
      contentScore += innerText.split(',').size

      // For every 100 characters in this paragraph, add another point. Up to 3 points.
      contentScore += min(floor(innerText.length / 100.0), 3.0)

      // Initialize and score ancestors.
      for (level in ancestors.indices) {
        val ancestor = ancestors[level]
        if (
          ancestor.tagName().isBlank()
        ) { // with Jsoup this should never be true as we're only handling Elements
          return@forEach
        }

        if (getReadabilityObject(ancestor) == null) {
          candidates.add(ancestor)
          initializeNode(ancestor, options)
        }

        // Node score divider:
        // - parent:             1 (no division)
        // - grandparent:        2
        // - great grandparent+: ancestor level * 3
        val scoreDivider =
          when (level) {
            0 -> 1
            1 -> 2
            else -> level * 3
          }

        getReadabilityObject(ancestor)?.let { readability ->
          readability.contentScore += contentScore / scoreDivider.toDouble()
        }
      }
    }

    return candidates
  }

  /**
   * Initialize a node with the readability object. Also checks the className/id for special names
   * to add to its score.
   */
  protected open fun initializeNode(
    node: Element,
    options: ArticleGrabberOptions
  ): ReadabilityObject {
    val readability = ReadabilityObject(0.0)
    readabilityObjects[node] = readability

    when (node.tagName()) {
      "div" -> readability.contentScore += 5
      "pre",
      "td",
      "blockquote" -> readability.contentScore += 3
      "address",
      "ol",
      "ul",
      "dl",
      "dd",
      "dt",
      "li",
      "form" -> readability.contentScore -= 3
      "h1",
      "h2",
      "h3",
      "h4",
      "h5",
      "h6",
      "th" -> readability.contentScore -= 5
    }

    readability.contentScore += getClassWeight(node, options)

    return readability
  }

  /**
   * Get an elements class/id weight. Uses regular expressions to tell if this element looks good or
   * bad.
   */
  protected open fun getClassWeight(e: Element, options: ArticleGrabberOptions): Int {
    if (!options.weightClasses) {
      return 0
    }

    var weight = 0

    // Look for a special classname
    if (e.className().isNotBlank()) {
      if (regEx.isNegative(e.className())) {
        weight -= 25
      }

      if (regEx.isPositive(e.className())) {
        weight += 25
      }
    }

    // Look for a special ID
    if (e.id().isNotBlank()) {
      if (regEx.isNegative(e.id())) {
        weight -= 25
      }

      if (regEx.isPositive(e.id())) {
        weight += 25
      }
    }

    return weight
  }

  protected open fun getNodeAncestors(node: Element?, maxDepth: Int = 0): List<Element> {
    var i = 0
    val ancestors = ArrayList<Element>()
    var next = node

    while (next?.parent() != null) {
      ancestors.add(next.parent()!!)
      if (++i == maxDepth) {
        break
      }

      next = next.parent()
    }

    return ancestors
  }

  protected open fun getTopCandidate(
    page: Element,
    candidates: List<Element>,
    options: ArticleGrabberOptions
  ): Pair<Element, Boolean> {
    val topCandidates = ArrayList<Element>()

    candidates.forEach { candidate ->
      getReadabilityObject(candidate)?.let { readability ->
        // Scale the final candidates score based on link density. Good content
        // should have a relatively small link density (5% or less) and be mostly
        // unaffected by this operation.
        val candidateScore = readability.contentScore * (1 - this.getLinkDensity(candidate))
        readability.contentScore = candidateScore

        for (t in 0 until nbTopCandidates) {
          val aTopCandidate = if (topCandidates.size > t) topCandidates[t] else null
          val topCandidateReadability =
            if (aTopCandidate != null) getReadabilityObject(aTopCandidate) else null

          if (
            aTopCandidate == null ||
              (topCandidateReadability != null &&
                candidateScore > topCandidateReadability.contentScore)
          ) {
            topCandidates.add(t, candidate)

            if (topCandidates.size > this.nbTopCandidates) {
              topCandidates.removeAt(nbTopCandidates)
            }
            break
          }
        }
      }
    }

    var topCandidate = if (topCandidates.size > 0) topCandidates[0] else null
    var parentOfTopCandidate: Element?

    // If we still have no top candidate, just use the body as a last resort.
    // We also have to copy the body node, so it is something we can modify.
    if (topCandidate == null || topCandidate.tagName() == "body") {
      // Move all the page's children into topCandidate
      topCandidate = Element("div")
      // Move everything (not just elements, also text nodes etc.) into the container,
      // so we even include text directly in the body:
      ArrayList(page.childNodes()).forEach { child ->
        child.remove()
        topCandidate?.appendChild(child)
      }

      page.appendChild(topCandidate)

      this.initializeNode(topCandidate, options)

      return Pair(topCandidate, true)
    } else {
      // Find a better top candidate node if it contains (at least three) nodes which belong to
      // `topCandidates` array
      // and whose scores are quite closed with current `topCandidate` node.
      val alternativeCandidateAncestors = ArrayList<List<Element>>()

      getReadabilityObject(topCandidate)?.let { topCandidateReadability ->
        topCandidates
          .filter { it != topCandidate }
          .forEach { otherTopCandidate ->
            if (
              ((getReadabilityObject(otherTopCandidate)?.contentScore
                ?: 0.0) / topCandidateReadability.contentScore) >= 0.75
            ) {
              alternativeCandidateAncestors.add(this.getNodeAncestors(otherTopCandidate))
            }
          }
      }

      if (alternativeCandidateAncestors.size >= MINIMUM_TOP_CANDIDATES) {
        parentOfTopCandidate = topCandidate.parent()

        while (parentOfTopCandidate != null && parentOfTopCandidate.tagName() !== "body") {
          var listsContainingThisAncestor = 0
          var ancestorIndex = 0
          while (
            ancestorIndex < alternativeCandidateAncestors.size &&
              listsContainingThisAncestor < MINIMUM_TOP_CANDIDATES
          ) {
            if (alternativeCandidateAncestors[ancestorIndex].contains(parentOfTopCandidate)) {
              listsContainingThisAncestor++
            }
            ancestorIndex++
          }

          if (listsContainingThisAncestor >= MINIMUM_TOP_CANDIDATES) {
            topCandidate = parentOfTopCandidate
            break
          }
          parentOfTopCandidate = parentOfTopCandidate.parent()
        }
      }

      topCandidate = topCandidate!!
      if (getReadabilityObject(topCandidate) == null) {
        this.initializeNode(topCandidate, options)
      }

      // Because of our bonus system, parents of candidates might have scores
      // themselves. They get half of the node. There won't be nodes with higher
      // scores than our topCandidate, but if we see the score going *up* in the first
      // few steps up the tree, that's a decent sign that there might be more content
      // lurking in other places that we want to unify in. The sibling stuff
      // below does some of that - but only if we've looked high enough up the DOM
      // tree.
      parentOfTopCandidate = topCandidate.parent()
      var lastScore = getReadabilityObject(topCandidate)?.contentScore ?: 0.0
      // The scores shouldn't get too low.
      val scoreThreshold = lastScore / 3.0

      while (parentOfTopCandidate != null && parentOfTopCandidate.tagName() != "body") {
        val parentOfTopCandidateReadability = getReadabilityObject(parentOfTopCandidate)
        if (parentOfTopCandidateReadability == null) {
          parentOfTopCandidate = parentOfTopCandidate.parent()
          continue
        }

        val parentScore = parentOfTopCandidateReadability.contentScore
        if (parentScore < scoreThreshold) {
          break
        }
        if (parentScore > lastScore) {
          // Alright! We found a better parent to use.
          topCandidate = parentOfTopCandidate
          break
        }

        lastScore = parentOfTopCandidateReadability.contentScore
        parentOfTopCandidate = parentOfTopCandidate.parent()
      }

      // If the top candidate is the only child, use parent instead. This will help sibling
      // joining logic when adjacent content is actually located in parent's sibling node.
      topCandidate = topCandidate!!
      parentOfTopCandidate = topCandidate.parent()
      while (
        parentOfTopCandidate != null &&
          parentOfTopCandidate.tagName() != "body" &&
          parentOfTopCandidate.children().size == 1
      ) {
        topCandidate = parentOfTopCandidate
        parentOfTopCandidate = topCandidate.parent()
      }

      topCandidate = topCandidate!!
      if (getReadabilityObject(topCandidate) == null) {
        this.initializeNode(topCandidate, options)
      }

      return Pair(topCandidate, false)
    }
  }

  /**
   * Get the density of links as a percentage of the content This is the amount of text that is
   * inside a link divided by the total text in the node.
   */
  protected open fun getLinkDensity(element: Element): Double {
    val textLength = this.getInnerText(element, regEx).length
    if (textLength == 0) {
      return 0.0
    }

    var linkLength = 0

    // XXX implement _reduceNodeList?
    element.getElementsByTag("a").forEach { linkNode ->
      linkLength += this.getInnerText(linkNode, regEx).length
    }

    return linkLength / textLength.toDouble()
  }

  protected open fun createArticleContent(
    doc: Document,
    topCandidate: Element,
    isPaging: Boolean
  ): Element {
    val articleContent = doc.createElement("div")
    if (isPaging) {
      articleContent.attr("id", "readability-content")
    }

    val topCandidateReadability = getReadabilityObject(topCandidate) ?: return articleContent

    val siblingScoreThreshold = max(10.0, topCandidateReadability.contentScore * 0.2)
    // Keep potential top candidate's parent node to try to get text direction of it later.
    val parentOfTopCandidate =
      topCandidate.parent() // parentOfTopCandidate may is null, see issue #12
    val siblings = parentOfTopCandidate?.children() ?: Elements()

    ArrayList(siblings).forEach { sibling
      -> // make a copy of children as the may get modified below -> we can get rid of s -= 1 sl -=
      // 1 compared to original source
      var append = false

      val siblingReadability = getReadabilityObject(sibling)

      if (sibling == topCandidate) {
        append = true
      } else {
        var contentBonus = 0.0

        // Give a bonus if sibling nodes and top candidates have the example same classname
        if (sibling.className() == topCandidate.className() && topCandidate.className() !== "")
          contentBonus += topCandidateReadability.contentScore * 0.2

        if (
          siblingReadability != null &&
            ((siblingReadability.contentScore + contentBonus) >= siblingScoreThreshold)
        ) {
          append = true
        } else if (shouldKeepSibling(sibling)) {
          val linkDensity = this.getLinkDensity(sibling)
          val nodeContent = this.getInnerText(sibling, regEx)
          val nodeLength = nodeContent.length

          if (nodeLength > 80 && linkDensity < 0.25) {
            append = true
          } else if (
            nodeLength in 1..79 && linkDensity == 0.0 && nodeContent.contains("\\.( |$)".toRegex())
          ) {
            append = true
          }
        }
      }

      if (append) {
        if (!ALTER_TO_DIV_EXCEPTIONS.contains(sibling.tagName())) {
          // We have a node that isn't a common block level element, like a form or td tag.
          // Turn it into a div, so it doesn't get filtered out later by accident.
          setNodeTag(sibling, "div")
        }

        articleContent.appendChild(sibling)
      }
    }

    return articleContent
  }

  protected open fun shouldKeepSibling(sibling: Element): Boolean {
    return sibling.tagName() == "p"
  }

  /**
   * Prepare the article node for display. Clean out any inline styles, iframes, forms, strip
   * extraneous <p> tags, etc.
   */
  protected open fun prepArticle(
    articleContent: Element,
    options: ArticleGrabberOptions,
    metadata: ArticleMetadata
  ) {
    this.cleanStyles(articleContent)

    // Check for data tables before we continue, to avoid removing items in
    // those tables, which will often be isolated even though they're
    // visually linked to other content-ful elements (text, images, etc.).
    markDataTables(articleContent)

    // Clean out junk from the article content
    this.cleanConditionally(articleContent, "form", options)
    this.cleanConditionally(articleContent, "fieldset", options)
    this.clean(articleContent, "object")
    this.clean(articleContent, "embed")
    this.clean(articleContent, "footer")
    this.clean(articleContent, "link")

    // Clean out elements have "share" in their id/class combinations from final top candidates,
    // which means we don't remove the top candidates even they have "share".
    val shareRegex = "share".toRegex()
    articleContent.children().forEach { topCandidate ->
      cleanMatchedNodes(topCandidate, shareRegex)
    }

    // If there is only one h2 and its text content substantially equals article title,
    // they are probably using it as a header and not a subheader,
    // so remove it since we already extract the title separately.
    val h2 = articleContent.getElementsByTag("h2")
    if (h2.size == 1) {
      metadata.title?.let { articleTitle ->
        if (articleTitle.isNotEmpty()) {
          val lengthSimilarRate =
            (h2[0].text().length - articleTitle.length) / articleTitle.length.toFloat()
          if (abs(lengthSimilarRate) < 0.5) {
            val titlesMatch =
              if (lengthSimilarRate > 0) {
                h2[0].text().contains(articleTitle)
              } else {
                articleTitle.contains(h2[0].text())
              }

            if (titlesMatch) {
              this.clean(articleContent, "h2")
            }
          }
        }
      }
    }

    this.clean(articleContent, "iframe")
    this.clean(articleContent, "input")
    this.clean(articleContent, "textarea")
    this.clean(articleContent, "select")
    this.clean(articleContent, "button")
    this.cleanHeaders(articleContent, options)

    // Do these last as the previous stuff may have removed junk
    // that will affect these
    this.cleanConditionally(articleContent, "table", options)
    this.cleanConditionally(articleContent, "ul", options)
    this.cleanConditionally(articleContent, "div", options)

    // Remove extra paragraphs
    removeNodes(articleContent, "p") { paragraph ->
      val imgCount = paragraph.getElementsByTag("img").size
      val embedCount = paragraph.getElementsByTag("embed").size
      val objectCount = paragraph.getElementsByTag("object").size
      // At this point, nasty iframes have been removed, only remain embedded video ones.
      val iframeCount = paragraph.getElementsByTag("iframe").size
      val totalCount = imgCount + embedCount + objectCount + iframeCount

      return@removeNodes totalCount == 0 &&
        getInnerText(paragraph, normalizeSpaces = false).isEmpty()
    }

    articleContent.select("br").forEach { br ->
      val next = nextElement(br.nextSibling(), regEx)
      if (next != null && next.tagName() == "p") {
        br.remove()
      }
    }
  }

  /**
   * Remove the style attribute on every e and under.
   *
   * TODO: Test if getElementsByTagName(*) is faster.
   */
  protected open fun cleanStyles(e: Element) {
    if (e.tagName() == "svg") {
      return
    }

    if (e.className() !== "readability-styled") {
      // Remove `style` and deprecated presentational attributes
      PRESENTATIONAL_ATTRIBUTES.forEach { attributeName -> e.removeAttr(attributeName) }

      if (DEPRECATED_SIZE_ATTRIBUTE_ELEMS.contains(e.tagName())) {
        e.removeAttr("width")
        e.removeAttr("height")
      }
    }

    e.children().forEach { child -> cleanStyles(child) }
  }

  protected open fun markDataTables(root: Element) {
    root.getElementsByTag("table").forEach outer@{ table ->
      val role = table.attr("role")
      if (role == "presentation") {
        setReadabilityDataTable(table, false)
        return@outer
      }
      val datatable = table.attr("datatable")
      if (datatable == "0") {
        setReadabilityDataTable(table, false)
        return@outer
      }
      val summary = table.attr("summary")
      if (summary.isNotBlank()) {
        setReadabilityDataTable(table, true)
        return@outer
      }

      val caption = table.getElementsByTag("caption")
      if (caption.size > 0 && caption[0].childNodeSize() > 0) {
        setReadabilityDataTable(table, true)
        return@outer
      }

      // If the table has a descendant with any of these tags, consider a data table: (move to
      // DATA_TABLE_DESCENDANTS to make code a more readable and a bit faster)
      DATA_TABLE_DESCENDANTS.forEach { tag ->
        if (table.getElementsByTag(tag).size > 0) {
          setReadabilityDataTable(table, true)
          return@outer
        }
      }

      // Nested tables indicate a layout table:
      if (table.getElementsByTag("table").size > 0) {
        setReadabilityDataTable(table, false)
        return@outer
      }

      val sizeInfo = getRowAndColumnCount(table)
      if (sizeInfo.first >= 10 || sizeInfo.second > 4) {
        setReadabilityDataTable(table, true)
        return@outer
      }

      // Now just go by size entirely:
      setReadabilityDataTable(table, sizeInfo.first * sizeInfo.second > 10)
    }
  }

  /** Return an object indicating how many rows and columns this table has. */
  protected open fun getRowAndColumnCount(table: Element): Pair<Int, Int> {
    var rows = 0
    var columns = 0

    val trs = table.getElementsByTag("tr")
    trs.forEach { tr ->
      rows +=
        try {
          tr.attr("rowspan").toInt()
        } catch (ignored: Exception) {
          1
        }

      // Now look for column-related info
      var columnsInThisRow = 0
      tr.getElementsByTag("td").forEach { cell ->
        columnsInThisRow +=
          try {
            cell.attr("colspan").toInt()
          } catch (ignored: Exception) {
            1
          }
      }

      columns = max(columns, columnsInThisRow)
    }

    return Pair(rows, columns)
  }

  protected open fun cleanConditionally(e: Element, tag: String, options: ArticleGrabberOptions) {
    if (!options.cleanConditionally) return

    val isList = tag == "ul" || tag == "ol"

    // Gather counts for other typical elements embedded within.
    // Traverse backwards so we can remove nodes at the same time
    // without effecting the traversal.
    //
    // TODO: Consider taking into account original contentScore here.
    removeNodes(e, tag) { node ->
      // First check if we're in a data table, in which case don't remove us.
      val isDataTable: (Element) -> Boolean = { element -> getReadabilityDataTable(element) }

      if (hasAncestorTag(node, "table", -1, isDataTable)) {
        return@removeNodes false
      }

      val weight = getClassWeight(node, options)
      val contentScore = 0

      if (weight + contentScore < 0) {
        return@removeNodes true
      }

      if (getCharCount(node, ',') < 10) {
        // If there are not very many commas, and the number of
        // non-paragraph elements is more than paragraphs or other
        // ominous signs, remove the element.
        val p = node.getElementsByTag("p").size
        val img = node.getElementsByTag("img").size
        val li = node.getElementsByTag("li").size - 100
        val input = node.getElementsByTag("input").size

        var embedCount = 0
        node.getElementsByTag("embed").forEach {
          if (!regEx.isVideo(it.attr("src"))) {
            embedCount += 1
          }
        }

        val linkDensity = getLinkDensity(node)
        val contentLength = getInnerText(node, regEx).length

        val haveToRemove =
          (img > 1 && p / img.toFloat() < 0.5 && !hasAncestorTag(node, "figure")) ||
            (!isList && li > p) ||
            (input > floor(p / 3.0)) ||
            (!isList && contentLength < 25 && img == 0 && !hasAncestorTag(node, "figure")) ||
            (!isList && weight < 25 && linkDensity > 0.2) ||
            (weight >= 25 && linkDensity > 0.5) ||
            ((embedCount == 1 && contentLength < 75) || embedCount > 1)
        return@removeNodes haveToRemove
      }

      return@removeNodes false
    }
  }

  /** Check if a given node has one of its ancestor tag name matching the provided one */
  protected open fun hasAncestorTag(
    node: Element,
    tagName: String,
    maxDepth: Int = 3,
    filterFn: ((Element) -> Boolean)? = null
  ): Boolean {
    val tagNameLowerCase = tagName.lowercase()
    var parent = node
    var depth = 0

    while (parent.parent() != null) {
      if (maxDepth in 1 until depth) {
        return false
      }

      if (
        parent.parent()!!.tagName() == tagNameLowerCase &&
          (filterFn == null || filterFn(parent.parent()!!))
      ) {
        return true
      }

      parent = parent.parent()!!
      depth++
    }

    return false
  }

  /** Get the number of times a string s appears in the node e. */
  protected open fun getCharCount(node: Element, c: Char = ','): Int {
    return getInnerText(node, regEx).split(c).size - 1
  }

  /**
   * Clean a node of all elements of type "tag". (Unless it's a YouTube/vimeo video. People love
   * movies.)
   */
  protected open fun clean(e: Element, tag: String) {
    val isEmbed = EMBEDDED_NODES.contains(tag)

    removeNodes(e, tag) { element ->
      // Allow YouTube and vimeo videos through as people usually want to see those.
      if (isEmbed) {
        val attributeValues = element.attributes().joinToString("|") { it.value }

        // First, check the elements attributes to see if any of them contain YouTube or vimeo
        if (regEx.isVideo(attributeValues)) {
          return@removeNodes false
        }

        // Then check the elements inside this element for the same.
        if (regEx.isVideo(element.html())) {
          return@removeNodes false
        }
      }

      return@removeNodes true
    }
  }

  /** Clean out elements whose id/class combinations match specific string. */
  protected open fun cleanMatchedNodes(e: Element, regex: Regex) {
    val endOfSearchMarkerNode = getNextNode(e, true)
    var next = getNextNode(e)

    while (next != null && next != endOfSearchMarkerNode) {
      next =
        if (regex.containsMatchIn(next.className() + " " + next.id())) {
          removeAndGetNext(next, regex.pattern)
        } else {
          getNextNode(next)
        }
    }
  }

  /** Clean out spurious headers from an Element. Checks things like classnames and link density. */
  protected open fun cleanHeaders(e: Element, options: ArticleGrabberOptions) {
    arrayOf("h1", "h2").forEach {
      removeNodes(e, it) { header -> getClassWeight(header, options) < 0 }
    }
  }

  protected open fun removeAndGetNext(node: Element, reason: String = ""): Element? {
    val nextNode = this.getNextNode(node, true)
    printAndRemove(node, reason)
    return nextNode
  }

  /**
   * Traverse the DOM from node to node, starting at the node passed in. Pass true for the second
   * parameter to indicate this node itself (and its kids) are going away, and we want the next node
   * over.
   *
   * Calling this in a loop will traverse the DOM depth-first.
   */
  protected open fun getNextNode(node: Element, ignoreSelfAndKids: Boolean = false): Element? {
    // First check for kids if those aren't being ignored
    if (!ignoreSelfAndKids && node.children().size > 0) {
      return node.child(0)
    }

    // Then for siblings...
    node.nextElementSibling()?.let {
      return it
    }

    // And finally, move up the parent chain *and* find a sibling
    // (because this is depth-first traversal, we will have already
    // seen the parent nodes themselves).
    var parent = node.parent()
    while (parent != null && parent.nextElementSibling() == null) {
      parent = parent.parent()
    }

    return parent?.nextElementSibling()
  }

  protected open fun getTextDirection(topCandidate: Element, doc: Document) {
    val ancestors = mutableSetOf(topCandidate.parent(), topCandidate)
    ancestors.addAll(getNodeAncestors(topCandidate.parent()))
    ancestors.add(doc.body())
    ancestors.add(doc.selectFirst("html")) // needed as dir is often set on html tag

    ancestors.filterNotNull().forEach { ancestor ->
      val articleDir = ancestor.attr("dir")
      if (articleDir.isNotBlank()) {
        this.articleDir = articleDir
        return
      }
    }
  }

  protected open fun getReadabilityObject(element: Element): ReadabilityObject? {
    return readabilityObjects[element]
  }

  protected open fun getReadabilityDataTable(table: Element): Boolean {
    return this.readabilityDataTable[table] ?: false
  }

  protected open fun setReadabilityDataTable(table: Element, readabilityDataTable: Boolean) {
    this.readabilityDataTable[table] = readabilityDataTable
  }
}
