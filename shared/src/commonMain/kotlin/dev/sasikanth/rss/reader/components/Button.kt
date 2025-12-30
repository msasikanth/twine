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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles

@Composable
fun Button(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = MaterialTheme.shapes.medium,
  colors: ButtonColors =
    ButtonDefaults.buttonColors(
      containerColor = AppTheme.colorScheme.tintedForeground,
      contentColor = AppTheme.colorScheme.tintedBackground,
      disabledContainerColor = AppTheme.colorScheme.onSurface.copy(alpha = 0.12f),
      disabledContentColor = AppTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ),
  content: @Composable RowScope.() -> Unit
) {
  androidx.compose.material3.Button(
    modifier = modifier,
    onClick = onClick,
    colors = colors,
    shape = shape,
    content = content,
    enabled = enabled
  )
}

@Composable
fun IconButton(
  icon: ImageVector,
  contentDescription: String?,
  darkTheme: Boolean,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  blendMode: BlendMode = if (darkTheme) BlendMode.Screen else BlendMode.Multiply,
  onClick: () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }

  Box(
    modifier =
      Modifier.semantics { role = Role.Button }
        .requiredSize(40.dp)
        .graphicsLayer { this.blendMode = blendMode }
        .clip(MaterialTheme.shapes.small)
        .clickable(
          interactionSource = interactionSource,
          indication = ripple(color = AppTheme.colorScheme.secondary),
          enabled = enabled,
          onClick = onClick
        )
        .then(modifier),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      modifier = Modifier.requiredSize(20.dp),
      imageVector = icon,
      contentDescription = contentDescription,
      tint = AppTheme.colorScheme.secondary
    )
  }
}

@Composable
fun CircularIconButton(
  icon: ImageVector,
  label: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  onClick: () -> Unit
) {
  val animatedAlpha by animateFloatAsState(if (enabled) 1.0f else 0.12f)
  val translucentStyle = LocalTranslucentStyles.current

  Box(
    modifier =
      modifier
        .requiredSize(40.dp)
        .clip(CircleShape)
        .clickable(enabled = enabled) { onClick() }
        .background(translucentStyle.default.background, CircleShape)
        .border(width = 1.dp, color = translucentStyle.default.outline, shape = CircleShape)
        .semantics {
          contentDescription = label
          role = Role.Button
        }
        .graphicsLayer { alpha = animatedAlpha },
    contentAlignment = Alignment.Center
  ) {
    Icon(
      modifier = Modifier.requiredSize(20.dp),
      imageVector = icon,
      contentDescription = null,
      tint = AppTheme.colorScheme.onSurface,
    )
  }
}
