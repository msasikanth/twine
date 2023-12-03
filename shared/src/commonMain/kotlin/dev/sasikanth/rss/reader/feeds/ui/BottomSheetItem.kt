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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.bottomSheetItemLabel

private const val BADGE_COUNT_TRIM_LIMIT = 99

@Composable
internal fun BottomSheetItem(
  text: String,
  badgeCount: Long,
  iconUrl: String,
  canShowUnreadPostsCount: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
) {
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Box(contentAlignment = Alignment.Center) {
      SelectionIndicator(selected = selected, animationProgress = 1f)
      Box(
        modifier = Modifier.requiredSize(56.dp).background(Color.White, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
      ) {
        BadgedBox(
          badge = {
            if (badgeCount > 0 && canShowUnreadPostsCount) {
              Badge(
                containerColor = AppTheme.colorScheme.tintedForeground,
                contentColor = AppTheme.colorScheme.tintedBackground,
                modifier =
                  Modifier.sizeIn(minWidth = 24.dp, minHeight = 16.dp).graphicsLayer {
                    translationX = -8.dp.toPx()
                    translationY = 4.dp.toPx()
                  },
              ) {
                val badgeText =
                  if (badgeCount > BADGE_COUNT_TRIM_LIMIT) {
                    "+$BADGE_COUNT_TRIM_LIMIT"
                  } else {
                    badgeCount.toString()
                  }

                Text(badgeText)
              }
            }
          }
        ) {
          AsyncImage(
            url = iconUrl,
            contentDescription = null,
            modifier =
              Modifier.requiredSize(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
          )
        }
      }
    }

    Text(
      text = text.uppercase(),
      style = MaterialTheme.typography.bottomSheetItemLabel,
      overflow = TextOverflow.Ellipsis,
      color = AppTheme.colorScheme.textEmphasisHigh,
      maxLines = 1,
      modifier =
        Modifier.requiredSizeIn(maxWidth = 56.dp).wrapContentHeight(Alignment.CenterVertically)
    )
  }
}

/**
 * This component receives animation progress within the threshold of 0..0.2 of the original bottom
 * sheet transition.
 */
@Composable
internal fun SelectionIndicator(selected: Boolean, animationProgress: Float) {
  // Set alpha to 0 once the progress goes below this threshold
  val threshold = 0.89f

  val size = (64.dp * animationProgress).coerceAtLeast(56.dp)
  val cornerRadius = (20.dp * animationProgress).coerceAtLeast(16.dp)
  val alpha = animationProgress.takeIf { it >= threshold } ?: 0f

  Box(
    Modifier.requiredSize(size).graphicsLayer { this.alpha = alpha },
    contentAlignment = Alignment.Center
  ) {
    AnimatedVisibility(
      modifier = Modifier.matchParentSize(),
      visible = selected,
      enter = scaleIn() + fadeIn(),
      exit = fadeOut() + scaleOut()
    ) {
      Box(
        Modifier.background(AppTheme.colorScheme.tintedForeground, RoundedCornerShape(cornerRadius))
      )
    }
  }
}
