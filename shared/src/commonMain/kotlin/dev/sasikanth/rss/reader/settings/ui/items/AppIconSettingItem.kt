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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.app.AppIcon
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.ic_launcher_foreground
import twine.shared.generated.resources.settingsAppIconSubtitle
import twine.shared.generated.resources.settingsAppIconTitle

@Composable
internal fun AppIconSettingItem(
  appIcon: AppIcon,
  isSubscribed: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      Modifier.clickable(onClick = onClick)
        .padding(horizontal = 24.dp, vertical = 16.dp)
        .fillMaxWidth()
        .then(modifier),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Column(Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = stringResource(Res.string.settingsAppIconTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh,
        )

        if (!isSubscribed) {
          Spacer(Modifier.width(8.dp))

          Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.Rounded.WorkspacePremium,
            contentDescription = null,
            tint = AppTheme.colorScheme.primary,
          )
        }
      }

      Text(
        text = stringResource(Res.string.settingsAppIconSubtitle),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.textEmphasisMed,
      )
    }

    AppIconPreview(appIcon = appIcon, modifier = Modifier.size(48.dp))
  }
}

@Composable
internal fun AppIconSelectionSheet(
  currentAppIcon: AppIcon,
  onAppIconChange: (AppIcon) -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = AppTheme.colorScheme.onSurface,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    LazyVerticalGrid(
      columns = GridCells.Fixed(3),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier =
        Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 48.dp),
    ) {
      items(AppIcon.entries) { appIcon ->
        val isSelected = appIcon == currentAppIcon
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier =
            Modifier.clip(RoundedCornerShape(12.dp))
              .clickable { onAppIconChange(appIcon) }
              .padding(8.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            val shape = RoundedCornerShape(28.dp)

            AppIconPreview(appIcon = appIcon, shape = shape, modifier = Modifier.size(64.dp))

            if (isSelected) {
              Box(
                Modifier.matchParentSize().clip(shape).background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp),
                )
              }
            }
          }

          Spacer(Modifier.height(8.dp))

          Text(
            text = appIcon.title,
            style = MaterialTheme.typography.labelMedium,
            color =
              if (isSelected) AppTheme.colorScheme.tintedForeground
              else AppTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
          )
        }
      }
    }
  }
}

@Composable
private fun AppIconPreview(
  appIcon: AppIcon,
  modifier: Modifier = Modifier,
  shape: Shape = CircleShape,
) {
  val backgroundColor =
    when (appIcon) {
      AppIcon.AntiqueGold -> Color(0xFFC5A059)
      AppIcon.Cranberry -> Color(0xFFF62F27)
      AppIcon.DarkJade -> Color(0xFF006C53)
      AppIcon.DeepIce -> Color(0xFF29B6F6)
      AppIcon.DeepTeal -> Color(0xFF00838F)
      AppIcon.DustyRose -> Color(0xFFC98CA7)
      AppIcon.RoyalPlum -> Color(0xFF693764)
      AppIcon.SlateBlue -> Color(0xFF375069)
      AppIcon.SoftSage -> Color(0xFF9BB49D)
      AppIcon.StormySky -> Color(0xFF607D8B)
    }
  val backgroundBrush =
    Brush.radialGradient(
      0.17f to backgroundColor.copy(alpha = 0.55f).compositeOver(Color.White),
      1f to backgroundColor,
      center = Offset(20f, 24f),
    )

  Box(
    modifier = modifier.clip(shape).background(backgroundBrush),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      painter = painterResource(Res.drawable.ic_launcher_foreground),
      contentDescription = null,
      tint = Color.Unspecified,
      modifier = Modifier.scale(1.2f).fillMaxSize(),
    )
  }
}
