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

package dev.sasikanth.rss.reader.reader.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.ArticleShortcut
import dev.sasikanth.rss.reader.resources.icons.OpenBrowser
import dev.sasikanth.rss.reader.resources.icons.Settings
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.cdLoadFullArticle
import twine.shared.generated.resources.openWebsite
import twine.shared.generated.resources.readerSettings

@Composable
internal fun ReaderViewBottomBar(
  loadFullArticle: Boolean,
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
) {
  Row(
    modifier = Modifier.wrapContentWidth().height(IntrinsicSize.Min).padding(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val transition = updateTransition(loadFullArticle)
    val buttonMinWidth by
      transition.animateDp {
        if (it) {
          52.dp
        } else {
          56.dp
        }
      }
    val readerViewToggleWidth by
      transition.animateDp(
        transitionSpec = {
          spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy)
        }
      ) {
        if (it) {
          88.dp
        } else {
          72.dp
        }
      }
    val readerViewToggleVerticalPadding by
      transition.animateDp(
        transitionSpec = {
          spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy)
        }
      ) {
        if (it) {
          8.dp
        } else {
          12.dp
        }
      }
    val readerViewToggleBackgroundColor by
      transition.animateColor {
        if (it) {
          AppTheme.colorScheme.primaryContainer
        } else {
          AppTheme.colorScheme.surfaceContainerHighest
        }
      }
    val readerViewToggleContentColor by
      transition.animateColor {
        if (it) {
          AppTheme.colorScheme.onPrimaryContainer
        } else {
          AppTheme.colorScheme.onSurface
        }
      }

    BottomBarIconButton(
      modifier = Modifier.padding(vertical = 12.dp),
      label = stringResource(Res.string.openWebsite),
      icon = TwineIcons.OpenBrowser,
      onClick = openInBrowserClick,
      minWidth = buttonMinWidth
    )

    BottomBarToggleIconButton(
      modifier = Modifier.fillMaxHeight().padding(vertical = readerViewToggleVerticalPadding),
      label = stringResource(Res.string.cdLoadFullArticle),
      icon = TwineIcons.ArticleShortcut,
      onClick = loadFullArticleClick,
      backgroundColor = readerViewToggleBackgroundColor,
      contentColor = readerViewToggleContentColor,
      minWidth = readerViewToggleWidth
    )

    BottomBarIconButton(
      modifier = Modifier.padding(vertical = 12.dp),
      label = stringResource(Res.string.readerSettings),
      icon = TwineIcons.Settings,
      onClick = openReaderViewSettings,
      minWidth = buttonMinWidth
    )
  }
}

@Composable
private fun BottomBarIconButton(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentColor: Color = AppTheme.colorScheme.onSurfaceVariant,
  minWidth: Dp = 56.dp,
) {
  Box(
    modifier =
      Modifier.then(modifier)
        .requiredSizeIn(minWidth = minWidth)
        .clip(RoundedCornerShape(50))
        .semantics {
          role = Role.Button
          contentDescription = label
        }
        .clickable { onClick() },
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      modifier = Modifier.padding(vertical = 10.dp).requiredSize(20.dp),
      imageVector = icon,
      contentDescription = null,
      tint = contentColor,
    )
  }
}

@Composable
private fun BottomBarToggleIconButton(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  backgroundColor: Color = Color.Transparent,
  contentColor: Color = AppTheme.colorScheme.onSurfaceVariant,
  minWidth: Dp = 72.dp,
) {
  Box(
    modifier =
      Modifier.requiredSizeIn(minHeight = 40.dp, minWidth = minWidth)
        .then(modifier)
        .clip(RoundedCornerShape(50))
        .background(backgroundColor, RoundedCornerShape(50))
        .semantics {
          role = Role.Button
          contentDescription = label
        }
        .clickable { onClick() },
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      modifier = Modifier.requiredSize(20.dp),
      imageVector = icon,
      contentDescription = null,
      tint = contentColor,
    )
  }
}
