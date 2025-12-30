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

package dev.sasikanth.rss.reader.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonCancel

@Composable
internal fun ContextActionsBottomBar(
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
  tooltip: (@Composable () -> Unit)? = null,
  content: @Composable RowScope.() -> Unit,
) {
  BottomBarWithGradientShadow(modifier) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
      tooltip?.let { tooltip ->
        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
          CompositionLocalProvider(
            LocalContentColor provides AppTheme.colorScheme.onSurfaceVariant,
            LocalTextStyle provides MaterialTheme.typography.bodySmall
          ) {
            tooltip.invoke()
          }
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp), content = content)

      Spacer(Modifier.requiredHeight(4.dp))

      OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        colors =
          ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Unspecified,
            contentColor = AppTheme.colorScheme.tintedForeground
          ),
        border = BorderStroke(1.dp, AppTheme.colorScheme.tintedHighlight),
        onClick = onCancel
      ) {
        Text(
          text = stringResource(Res.string.buttonCancel),
          style = MaterialTheme.typography.labelLarge
        )
      }
    }
  }
}

@Composable
internal fun ContextActionItem(
  icon: ImageVector,
  label: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  Box(
    Modifier.clip(MaterialTheme.shapes.large)
      .clickable(enabled = enabled) { onClick() }
      .padding(12.dp)
      .then(modifier)
  ) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
      val color =
        if (enabled) {
          AppTheme.colorScheme.tintedForeground
        } else {
          AppTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        }

      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.requiredSize(20.dp)
      )

      Spacer(Modifier.requiredHeight(4.dp))

      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        maxLines = 1,
      )
    }
  }
}
