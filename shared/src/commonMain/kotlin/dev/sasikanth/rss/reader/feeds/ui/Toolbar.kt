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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun Toolbar(modifier: Modifier = Modifier, onCloseClicked: () -> Unit) {
  Box(modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
    IconButton(onClick = onCloseClicked) {
      Icon(
        imageVector = Icons.Rounded.Close,
        contentDescription = null,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    Text(
      text = "Feeds",
      style = MaterialTheme.typography.titleLarge,
      color = AppTheme.colorScheme.textEmphasisHigh,
      textAlign = TextAlign.Center,
      modifier = Modifier.matchParentSize().wrapContentHeight()
    )
  }
}
