/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.utils

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
@ReadOnlyComposable
inline fun Dp.toSp() = with(LocalDensity.current) { this@toSp.toSp() }

inline fun Float.inverseProgress() = 1f - this

@Composable
@ReadOnlyComposable
inline fun Float.toDp() = with(LocalDensity.current) { this@toDp.toDp() }

inline fun pressInteraction(
  coroutineScope: CoroutineScope,
  interactionSource: MutableInteractionSource,
  offset: Offset = Offset.Zero,
  crossinline block: () -> Unit
) {
  val pressInteraction = PressInteraction.Press(offset)
  coroutineScope.launch { interactionSource.emit(pressInteraction) }

  block()

  coroutineScope.launch { interactionSource.emit(PressInteraction.Release(pressInteraction)) }
}

enum class KeyboardState {
  Opened,
  Closed
}

@Composable
fun keyboardVisibilityAsState(): State<KeyboardState> {
  return rememberUpdatedState(
    if (WindowInsets.ime.getBottom(LocalDensity.current) > 0) KeyboardState.Opened
    else KeyboardState.Closed
  )
}
