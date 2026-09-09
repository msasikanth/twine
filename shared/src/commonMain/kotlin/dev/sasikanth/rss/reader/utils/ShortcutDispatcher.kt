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

package dev.sasikanth.rss.reader.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.key.KeyEvent

/**
 * Routes shortcuts from the window rather than from whichever composable currently holds focus.
 *
 * Focus-scoped handlers only fire while their own node is focused, so screens hosted as siblings in
 * the navigation graph never see each other's shortcuts, and any two screens that both request
 * focus race for it. Registration follows composition instead, so a screen keeps its shortcuts for
 * as long as it is on screen.
 */
@Stable
class ShortcutDispatcher {

  private val handlers = mutableListOf<(KeyEvent) -> Boolean>()

  fun register(handler: (KeyEvent) -> Boolean) {
    handlers += handler
  }

  fun unregister(handler: (KeyEvent) -> Boolean) {
    handlers -= handler
  }

  /**
   * Handlers are offered the event in reverse registration order, so the most recently composed
   * screen — the detail pane over the list pane — gets first refusal.
   */
  fun dispatch(event: KeyEvent): Boolean {
    for (index in handlers.indices.reversed()) {
      if (handlers[index].invoke(event)) return true
    }
    return false
  }
}

val LocalShortcutDispatcher = staticCompositionLocalOf<ShortcutDispatcher?> { null }

/**
 * Registers [onKeyEvent] with the window's [ShortcutDispatcher] for as long as this composable is
 * in the composition. No-ops on platforms that do not provide a dispatcher.
 */
@Composable
fun ShortcutHandler(enabled: Boolean = true, onKeyEvent: (KeyEvent) -> Boolean) {
  val dispatcher = LocalShortcutDispatcher.current ?: return
  val currentOnKeyEvent by rememberUpdatedState(onKeyEvent)

  DisposableEffect(dispatcher, enabled) {
    val handler: (KeyEvent) -> Boolean = { event ->
      if (enabled) currentOnKeyEvent(event) else false
    }
    dispatcher.register(handler)
    onDispose { dispatcher.unregister(handler) }
  }
}
