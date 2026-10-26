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

package dev.sasikanth.rss.reader.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.feedIconFromFeed
import twine.shared.generated.resources.feedIconFromWebsite

private val PICKER_MAX_WIDTH = 280.dp

/** Lets a feed show its own icon or the website's fav icon, previewing both side by side. */
@Composable
internal fun FeedIconPicker(
  icon: String,
  homepageLink: String,
  showFeedFavIcon: Boolean,
  onShowFeedFavIconChanged: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.widthIn(max = PICKER_MAX_WIDTH).fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    IconChoice(
      modifier = Modifier.weight(1f),
      icon = icon,
      homepageLink = homepageLink,
      useFavIcon = false,
      label = stringResource(Res.string.feedIconFromFeed),
      selected = !showFeedFavIcon,
      onClick = { onShowFeedFavIconChanged(false) },
    )

    IconChoice(
      modifier = Modifier.weight(1f),
      icon = icon,
      homepageLink = homepageLink,
      useFavIcon = true,
      label = stringResource(Res.string.feedIconFromWebsite),
      selected = showFeedFavIcon,
      onClick = { onShowFeedFavIconChanged(true) },
    )
  }
}

@Composable
private fun RowScope.IconChoice(
  icon: String,
  homepageLink: String,
  useFavIcon: Boolean,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val borderColor =
    if (selected) AppTheme.colorScheme.primary else AppTheme.colorScheme.outlineVariant

  Column(
    modifier =
      modifier
        .clip(MaterialTheme.shapes.large)
        .clickable(onClick = onClick)
        .border(
          width = if (selected) 2.dp else 1.dp,
          color = borderColor,
          shape = MaterialTheme.shapes.large,
        )
        .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    FeedIcon(
      modifier = Modifier.requiredSize(48.dp),
      icon = icon,
      homepageLink = homepageLink,
      showFeedFavIcon = useFavIcon,
      contentDescription = null,
    )

    Spacer(Modifier.height(8.dp))

    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = if (selected) AppTheme.colorScheme.primary else AppTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
