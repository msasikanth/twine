/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package dev.sasikanth.rss.reader.util

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Entities
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import com.fleeksoft.ksoup.parser.Tag
import com.fleeksoft.ksoup.select.NodeTraversor
import com.fleeksoft.ksoup.select.NodeVisitor

actual val canBlurImage: Boolean = true

actual fun String.decodeHTMLString(): String {
  if (isEmpty() || (!contains('&') && !contains('<'))) return this
  if (!contains('<')) return decodeHtmlEntities()

  val preprocessed =
    preprocessForKsoup() ?: return if (contains('&')) decodeHtmlEntities() else this
  val doc = Ksoup.parseBodyFragment(preprocessed)
  return extractTextFromElement(doc.body())
}

private fun String.preprocessForKsoup(): String? {
  val length = this.length
  val hasTableElement = containsHtmlTag("table")
  val sb = StringBuilder(length + 16)
  var hasHtmlMarkup = false
  var i = 0

  while (i < length) {
    val ch = this[i]
    if (ch == '<') {
      if (startsWith("<!--", i)) {
        val commentEnd = indexOf("-->", i + 4)
        if (commentEnd != -1) {
          hasHtmlMarkup = true
          i = commentEnd + 3
          continue
        }
      }

      if (regionMatches(i, "<![CDATA[", 0, 9, ignoreCase = true)) {
        val cdataEnd = indexOf("]]>", i + 9)
        if (cdataEnd != -1) {
          hasHtmlMarkup = true
          sb.appendEscapedPlainSegment(this, i + 9, cdataEnd)
          i = cdataEnd + 3
          continue
        }
      }

      if (regionMatches(i, "<!doctype", 0, 9, ignoreCase = true)) {
        val docEnd = indexOf('>', i + 9)
        if (docEnd != -1) {
          hasHtmlMarkup = true
          i = docEnd + 1
          continue
        }
      }

      if (i + 1 < length && this[i + 1] == '?') {
        val piEnd = indexOf('>', i + 2)
        if (piEnd != -1) {
          hasHtmlMarkup = true
          i = piEnd + 1
          continue
        }
      }

      val matchedTag = matchValidHtmlTag(i)
      if (matchedTag != null) {
        hasHtmlMarkup = true
        val lowerName = matchedTag.lowerName
        if (
          (lowerName == "script" || lowerName == "style") &&
            !matchedTag.isClosing &&
            !matchedTag.isSelfClosing
        ) {
          i = skipRawTextElementBody(matchedTag.endIndex + 1, lowerName)
          continue
        }

        if (!hasTableElement && (lowerName == "td" || lowerName == "th")) {
          if (matchedTag.isClosing) {
            sb.append("</span>")
          } else if (matchedTag.isSelfClosing) {
            sb.append("<span ").append(SYNTHETIC_CELL_ATTR).append("=\"\"></span>")
          } else {
            sb.append("<span ").append(SYNTHETIC_CELL_ATTR).append("=\"\">")
          }
        } else if (!hasTableElement && lowerName == "tr") {
          if (matchedTag.isClosing) {
            sb.append("</div>")
          } else if (matchedTag.isSelfClosing) {
            sb.append("<div ").append(SYNTHETIC_ROW_ATTR).append("=\"\"></div>")
          } else {
            sb.append("<div ").append(SYNTHETIC_ROW_ATTR).append("=\"\">")
          }
        } else {
          sb.append(this, i, matchedTag.endIndex + 1)
        }

        i = matchedTag.endIndex + 1
        continue
      }

      sb.append("&lt;")
      i++
      continue
    }

    if (ch == '&') {
      val nextIndex = sb.appendEntityForKsoup(this, i)
      i = nextIndex
      continue
    }

    sb.append(ch)
    i++
  }

  return if (hasHtmlMarkup) sb.toString() else null
}

private fun StringBuilder.appendEscapedPlainSegment(source: String, start: Int, end: Int) {
  var i = start
  while (i < end) {
    when (val ch = source[i]) {
      '<' -> {
        append("&lt;")
        i++
      }
      '&' -> {
        i = appendEntityForKsoup(source, i, end)
      }
      else -> {
        append(ch)
        i++
      }
    }
  }
}

