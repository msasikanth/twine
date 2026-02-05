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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.settingsAboutSubtitle
import twine.shared.generated.resources.settingsAboutTitle

@Composable
internal fun AboutItem(onClick: () -> Unit) {
  Box(modifier = Modifier.clickable(onClick = onClick)) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.settingsAboutTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh,
        )
        Text(
          stringResource(Res.string.settingsAboutSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed,
        )
      }

      AboutProfileImages()
    }
  }
}

@Composable
private fun AboutProfileImages() {
  Box(contentAlignment = Alignment.Center) {
    val backgroundColor = AppTheme.colorScheme.backdrop

    AsyncImage(
      model = Constants.ABOUT_ED_PIC,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier =
        Modifier.padding(start = 72.dp)
          .requiredSize(62.dp)
          .drawWithCache { onDrawBehind { drawCircle(color = backgroundColor) } }
          .padding(6.dp)
          .clip(CircleShape),
    )

    AsyncImage(
      model = Constants.ABOUT_SASI_PIC,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier =
        Modifier.requiredSize(62.dp)
          .drawWithCache { onDrawBehind { drawCircle(color = backgroundColor) } }
          .padding(6.dp)
          .clip(CircleShape),
    )
  }
}
