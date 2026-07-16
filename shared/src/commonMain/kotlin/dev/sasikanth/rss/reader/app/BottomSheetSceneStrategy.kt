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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/** An [OverlayScene] that renders an [entry] within a [ModalBottomSheet]. */
@OptIn(ExperimentalMaterial3Api::class)
internal data class BottomSheetScene<T : Any>(
  override val key: T,
  override val previousEntries: List<NavEntry<T>>,
  override val overlaidEntries: List<NavEntry<T>>,
  private val entry: NavEntry<T>,
  private val modalBottomSheetProperties: ModalBottomSheetProperties,
  private val onBack: () -> Unit,
) : OverlayScene<T> {

  override val entries: List<NavEntry<T>> = listOf(entry)

  override val content: @Composable (() -> Unit) = {
    val lifecycleOwner = rememberLifecycleOwner()
    ModalBottomSheet(onDismissRequest = onBack, properties = modalBottomSheetProperties) {
      CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) { entry.Content() }
    }
  }
}

/**
 * A [SceneStrategy] that displays entries that have added [bottomSheet] to their
 * [NavEntry.metadata] within a [ModalBottomSheet] instance, overlaid on top of the entries below
 * them rather than replacing them.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

  override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
    val lastEntry = entries.lastOrNull() ?: return null
    val bottomSheetProperties = lastEntry.metadata[BottomSheetKey] ?: return null
    @Suppress("UNCHECKED_CAST")
    return BottomSheetScene(
      key = lastEntry.contentKey as T,
      previousEntries = entries.dropLast(1),
      overlaidEntries = entries.dropLast(1),
      entry = lastEntry,
      modalBottomSheetProperties = bottomSheetProperties,
      onBack = onBack,
    )
  }

  companion object {
    /**
     * Function to be called on the [NavEntry.metadata] to mark this entry as something that should
     * be displayed within a [ModalBottomSheet].
     *
     * @param modalBottomSheetProperties properties that should be passed to the containing
     *   [ModalBottomSheet].
     */
    fun bottomSheet(
      modalBottomSheetProperties: ModalBottomSheetProperties = ModalBottomSheetProperties()
    ) = metadata { put(BottomSheetKey, modalBottomSheetProperties) }

    object BottomSheetKey : NavMetadataKey<ModalBottomSheetProperties>
  }
}