private fun StringBuilder.appendEntityForKsoup(
  source: String,
  ampIndex: Int,
  limit: Int = source.length,
): Int {
  val semiIndex = source.findEntitySemicolon(ampIndex, limit)
  if (semiIndex != -1) {
    val entity = source.substring(ampIndex + 1, semiIndex)
    val decoded = resolveEntity(entity)
    if (decoded != null) {
      for (c in decoded) {
        when (c) {
          '<' -> append("&lt;")
          '&' -> append("&amp;")
          else -> append(c)
        }
      }
      return semiIndex + 1
    }
  }
  append("&amp;")
  return ampIndex + 1
}

private fun String.containsHtmlTag(tagName: String): Boolean {
  var idx = 0
  while (idx < length) {
    val found = indexOf('<', idx)
    if (found == -1 || found + tagName.length + 1 >= length) return false
    if (regionMatches(found + 1, tagName, 0, tagName.length, ignoreCase = true)) {
      val after = this[found + 1 + tagName.length]
      if (after == '>' || after == '/' || after.isWhitespace()) return true
    }
    idx = found + 1
  }
  return false
}

private data class MatchedHtmlTag(
  val lowerName: String,
  val endIndex: Int,
  val isClosing: Boolean,
  val isSelfClosing: Boolean,
)

private fun String.matchValidHtmlTag(startIndex: Int): MatchedHtmlTag? {
  if (startIndex + 1 >= length) return null
  var p = startIndex + 1
  val isClosing = this[p] == '/'
  if (isClosing) p++

  if (p >= length || !this[p].isAsciiLetter()) return null
  val nameStart = p
  p++
  while (p < length && (this[p].isAsciiLetterOrDigit() || this[p] == '-' || this[p] == ':')) {
    p++
  }

  val lowerName = substring(nameStart, p).lowercase()
  if (!isRecognizedHtmlTag(lowerName)) return null

  if (isClosing) {
    while (p < length && this[p].isHtmlWhitespace()) p++
    return if (p < length && this[p] == '>') {
      MatchedHtmlTag(lowerName = lowerName, endIndex = p, isClosing = true, isSelfClosing = false)
    } else {
      null
    }
  }

  while (p < length) {
    val wsStart = p
    while (p < length && this[p].isHtmlWhitespace()) p++
    val hadWhitespace = p > wsStart
    if (p >= length) return null

    val c = this[p]
    if (c == '>') {
      return MatchedHtmlTag(
        lowerName = lowerName,
        endIndex = p,
        isClosing = false,
        isSelfClosing = false,
      )
    }
    if (c == '/' && p + 1 < length && this[p + 1] == '>') {
      return MatchedHtmlTag(
        lowerName = lowerName,
        endIndex = p + 1,
        isClosing = false,
        isSelfClosing = true,
      )
    }

    if (!hadWhitespace) return null
    if (!c.isAttrNameStart()) return null

    val attrStart = p
    p++
    while (p < length && this[p].isAttrNamePart()) p++
    val attrName = substring(attrStart, p).lowercase()

    while (p < length && this[p].isHtmlWhitespace()) p++
    if (p >= length) return null

    if (this[p] == '=') {
      p++
      while (p < length && this[p].isHtmlWhitespace()) p++
      if (p >= length) return null

      val valChar = this[p]
      if (valChar == '"' || valChar == '\'') {
        val closeQuote = indexOf(valChar, p + 1)
        if (closeQuote == -1) return null
        p = closeQuote + 1
      } else {
        val valStart = p
        while (
          p < length && !this[p].isHtmlWhitespace() && !this[p].isForbiddenUnquotedAttrChar()
        ) {
          p++
        }
        if (p == valStart) return null
      }
    } else {
      val next = this[p]
      if (next != '>' && next != '/' && !next.isHtmlWhitespace()) return null
      if (!isAllowedValuelessAttribute(attrName)) return null
    }
  }

  return null
}

