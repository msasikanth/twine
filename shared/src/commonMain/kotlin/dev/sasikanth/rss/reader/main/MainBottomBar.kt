/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun MainBottomBar(
  containerColor: ColorProducer,
  borderColor: ColorProducer,
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
) {
  val shape = RoundedCornerShape(50)

  Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Row(
      modifier =
        modifier
          .pointerInput(Unit) {}
          .heightIn(min = 64.dp)
          .widthIn(max = 640.dp)
          .dropShadow(shape) {
            color = Color.Black.copy(alpha = 0.4f)
            offset = Offset(x = 0f, y = 16f)
            radius = 32.dp.toPx()
          }
          .dropShadow(shape) {
            color = Color.Black.copy(alpha = 0.16f)
            offset = Offset(x = 0f, y = 4f)
            radius = 8.dp.toPx()
          }
          .background(containerColor(), shape)
          .border(1.dp, borderColor(), shape)
          .padding(4.dp)
    ) {
      content()
    }
  }
}

@Composable
internal fun BottomBarItem(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  AppTheme(useDarkTheme = true) {
    val backgroundColor by
      animateColorAsState(
        if (selected) {
          AppTheme.colorScheme.secondary.copy(alpha = 0.24f)
        } else {
          Color.Transparent
        }
      )

    Column(
      modifier =
        modifier
          .clip(RoundedCornerShape(50))
          .background(backgroundColor)
          .clickable {
            if (selected.not()) {
              onClick()
            }
          }
          .padding(vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        modifier = Modifier.size(20.dp),
        imageVector = icon,
        contentDescription = null,
        tint = AppTheme.colorScheme.onSurface,
      )

      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = AppTheme.colorScheme.onSurface,
      )
    }
  }
}

enum class MainBottomBarItem {
  Home,
  Saved,
  Search,
  Feeds
}
