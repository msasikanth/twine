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
import com.mikepenz.markdown.model.Input
import com.mikepenz.markdown.model.MarkdownState
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.markdownExtendedSpans
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.intellij.markdown.MarkdownElementTypes
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
import org.intellij.markdown.ast.findChildOfType
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

internal class MarkdownStateImpl(
  private val input: Input,
) : MarkdownState {

  private val stateFlow: MutableStateFlow<State> =
    MutableStateFlow(State.Loading(input.referenceLinkHandler))
  override val state: StateFlow<State> = stateFlow.asStateFlow()

  private val linkStateFlow: MutableStateFlow<Map<String, String?>> = MutableStateFlow(emptyMap())
  override val links: StateFlow<Map<String, String?>> = linkStateFlow.asStateFlow()

  /**
   * Parses the markdown content asynchronously using the Default dispatcher. When a result is
   * available it will be emitted to the [state] flow.
   */
  override suspend fun parse(): State = withContext(Dispatchers.Default) { parseBlocking() }

  /** Parses the markdown content synchronously. */
  private fun parseBlocking(): State {
    return try {
        val parsedResult = input.parser.buildMarkdownTreeFromString(input.content)
        if (input.lookupLinks) {
          val links = mutableMapOf<String, String?>()
          lookupLinkDefinition(links, parsedResult, input.content, recursive = true)
          links.onEach { (key, value) -> input.referenceLinkHandler.store(key, value) }
          linkStateFlow.value = links
        }
        State.Success(parsedResult, input.content, input.lookupLinks, input.referenceLinkHandler)
      } catch (error: Throwable) {
        State.Error(error, input.referenceLinkHandler)
      }
      .also { result -> stateFlow.value = result }
  }
}

/** Helper function to lookup link definitions in the parsed markdown tree. */
internal fun lookupLinkDefinition(
  store: MutableMap<String, String?>,
  node: ASTNode,
  content: String,
  recursive: Boolean = true,
  onlyDefinitions: Boolean = false,
) {
  var linkOnly = false
  val linkLabel =
    if (node.type == MarkdownElementTypes.LINK_DEFINITION) {
      node.findChildOfType(MarkdownElementTypes.LINK_LABEL)?.getUnescapedTextInNode(content)
    } else if (!onlyDefinitions && node.type == MarkdownElementTypes.INLINE_LINK) {
      node.findChildOfType(MarkdownElementTypes.LINK_TEXT)?.getUnescapedTextInNode(content)
    } else if (!onlyDefinitions && node.type == MarkdownElementTypes.AUTOLINK) {
      linkOnly = true
      (node.children.firstOrNull { it.type.name == MarkdownElementTypes.AUTOLINK.name } ?: node)
        .getUnescapedTextInNode(content)
    } else {
      null
    }

  if (linkLabel != null) {
    val destination =
      if (linkOnly) {
        linkLabel
      } else {
        node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getUnescapedTextInNode(content)
      }
    store[linkLabel] = destination
  }

  if (recursive) {
    node.children.forEach { lookupLinkDefinition(store, it, content) }
  }
}
