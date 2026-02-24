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

package dev.sasikanth.rss.reader.feeds.ui.pinned

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.tokens.MotionSchemeKeyTokens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

@Stable
internal class PinnedSourcesBottomBarState(
  initialHeightOffsetLimit: Float,
  initialHeightOffset: Float,
  initialContentOffset: Float,
) {
  var heightOffsetLimit by mutableFloatStateOf(initialHeightOffsetLimit)

  var heightOffset by mutableFloatStateOf(initialHeightOffset)
    set(value) {
      field = value.coerceIn(heightOffsetLimit, 0f)
    }

  var contentOffset by mutableFloatStateOf(initialContentOffset)

  companion object {
    val Saver: Saver<PinnedSourcesBottomBarState, *> =
      listSaver(
        save = { listOf(it.heightOffsetLimit, it.heightOffset, it.contentOffset) },
        restore = {
          PinnedSourcesBottomBarState(
            initialHeightOffsetLimit = it[0],
            initialHeightOffset = it[1],
            initialContentOffset = it[2],
          )
        },
      )
  }
}

@Composable
internal fun rememberPinnedSourcesBottomBarState(
  initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
  initialHeightOffset: Float = 0f,
  initialContentOffset: Float = 0f,
): PinnedSourcesBottomBarState {
  return rememberSaveable(saver = PinnedSourcesBottomBarState.Saver) {
    PinnedSourcesBottomBarState(initialHeightOffsetLimit, initialHeightOffset, initialContentOffset)
  }
}

@Stable
internal class PinnedSourcesBottomBarScrollBehavior(
  val state: PinnedSourcesBottomBarState,
  val snapAnimationSpec: AnimationSpec<Float>?,
  val flingAnimationSpec: DecayAnimationSpec<Float>?,
  val canScroll: () -> Boolean = { true },
  val reverseLayout: Boolean = false,
) {
  val nestedScrollConnection =
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (!canScroll()) return Offset.Zero
        val prevHeightOffset = state.heightOffset
        state.heightOffset += available.y
        return if (!reverseLayout && prevHeightOffset != state.heightOffset) {
          available.copy(x = 0f)
        } else {
          Offset.Zero
        }
      }

      override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
      ): Offset {
        if (!canScroll()) return Offset.Zero
        state.contentOffset += consumed.y
        if (!reverseLayout) state.heightOffset += consumed.y
        return Offset.Zero
      }

      override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (
          available.y > 0f &&
            (state.heightOffset == 0f || state.heightOffset == state.heightOffsetLimit)
        ) {
          state.contentOffset = 0f
        }
        return settlePinnedSourcesBottomBar(
          state,
          available.y,
          flingAnimationSpec,
          snapAnimationSpec,
        )
      }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberPinnedSourcesBottomBarScrollBehavior(
  state: PinnedSourcesBottomBarState = rememberPinnedSourcesBottomBarState(),
  canScroll: () -> Boolean = { true },
  snapAnimationSpec: AnimationSpec<Float>? = MotionSchemeKeyTokens.DefaultEffects.value(),
  flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay(),
  reverseLayout: Boolean = false,
): PinnedSourcesBottomBarScrollBehavior =
  remember(state, canScroll, snapAnimationSpec, flingAnimationSpec, reverseLayout) {
    PinnedSourcesBottomBarScrollBehavior(
      state = state,
      snapAnimationSpec = snapAnimationSpec,
      flingAnimationSpec = flingAnimationSpec,
      canScroll = canScroll,
      reverseLayout = reverseLayout,
    )
  }

private suspend fun settlePinnedSourcesBottomBar(
  state: PinnedSourcesBottomBarState,
  velocity: Float,
  flingAnimationSpec: DecayAnimationSpec<Float>?,
  snapAnimationSpec: AnimationSpec<Float>?,
): Velocity {
  if (state.heightOffset == 0f || state.heightOffset == state.heightOffsetLimit) {
    return Velocity.Zero
  }
  var remainingVelocity = velocity
  if (flingAnimationSpec != null && abs(velocity) > 1f) {
    var lastValue = state.heightOffset
    AnimationState(initialValue = state.heightOffset, initialVelocity = velocity).animateDecay(
      flingAnimationSpec
    ) {
      val delta = value - lastValue
      val initialHeightOffset = state.heightOffset
      state.heightOffset = value
      lastValue = value
      remainingVelocity = this.velocity
      if (initialHeightOffset == state.heightOffset) {
        cancelAnimation()
      }
    }
  }
  if (snapAnimationSpec != null) {
    if (state.heightOffset != 0f && state.heightOffset != state.heightOffsetLimit) {
      val target =
        if (state.heightOffset > state.heightOffsetLimit / 2) 0f else state.heightOffsetLimit
      animate(
        initialValue = state.heightOffset,
        targetValue = target,
        animationSpec = snapAnimationSpec,
      ) { value, _ ->
        state.heightOffset = value
      }
    }
  }
  return Velocity(0f, velocity - remainingVelocity)
}