private fun isRecognizedHtmlTag(lowerName: String): Boolean {
  if (Tag.isKnownTag(lowerName)) return true
  val dashIndex = lowerName.indexOf('-')
  return dashIndex > 0 && dashIndex < lowerName.length - 1
}

private fun isAllowedValuelessAttribute(attrName: String): Boolean {
  if (attrName in BOOLEAN_HTML_ATTRIBUTES) return true
  return attrName.contains('-') || attrName.startsWith('_')
}

private fun String.skipRawTextElementBody(contentStart: Int, lowerTagName: String): Int {
  var searchIndex = contentStart
  val closePrefix = "</$lowerTagName"
  while (searchIndex < length) {
    val closeStart = indexOf(closePrefix, searchIndex, ignoreCase = true)
    if (closeStart == -1) return length
    val afterName = closeStart + closePrefix.length
    if (afterName < length) {
      val afterChar = this[afterName]
      if (afterChar == '>' || afterChar.isHtmlWhitespace()) {
        val closeEnd = indexOf('>', afterName)
        return if (closeEnd != -1) closeEnd + 1 else length
      }
    }
    searchIndex = afterName
  }
  return length
}

private fun extractTextFromElement(root: Element): String {
  val sb = StringBuilder()
  val visitor =
    object : NodeVisitor {
      private var ignoredDepth = 0
      private var preDepth = 0
      private var pendingBlockBreak = false
      private var pendingCellSpace = false

      override fun head(node: Node, depth: Int) {
        if (ignoredDepth > 0) {
          if (node is Element && node.normalName() in IGNORED_TAG_NAMES) {
            ignoredDepth++
          }
          return
        }

        when (node) {
          is Element -> {
            val tag = node.normalName()
            if (tag in IGNORED_TAG_NAMES) {
              ignoredDepth = 1
              return
            }
            if (tag == "pre") {
              preDepth++
            }
            when {
              tag == "br" -> {
                sb.trimTrailingAsciiSpace()
                sb.append('\n')
                pendingBlockBreak = false
                pendingCellSpace = false
              }
              tag == "td" || tag == "th" || node.hasAttr(SYNTHETIC_CELL_ATTR) -> {
                if (
                  !pendingBlockBreak && sb.isNotEmpty() && sb.last() != '\n' && sb.last() != ' '
                ) {
                  pendingCellSpace = true
                }
              }
              tag in BLOCK_BREAK_TAG_NAMES || node.hasAttr(SYNTHETIC_ROW_ATTR) -> {
                if (sb.isNotEmpty() && sb.last() != '\n') {
                  sb.trimTrailingAsciiSpace()
                  if (sb.isNotEmpty() && sb.last() != '\n') {
                    pendingBlockBreak = true
                    pendingCellSpace = false
                  }
                }
              }
            }
          }
          is TextNode -> {
            val wholeText = node.getWholeText()
            if (wholeText.isEmpty()) return

            if (preDepth > 0) {
              flushPendingSeparator()
              sb.append(wholeText)
            } else {
              appendCollapsedText(wholeText)
            }
          }
        }
      }

      override fun tail(node: Node, depth: Int) {
        if (node !is Element) return
        val tag = node.normalName()
        if (ignoredDepth > 0) {
          if (tag in IGNORED_TAG_NAMES) {
            ignoredDepth--
          }
          return
        }

        if (tag == "pre" && preDepth > 0) {
          preDepth--
        }

        when {
          tag == "td" || tag == "th" || node.hasAttr(SYNTHETIC_CELL_ATTR) -> {
            sb.trimTrailingAsciiSpace()
            if (!pendingBlockBreak && sb.isNotEmpty() && sb.last() != '\n' && sb.last() != ' ') {
              pendingCellSpace = true
            }
          }
          tag in BLOCK_BREAK_TAG_NAMES || node.hasAttr(SYNTHETIC_ROW_ATTR) -> {
            sb.trimTrailingAsciiSpace()
            if (sb.isNotEmpty() && sb.last() != '\n') {
              pendingBlockBreak = true
              pendingCellSpace = false
            }
          }
        }
      }

      private fun flushPendingSeparator() {
        if (pendingBlockBreak) {
          sb.trimTrailingAsciiSpace()
          if (sb.isNotEmpty() && sb.last() != '\n') {
            sb.append('\n')
          }
          pendingBlockBreak = false
          pendingCellSpace = false
        } else if (pendingCellSpace) {
          if (sb.isNotEmpty() && sb.last() != '\n' && sb.last() != ' ') {
            sb.append(' ')
          }
          pendingCellSpace = false
        }
      }

      private fun appendCollapsedText(text: String) {
        var i = 0
        val len = text.length
        while (i < len) {
          val ch = text[i]
          if (ch.isHtmlWhitespace()) {
            while (i + 1 < len && text[i + 1].isHtmlWhitespace()) i++
            val atLineStart = sb.isEmpty() || sb.last() == '\n' || pendingBlockBreak
            if (!atLineStart && !pendingCellSpace && sb.last() != ' ') {
              sb.append(' ')
            }
          } else {
            flushPendingSeparator()
            sb.append(ch)
          }
          i++
        }
      }
    }

  NodeTraversor.traverse(visitor, root)
  sb.trimTrailingAsciiSpace()
  return sb.toString()
}

