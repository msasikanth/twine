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

package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.RadioSelected
import dev.sasikanth.rss.reader.resources.icons.RadioUnselected
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun SelectedCheckIndicator(selected: Boolean, modifier: Modifier = Modifier) {
  val icon =
    if (selected) {
      TwineIcons.RadioSelected
    } else {
      TwineIcons.RadioUnselected
    }

  val tint =
    if (selected) {
      AppTheme.colorScheme.tintedForeground
    } else {
      AppTheme.colorScheme.onSurface
    }

  Box(modifier = modifier.requiredSize(40.dp), contentAlignment = Alignment.Center) {
    Icon(
      modifier = Modifier.requiredSize(20.dp),
      imageVector = icon,
      contentDescription = null,
      tint = tint,
    )
  }
}
