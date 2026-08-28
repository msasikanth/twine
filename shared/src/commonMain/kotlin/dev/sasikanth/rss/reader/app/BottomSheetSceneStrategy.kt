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
package dev.sasikanth.rss.reader.app

import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * An [OverlayScene] that renders an [entry] on top of the entries below it. The entry is expected
 * to host its own [ModalBottomSheet] so that it keeps control of its container color, window
 * insets, and sheet state.
 */
internal data class BottomSheetScene<T : Any>(
  override val key: T,
  override val previousEntries: List<NavEntry<T>>,
  override val overlaidEntries: List<NavEntry<T>>,
  private val entry: NavEntry<T>,
) : OverlayScene<T> {

  override val entries: List<NavEntry<T>> = listOf(entry)

  override val content: @Composable (() -> Unit) = {
    val lifecycleOwner = rememberLifecycleOwner()
    CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) { entry.Content() }
  }
}

/**
 * A [SceneStrategy] that displays entries that have added [bottomSheet] to their
 * [NavEntry.metadata] as an overlay on top of the entries below them rather than replacing them.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
internal class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

  override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
    val lastEntry = entries.lastOrNull() ?: return null
    if (BottomSheetKey !in lastEntry.metadata) return null
    @Suppress("UNCHECKED_CAST")
    return BottomSheetScene(
      key = lastEntry.contentKey as T,
      previousEntries = entries.dropLast(1),
      overlaidEntries = entries.dropLast(1),
      entry = lastEntry,
    )
  }

  companion object {
    /**
     * Function to be called on the [NavEntry.metadata] to mark this entry as something that hosts
     * its own [ModalBottomSheet] and should be overlaid on the entries below it.
     */
    fun bottomSheet() = metadata { put(BottomSheetKey, Unit) }

    object BottomSheetKey : NavMetadataKey<Unit>
  }
}
