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
package dev.sasikanth.rss.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.launch

@Composable
fun BoxScope.ScrollToTopButton(
  visible: Boolean,
  modifier: Modifier = Modifier,
  onClick: suspend () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  AnimatedVisibility(
    visible = visible,
    enter = slideInVertically { it / 2 },
    exit = slideOutVertically { it / 2 },
    modifier = Modifier.align(Alignment.BottomEnd)
  ) {
    ExtendedFloatingActionButton(
      modifier = modifier,
      shape = RoundedCornerShape(50),
      containerColor = AppTheme.colorScheme.tintedBackground,
      contentColor = AppTheme.colorScheme.tintedForeground,
      text = {
        Text(text = LocalStrings.current.scrollToTop, color = AppTheme.colorScheme.tintedForeground)
      },
      icon = { Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = null) },
      onClick = { coroutineScope.launch { onClick() } }
    )
  }
}
