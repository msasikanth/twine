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
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.tokens.MotionSchemeKeyTokens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
private class EnterAlwaysScrollBehavior(
  override val state: TopAppBarState,
  override val snapAnimationSpec: AnimationSpec<Float>?,
  override val flingAnimationSpec: DecayAnimationSpec<Float>?,
  val canScroll: () -> Boolean = { true },
  val reverseLayout: Boolean = false,
) : TopAppBarScrollBehavior {
  override val isPinned: Boolean = false
  override var nestedScrollConnection =
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (!canScroll()) return Offset.Zero
        val prevHeightOffset = state.heightOffset
        state.heightOffset += available.y
        // The state's heightOffset is coerce in a minimum value of heightOffsetLimit and a
        // maximum value 0f, so we check if its value was actually changed after the
        // available.y was added to it in order to tell if the top app bar is currently
        // collapsing or expanding.
        // Note that when the content was set with a revered layout, we always return a
        // zero offset.
        return if (!reverseLayout && prevHeightOffset != state.heightOffset) {
          // We're in the middle of top app bar collapse or expand.
          // Consume only the scroll on the Y axis.
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
          // Reset the total content offset to zero when scrolling all the way down.
          // This will eliminate some float precision inaccuracies.
          state.contentOffset = 0f
        }
        val superConsumed = Velocity.Zero
        return superConsumed +
          settleAppBar(state, available.y, flingAnimationSpec, snapAnimationSpec)
      }
    }
}

@ExperimentalMaterial3Api
@Composable
fun enterAlwaysScrollBehavior(
  state: TopAppBarState = rememberTopAppBarState(),
  canScroll: () -> Boolean = { true },
  snapAnimationSpec: AnimationSpec<Float>? = MotionSchemeKeyTokens.DefaultEffects.value(),
  flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay(),
  reverseLayout: Boolean = false,
): TopAppBarScrollBehavior =
  remember(state, canScroll, snapAnimationSpec, flingAnimationSpec, reverseLayout) {
    EnterAlwaysScrollBehavior(
      state = state,
      snapAnimationSpec = snapAnimationSpec,
      flingAnimationSpec = flingAnimationSpec,
      canScroll = canScroll,
      reverseLayout = reverseLayout,
    )
  }

@OptIn(ExperimentalMaterial3Api::class)
private suspend fun settleAppBar(
  state: TopAppBarState,
  velocity: Float,
  flingAnimationSpec: DecayAnimationSpec<Float>?,
  snapAnimationSpec: AnimationSpec<Float>?,
): Velocity {
  // Check if the app bar is already at its limits.
  if (state.heightOffset == 0f || state.heightOffset == state.heightOffsetLimit) {
    return Velocity.Zero
  }
  var remainingVelocity = velocity
  // If there is a fling spec, try to fling the app bar.
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
  // If there is a snap spec, snap the app bar to its closest limit.
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
