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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.rectangle
import dev.sasikanth.rss.reader.app.AppIcon
import dev.sasikanth.rss.reader.resources.icons.StarShine
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.ic_launcher_foreground
import twine.shared.generated.resources.settingsAppIconDefault
import twine.shared.generated.resources.themeVariantAmber
import twine.shared.generated.resources.themeVariantCoral
import twine.shared.generated.resources.themeVariantLavender
import twine.shared.generated.resources.themeVariantParchment
import twine.shared.generated.resources.themeVariantRaspberry
import twine.shared.generated.resources.themeVariantSepia
import twine.shared.generated.resources.themeVariantSkyline
import twine.shared.generated.resources.themeVariantSlate
import twine.shared.generated.resources.themeVariantSolarized

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppIconButton(
  appIcon: AppIcon,
  selected: Boolean,
  isSubscribed: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  showLabel: Boolean = false,
) {
  val iconShape = squircleShape(IconCornerFraction)
  val borderWidth by animateDpAsState(if (selected) 2.dp else 1.dp)
  val borderColor by
    animateColorAsState(
      if (selected) AppTheme.colorScheme.outline else AppTheme.colorScheme.outlineVariant
    )

  Column(
    modifier = Modifier.width(IntrinsicSize.Min),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier =
        Modifier.then(
          if (selected) {
            Modifier.border(borderWidth, borderColor, squircleShape(RingCornerFraction))
          } else {
            Modifier
          }
        ),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier =
          modifier.requiredSize(72.dp).padding(4.dp).clip(iconShape).clickable { onClick() },
        contentAlignment = Alignment.Center,
      ) {
        AppIconPreview(appIcon = appIcon, shape = iconShape, modifier = Modifier.matchParentSize())

        if (appIcon.isPremium && !isSubscribed) {
          PremiumBadge()
        }
      }
    }

    if (showLabel) {
      Spacer(Modifier.height(8.dp))

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = appIcon.displayName(),
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.onSurfaceVariant,
        minLines = 2,
        maxLines = 2,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun BoxScope.PremiumBadge() {
  Box(
    modifier =
      Modifier.align(Alignment.TopEnd)
        .padding(6.dp)
        .size(20.dp)
        .background(AppTheme.colorScheme.surfaceContainerLowest, CircleShape),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = TwineIcons.StarShine,
      contentDescription = null,
      tint = AppTheme.colorScheme.primary,
      modifier = Modifier.size(12.dp),
    )
  }
}

@Composable
internal fun AppIconPreview(
  appIcon: AppIcon,
  modifier: Modifier = Modifier,
  shape: Shape = squircleShape(IconCornerFraction),
) {
  val backgroundColor =
    when (appIcon) {
      AppIcon.Default -> Color(0xFF006C53)
      AppIcon.Solarized -> Color(0xFFA85232)
      AppIcon.Amber -> Color(0xFFD59C20)
      AppIcon.Coral -> Color(0xFFF8875C)
      AppIcon.Raspberry -> Color(0xFFC55FA8)
      AppIcon.Skyline -> Color(0xFF048BD0)
      AppIcon.Lavender -> Color(0xFF8976D3)
      AppIcon.Parchment -> Color(0xFF212121)
      AppIcon.Slate -> Color(0xFF546E7D)
      AppIcon.Sepia -> Color(0xFF8A6246)
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

@Composable
private fun AppIcon.displayName(): String =
  when (this) {
    AppIcon.Default -> stringResource(Res.string.settingsAppIconDefault)
    AppIcon.Solarized -> stringResource(Res.string.themeVariantSolarized)
    AppIcon.Amber -> stringResource(Res.string.themeVariantAmber)
    AppIcon.Coral -> stringResource(Res.string.themeVariantCoral)
    AppIcon.Raspberry -> stringResource(Res.string.themeVariantRaspberry)
    AppIcon.Skyline -> stringResource(Res.string.themeVariantSkyline)
    AppIcon.Lavender -> stringResource(Res.string.themeVariantLavender)
    AppIcon.Parchment -> stringResource(Res.string.themeVariantParchment)
    AppIcon.Slate -> stringResource(Res.string.themeVariantSlate)
    AppIcon.Sepia -> stringResource(Res.string.themeVariantSepia)
  }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun squircleShape(cornerFraction: Float): Shape {
  val polygon =
    remember(cornerFraction) {
      RoundedPolygon.rectangle(
        width = 1f,
        height = 1f,
        rounding = CornerRounding(radius = cornerFraction, smoothing = SquircleSmoothing),
        centerX = 0.5f,
        centerY = 0.5f,
      )
    }
  return polygon.toShape()
}

private const val SquircleSmoothing = 0.6f
private const val IconCornerFraction = 0.25f
private const val RingCornerFraction = 0.28f
