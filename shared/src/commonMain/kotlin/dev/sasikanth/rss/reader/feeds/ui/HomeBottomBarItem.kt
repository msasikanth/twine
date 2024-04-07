/*
 * Copyright 2024 Sasikanth Miriyampalli
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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.All
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.bottomSheetItemLabel

@Composable
internal fun HomeBottomBarItem(
  selected: Boolean = false,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier.wrapContentHeight().then(modifier),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box {
      SelectionIndicator(selected = selected, animationProgress = 1f)
      Box(
        modifier =
          Modifier.clip(RoundedCornerShape(16.dp))
            .background(color = AppTheme.colorScheme.tintedSurface)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
            .align(Alignment.Center),
      ) {
        Icon(
          imageVector = TwineIcons.All,
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }
    }

    Text(
      text = LocalStrings.current.buttonAll,
      style = MaterialTheme.typography.bottomSheetItemLabel,
      textAlign = TextAlign.Center,
      color = AppTheme.colorScheme.textEmphasisHigh,
      modifier = Modifier.wrapContentHeight().requiredWidth(56.dp)
    )
  }
}
