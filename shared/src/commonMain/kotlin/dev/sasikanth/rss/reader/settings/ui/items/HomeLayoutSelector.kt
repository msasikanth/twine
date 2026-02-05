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

package dev.sasikanth.rss.reader.settings.ui.items

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.resources.icons.LayoutCompact
import dev.sasikanth.rss.reader.resources.icons.LayoutDefault
import dev.sasikanth.rss.reader.resources.icons.LayoutSimple
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.homeViewModeCompact
import twine.shared.generated.resources.homeViewModeDefault
import twine.shared.generated.resources.homeViewModeSimple

@Composable
internal fun HomeLayoutSelector(homeViewMode: HomeViewMode, onClick: (HomeViewMode) -> Unit) {
  Row(modifier = Modifier.padding(horizontal = 8.dp)) {
    LayoutIconButton(
      modifier = Modifier.weight(1f),
      label = stringResource(Res.string.homeViewModeDefault),
      icon = TwineIcons.LayoutDefault,
      selected = homeViewMode == HomeViewMode.Default,
      onClick = { onClick(HomeViewMode.Default) },
    )

    LayoutIconButton(
      modifier = Modifier.weight(1f),
      label = stringResource(Res.string.homeViewModeSimple),
      icon = TwineIcons.LayoutSimple,
      selected = homeViewMode == HomeViewMode.Simple,
      onClick = { onClick(HomeViewMode.Simple) },
    )

    LayoutIconButton(
      modifier = Modifier.weight(1f),
      label = stringResource(Res.string.homeViewModeCompact),
      icon = TwineIcons.LayoutCompact,
      selected = homeViewMode == HomeViewMode.Compact,
      onClick = { onClick(HomeViewMode.Compact) },
    )
  }
}

@Composable
private fun LayoutIconButton(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Column(
    modifier =
      Modifier.then(modifier)
        .clip(MaterialTheme.shapes.medium)
        .clickable { onClick() }
        .padding(vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val defaultTranslucentStyle = LocalTranslucentStyles.current.default
    val background =
      if (selected) {
        Color.Transparent
      } else {
        defaultTranslucentStyle.background
      }
    val border =
      if (selected) {
        defaultTranslucentStyle.outline.copy(alpha = 0.48f)
      } else {
        defaultTranslucentStyle.outline
      }
    val shape =
      if (selected) {
        MaterialTheme.shapes.medium
      } else {
        MaterialTheme.shapes.small
      }
    val iconTint =
      if (selected) {
        AppTheme.colorScheme.inverseOnSurface
      } else {
        AppTheme.colorScheme.outline
      }
    val padding by animateDpAsState(if (selected) 0.dp else 4.dp)

    Box(
      modifier =
        Modifier.requiredSize(48.dp)
          .padding(padding)
          .background(background, shape)
          .border(1.dp, border, shape),
      contentAlignment = Alignment.Center,
    ) {
      val iconBackground by
        animateColorAsState(
          if (selected) {
            AppTheme.colorScheme.primary
          } else {
            Color.Transparent
          }
        )
      val iconBackgroundSize by animateDpAsState(if (selected) 40.dp else 0.dp)

      Box(
        modifier =
          Modifier.requiredSize(iconBackgroundSize)
            .background(iconBackground, MaterialTheme.shapes.small)
      )

      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.requiredSize(20.dp),
      )
    }

    Spacer(Modifier.requiredHeight(4.dp))

    val textStyle =
      if (selected) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall
    Text(text = label, style = textStyle, color = AppTheme.colorScheme.onSurface)
  }
}