private fun StringBuilder.trimTrailingAsciiSpace() {
  while (isNotEmpty() && this[length - 1] == ' ') {
    deleteAt(length - 1)
  }
}

private fun String.decodeHtmlEntities(): String {
  val length = this.length
  var sb: StringBuilder? = null
  var lastCopied = 0
  var i = 0

  while (i < length) {
    if (this[i] == '&') {
      val semiIndex = findEntitySemicolon(i, length)
      if (semiIndex != -1) {
        val entity = substring(i + 1, semiIndex)
        val decoded = resolveEntity(entity)
        if (decoded != null) {
          val builder = sb ?: StringBuilder(length).also { sb = it }
          builder.append(this, lastCopied, i)
          builder.append(decoded)
          i = semiIndex + 1
          lastCopied = i
          continue
        }
      }
    }
    i++
  }

  val builder = sb ?: return this
  if (lastCopied < length) {
    builder.append(this, lastCopied, length)
  }
  return builder.toString()
}

private fun String.findEntitySemicolon(ampIndex: Int, limit: Int): Int {
  val maxScan = minOf(ampIndex + MAX_ENTITY_LENGTH, limit - 1)
  var k = ampIndex + 1
  while (k <= maxScan) {
    val c = this[k]
    if (c == ';') return if (k > ampIndex + 1) k else -1
    if (c == '&' || c == '<' || c.isWhitespace()) return -1
    k++
  }
  return -1
}

private fun resolveEntity(entity: String): String? {
  if (entity.isEmpty()) return null
  if (entity[0] == '#') {
    val isHex = entity.length > 2 && (entity[1] == 'x' || entity[1] == 'X')
    val digitsStart = if (isHex) 2 else 1
    if (digitsStart >= entity.length) return null

    var acc = 0L
    val radix = if (isHex) 16 else 10
    for (idx in digitsStart until entity.length) {
      val digit = entity[idx].hexDigitValue()
      if (digit < 0 || digit >= radix) return null
      acc = (acc * radix + digit).coerceAtMost(0x110000L)
    }

    val mapped = mapHtmlCodePoint(acc.toInt())
    return codePointToString(mapped)
  }

  resolveNamedEntity(entity)?.let {
    return it
  }

  val lower = entity.lowercase()
  if (lower == "apos") return "'"
  if (Entities.isBaseNamedEntity(lower)) {
    resolveNamedEntity(lower)?.let {
      return it
    }
  }
  return null
}

private fun resolveNamedEntity(name: String): String? {
  val codepoints = IntArray(2)
  return when (Entities.codepointsForName(name, codepoints)) {
    1 -> codePointToString(codepoints[0])
    2 -> codePointToString(codepoints[0]) + codePointToString(codepoints[1])
    else -> null
  }
}

