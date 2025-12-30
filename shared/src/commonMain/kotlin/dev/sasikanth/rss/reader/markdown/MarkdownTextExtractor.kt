/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.markdown

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

object MarkdownTextExtractor {

  fun extract(node: ASTNode, content: String): String {
    val builder = StringBuilder()
    extractTextRecursive(node, content, builder)
    return builder.toString().replace(Regex("\\s+"), " ").trim()
  }

  private fun extractTextRecursive(node: ASTNode, content: String, builder: StringBuilder) {
    val type = node.type

    // Skip technical/formatting/non-speech tokens and elements
    if (
      type == MarkdownTokenTypes.LIST_BULLET ||
        type == MarkdownTokenTypes.BLOCK_QUOTE ||
        type == MarkdownTokenTypes.HORIZONTAL_RULE ||
        type == MarkdownTokenTypes.EMPH ||
        type == MarkdownTokenTypes.BACKTICK ||
        type == MarkdownTokenTypes.CODE_FENCE_CONTENT ||
        type == MarkdownTokenTypes.FENCE_LANG ||
        type == MarkdownElementTypes.LINK_DESTINATION ||
        type == MarkdownElementTypes.IMAGE
    ) {
      return
    }

    if (type == MarkdownTokenTypes.TEXT) {
      builder.append(node.getTextInNode(content))
    }

    for (child in node.children) {
      extractTextRecursive(child, content, builder)
    }

    // Add pauses/spacing for block elements
    when (type) {
      MarkdownElementTypes.PARAGRAPH,
      MarkdownElementTypes.LIST_ITEM,
      MarkdownElementTypes.ATX_1,
      MarkdownElementTypes.ATX_2,
      MarkdownElementTypes.ATX_3,
      MarkdownElementTypes.ATX_4,
      MarkdownElementTypes.ATX_5,
      MarkdownElementTypes.ATX_6 -> {
        if (builder.isNotEmpty() && !builder.endsWith(". ")) {
          val lastChar = builder.last()
          if (lastChar.isLetterOrDigit()) {
            builder.append(". ")
          } else if (lastChar == ' ') {
            builder.setLength(builder.length - 1)
            builder.append(". ")
          } else if (lastChar != '.' && lastChar != '!' && lastChar != '?') {
            builder.append(". ")
          } else {
            builder.append(" ")
          }
        }
      }
      MarkdownTokenTypes.WHITE_SPACE,
      MarkdownTokenTypes.EOL -> {
        if (builder.isNotEmpty() && !builder.endsWith(" ") && !builder.endsWith(". ")) {
          builder.append(" ")
        }
      }
    }
  }
}
