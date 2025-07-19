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

import com.mikepenz.markdown.model.Input
import com.mikepenz.markdown.model.MarkdownState
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType

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
