/*
 * Copyright 2025 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.markdown

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.compose.LocalMarkdownExtendedSpans
import com.mikepenz.markdown.compose.LocalMarkdownPadding
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownText
import com.mikepenz.markdown.compose.extendedspans.ExtendedSpans
import com.mikepenz.markdown.model.markdownExtendedSpans
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import dev.sasikanth.rss.reader.ui.AppTheme
import org.intellij.markdown.MarkdownElementTypes.ATX_1
import org.intellij.markdown.MarkdownElementTypes.ATX_2
import org.intellij.markdown.MarkdownElementTypes.ATX_3
import org.intellij.markdown.MarkdownElementTypes.ATX_4
import org.intellij.markdown.MarkdownElementTypes.ATX_5
import org.intellij.markdown.MarkdownElementTypes.ATX_6
import org.intellij.markdown.MarkdownElementTypes.BLOCK_QUOTE
import org.intellij.markdown.MarkdownElementTypes.CODE_BLOCK
import org.intellij.markdown.MarkdownElementTypes.CODE_FENCE
import org.intellij.markdown.MarkdownElementTypes.IMAGE
import org.intellij.markdown.MarkdownElementTypes.LINK_DEFINITION
import org.intellij.markdown.MarkdownElementTypes.ORDERED_LIST
import org.intellij.markdown.MarkdownElementTypes.PARAGRAPH
import org.intellij.markdown.MarkdownElementTypes.SETEXT_1
import org.intellij.markdown.MarkdownElementTypes.SETEXT_2
import org.intellij.markdown.MarkdownElementTypes.UNORDERED_LIST
import org.intellij.markdown.MarkdownTokenTypes.Companion.EOL
import org.intellij.markdown.MarkdownTokenTypes.Companion.HORIZONTAL_RULE
import org.intellij.markdown.MarkdownTokenTypes.Companion.TEXT
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes.TABLE

@Composable
internal fun handleElement(
  node: ASTNode,
  components: MarkdownComponents,
  content: String,
  includeSpacer: Boolean = true,
  skipLinkDefinition: Boolean = true,
): Boolean {
  val model =
    MarkdownComponentModel(
      content = content,
      node = node,
      typography = LocalMarkdownTypography.current,
    )
  var handled = true
  if (includeSpacer) Spacer(Modifier.height(LocalMarkdownPadding.current.block))

  val linkContentColor = AppTheme.colorScheme.primary
  val squigglyUnderlineAnimator = rememberSquigglyUnderlineAnimator()
  val extendedSpans =
    remember(linkContentColor) {
      ExtendedSpans(
        SquigglyUnderlineSpanPainter(
          contentColor = linkContentColor,
          animator = squigglyUnderlineAnimator
        )
      )
    }

  CompositionLocalProvider(
    LocalMarkdownExtendedSpans provides markdownExtendedSpans { extendedSpans }
  ) {
    when (node.type) {
      TEXT -> {
        MarkdownText(
          content = node.getUnescapedTextInNode(model.content),
          style = MaterialTheme.typography.bodyLarge.copy(color = AppTheme.colorScheme.onSurface)
        )
      }
      EOL -> components.eol(model)
      CODE_FENCE -> components.codeFence(model)
      CODE_BLOCK -> components.codeBlock(model)
      ATX_1 -> components.heading1(model)
      ATX_2 -> components.heading2(model)
      ATX_3 -> components.heading3(model)
      ATX_4 -> components.heading4(model)
      ATX_5 -> components.heading5(model)
      ATX_6 -> components.heading6(model)
      SETEXT_1 -> components.setextHeading1(model)
      SETEXT_2 -> components.setextHeading2(model)
      BLOCK_QUOTE -> components.blockQuote(model)
      PARAGRAPH -> {
        val bodyTextStyle = MaterialTheme.typography.bodyLarge
        val styledText = buildAnnotatedString {
          pushStyle(bodyTextStyle.toSpanStyle())
          buildMarkdownAnnotatedString(
            content = content,
            node = node,
            annotatorSettings =
              annotatorSettings(
                linkTextSpanStyle =
                  TextLinkStyles(
                    bodyTextStyle
                      .copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                      .toSpanStyle()
                  )
              )
          )
          pop()
        }

        MarkdownText(
          modifier = Modifier.fillMaxWidth(),
          content = styledText,
          style = bodyTextStyle.copy(color = AppTheme.colorScheme.onSurface)
        )
      }
      ORDERED_LIST -> components.orderedList(model)
      UNORDERED_LIST -> components.unorderedList(model)
      IMAGE -> components.image(model)
      LINK_DEFINITION -> {
        @Suppress("DEPRECATION") if (!skipLinkDefinition) components.linkDefinition(model)
      }
      HORIZONTAL_RULE -> components.horizontalRule(model)
      TABLE -> components.table(model)
      else -> {
        handled = components.custom?.invoke(node.type, model) != null
      }
    }
  }

  if (!handled) {
    node.children.forEach { child ->
      handleElement(child, components, content, includeSpacer, skipLinkDefinition)
    }
  }

  return handled
}
