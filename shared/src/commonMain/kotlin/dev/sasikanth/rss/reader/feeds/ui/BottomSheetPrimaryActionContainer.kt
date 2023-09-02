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
package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetValue
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetValue.Collapsed
import dev.sasikanth.rss.reader.resources.IconResources
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.bottomSheetItemLabel
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun BottomSheetPrimaryActionButton(
  selected: Boolean = false,
  bottomSheetSwipeProgress: Float,
  bottomSheetCurrentState: BottomSheetValue,
  bottomSheetTargetState: BottomSheetValue,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Column(
    modifier =
      modifier
        .wrapContentHeight()
        .padding(bottom = (4.dp * bottomSheetSwipeProgress).coerceAtLeast(0.dp)),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    val (icon, label) =
      when {
        bottomSheetCurrentState == Collapsed && bottomSheetTargetState == Collapsed ->
          Pair(IconResources.all, null)
        else -> Pair(IconResources.add, LocalStrings.current.buttonAddFeed)
      }

    FloatingActionButton(
      icon = icon,
      selected = selected,
      animationProgress = bottomSheetSwipeProgress,
      onClick = onClick,
      label = label
    )

    Text(
      text = LocalStrings.current.buttonAll,
      style = MaterialTheme.typography.bottomSheetItemLabel,
      textAlign = TextAlign.Center,
      color = AppTheme.colorScheme.textEmphasisHigh,
      modifier =
        Modifier.wrapContentHeight().requiredWidth(56.dp).graphicsLayer {
          alpha = bottomSheetSwipeProgress
        }
    )
  }
}

@Composable
private fun FloatingActionButton(
  modifier: Modifier = Modifier,
  icon: String,
  animationProgress: Float,
  label: String? = null,
  selected: Boolean = false,
  cornerShape: RoundedCornerShape = RoundedCornerShape(16.dp),
  onClick: () -> Unit
) {
  Box(modifier = modifier) {
    SelectionIndicator(selected = selected, animationProgress = animationProgress)
    Row(
      modifier =
        Modifier.clip(cornerShape)
          .background(color = AppTheme.colorScheme.tintedSurface)
          .clickable(onClick = onClick, role = Role.Button)
          .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
          .align(Alignment.Center),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Crossfade(icon) {
        Icon(
          painter = painterResource(icon),
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }

      if (label != null) {
        // We want the button to expand smoothly as the bottom sheet
        // expands. But since this component receives animation progress
        // within a threshold 0..0.2. We are converting the progress back
        // to original progress to have the smooth transition
        val labelAnimationProgress = (1f - animationProgress) / 5f
        Row(
          modifier =
            Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(
                  (placeable.width * labelAnimationProgress)
                    .coerceAtMost(placeable.width.toFloat())
                    .roundToInt(),
                  placeable.height
                ) {
                  placeable.place(0, 0)
                }
              }
              .graphicsLayer { alpha = labelAnimationProgress }
        ) {
          Spacer(Modifier.requiredWidth(12.dp))
          Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = AppTheme.colorScheme.tintedForeground
          )
        }
      }
    }
  }
}
