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

package dev.sasikanth.rss.reader.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope

internal val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

internal val LocalNavAnimatedContentScopeOrNull =
  compositionLocalWithComputedDefaultOf<AnimatedContentScope?> {
    runCatching { LocalNavAnimatedContentScope.currentValue }.getOrNull()
  }

internal val LocalImageSharedElementScopeKey = compositionLocalOf<String?> { null }

internal val ImageViewerBoundsTransform = BoundsTransform { _, _ ->
  spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = 320f,
    visibilityThreshold = Rect.VisibilityThreshold,
  )
}

internal val ImageViewerCornerRadiusSpec: FiniteAnimationSpec<Dp> =
  spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = 320f,
    visibilityThreshold = Dp.VisibilityThreshold,
  )