private fun codePointToString(codePoint: Int): String {
  return if (codePoint <= 0xFFFF) {
    codePoint.toChar().toString()
  } else {
    val offset = codePoint - 0x10000
    val high = ((offset ushr 10) + 0xD800).toChar()
    val low = ((offset and 0x3FF) + 0xDC00).toChar()
    charArrayOf(high, low).concatToString()
  }
}

private fun mapHtmlCodePoint(codePoint: Int): Int {
  if (codePoint == 0 || codePoint > 0x10FFFF || codePoint in 0xD800..0xDFFF) {
    return 0xFFFD
  }
  if (codePoint in 0x80..0x9F) {
    val win1252 = WINDOWS_1252_EXTENDED[codePoint - 0x80]
    if (win1252 != 0) return win1252
  }
  return codePoint
}

private fun Char.hexDigitValue(): Int =
  when (this) {
    in '0'..'9' -> this - '0'
    in 'a'..'f' -> this - 'a' + 10
    in 'A'..'F' -> this - 'A' + 10
    else -> -1
  }

private fun Char.isAsciiLetter(): Boolean = (this in 'a'..'z') || (this in 'A'..'Z')

private fun Char.isAsciiLetterOrDigit(): Boolean = isAsciiLetter() || (this in '0'..'9')

private fun Char.isHtmlWhitespace(): Boolean =
  this == ' ' || this == '\t' || this == '\n' || this == '\r' || this == '\u000c'

private fun Char.isAttrNameStart(): Boolean = isAsciiLetter() || this == '_' || this == ':'

private fun Char.isAttrNamePart(): Boolean =
  isAsciiLetterOrDigit() || this == '_' || this == ':' || this == '.' || this == '-'

private fun Char.isForbiddenUnquotedAttrChar(): Boolean =
  this == '"' || this == '\'' || this == '=' || this == '<' || this == '>' || this == '`'

private const val MAX_ENTITY_LENGTH = 64
private const val SYNTHETIC_CELL_ATTR = "data-twine-cell"
private const val SYNTHETIC_ROW_ATTR = "data-twine-row"

private val IGNORED_TAG_NAMES = setOf("script", "style", "template", "head")

private val BLOCK_BREAK_TAG_NAMES =
  setOf(
    "address",
    "article",
    "aside",
    "blockquote",
    "caption",
    "center",
    "dd",
    "details",
    "div",
    "dl",
    "dt",
    "fieldset",
    "figcaption",
    "figure",
    "footer",
    "h1",
    "h2",
    "h3",
    "h4",
    "h5",
    "h6",
    "header",
    "hgroup",
    "hr",
    "li",
    "listing",
    "main",
    "nav",
    "ol",
    "p",
    "plaintext",
    "pre",
    "section",
    "summary",
    "table",
    "tbody",
    "tfoot",
    "thead",
    "tr",
    "ul",
  )

private val BOOLEAN_HTML_ATTRIBUTES =
  setOf(
    "allowfullscreen",
    "async",
    "autofocus",
    "autoplay",
    "checked",
    "compact",
    "controls",
    "declare",
    "default",
    "defer",
    "disabled",
    "formnovalidate",
    "hidden",
    "inert",
    "ismap",
    "itemscope",
    "loop",
    "multiple",
    "muted",
    "nohref",
    "nomodule",
    "noresize",
    "noshade",
    "novalidate",
    "nowrap",
    "open",
    "playsinline",
    "readonly",
    "required",
    "reversed",
    "scoped",
    "seamless",
    "selected",
    "truespeed",
  )

private val WINDOWS_1252_EXTENDED =
  intArrayOf(
    0x20AC,
    0x0000,
    0x201A,
    0x0192,
    0x201E,
    0x2026,
    0x2020,
    0x2021,
    0x02C6,
    0x2030,
    0x0160,
    0x2039,
    0x0152,
    0x0000,
    0x017D,
    0x0000,
    0x0000,
    0x2018,
    0x2019,
    0x201C,
    0x201D,
    0x2022,
    0x2013,
    0x2014,
    0x02DC,
    0x2122,
    0x0161,
    0x203A,
    0x0153,
    0x0000,
    0x017E,
    0x0178,
  )
