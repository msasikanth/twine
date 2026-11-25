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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import dev.sasikanth.rss.reader.components.ThemeVariantIconButton
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import dev.sasikanth.rss.reader.data.repository.ReaderFont.ComicNeue
import dev.sasikanth.rss.reader.data.repository.ReaderFont.Golos
import dev.sasikanth.rss.reader.data.repository.ReaderFont.GoogleSans
import dev.sasikanth.rss.reader.data.repository.ReaderFont.Lora
import dev.sasikanth.rss.reader.data.repository.ReaderFont.Merriweather
import dev.sasikanth.rss.reader.data.repository.ReaderFont.Outfit
import dev.sasikanth.rss.reader.data.repository.ReaderFont.RobotoSerif
import dev.sasikanth.rss.reader.data.repository.isDynamicThemeSupported
import dev.sasikanth.rss.reader.data.repository.isPremium
import dev.sasikanth.rss.reader.resources.icons.CustomTypography
import dev.sasikanth.rss.reader.resources.icons.FormatLineSpacing
import dev.sasikanth.rss.reader.resources.icons.FormatSize
import dev.sasikanth.rss.reader.resources.icons.Palette
import dev.sasikanth.rss.reader.resources.icons.StarShine
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.ComicNeueFontFamily
import dev.sasikanth.rss.reader.ui.GolosFontFamily
import dev.sasikanth.rss.reader.ui.GoogleSansFontFamily
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.ui.LoraFontFamily
import dev.sasikanth.rss.reader.ui.MerriWeatherFontFamily
import dev.sasikanth.rss.reader.ui.OutfitFontFamily
import dev.sasikanth.rss.reader.ui.RobotoSerifFontFamily
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.readerCustomisationsTypeface
import twine.shared.generated.resources.themeVariantColorScheme

private val CustomizationsPillShape = RoundedCornerShape(50)
private val ThemeVariantRowPadding = PaddingValues(horizontal = 28.dp, vertical = 8.dp)
private val TypefaceRowPadding =
  PaddingValues(start = 28.dp, top = 8.dp, end = 28.dp, bottom = 16.dp)

@Composable
internal fun ReaderCustomizationsContent(
  selectedFont: ReaderFont,
  selectedThemeVariant: ThemeVariant,
  fontScaleFactor: Float,
  fontLineHeightFactor: Float,
  isSubscribed: Boolean,
  isParentThemeDark: Boolean,
  onFontChange: (ReaderFont) -> Unit,
  onThemeVariantChange: (ThemeVariant) -> Unit,
  onFontScaleFactorChange: (Float) -> Unit,
  onFontLineHeightFactorChange: (Float) -> Unit,
) {
  val colorScheme = AppTheme.colorScheme
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .drawBehind { drawRect(colorScheme.bottomSheet) }
        .padding(vertical = 16.dp)
  ) {
    ColorSchemeHeader()

    val availableThemeVariants = remember {
      ThemeVariant.entries.filter { it != ThemeVariant.Dynamic || isDynamicThemeSupported }
    }
    val themeVariantListState =
      rememberLazyListState(
        initialFirstVisibleItemIndex =
          availableThemeVariants.indexOf(selectedThemeVariant).coerceAtLeast(0)
      )

    LazyRow(
      state = themeVariantListState,
      contentPadding = ThemeVariantRowPadding,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.Top,
    ) {
      items(items = availableThemeVariants, key = { it.name }, contentType = { "theme-variant" }) {
        themeVariant ->
        ThemeVariantIconButton(
          themeVariant = themeVariant,
          selected = themeVariant == selectedThemeVariant,
          isSubscribed = isSubscribed,
          useDarkTheme = isParentThemeDark,
          showLabel = true,
          onClick = { onThemeVariantChange(themeVariant) },
        )
      }
    }

    CustomisationsTypefaceHeader()

    val activeTypefaceIndex = ReaderFont.entries.indexOf(selectedFont)
    val typefaceListState =
      rememberLazyListState(initialFirstVisibleItemIndex = activeTypefaceIndex)

    LazyRow(
      state = typefaceListState,
      contentPadding = TypefaceRowPadding,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      items(items = ReaderFont.entries, key = { it.name }, contentType = { "reader-font" }) {
        fontStyle ->
        val fontFamily =
          when (fontStyle) {
            ComicNeue -> ComicNeueFontFamily
            GoogleSans -> GoogleSansFontFamily
            Golos -> GolosFontFamily
            Lora -> LoraFontFamily
            Merriweather -> MerriWeatherFontFamily
            RobotoSerif -> RobotoSerifFontFamily
            Outfit -> OutfitFontFamily
          }

        TypefaceChip(
          selected = fontStyle == selectedFont,
          label = fontStyle.value,
          fontFamily = fontFamily,
          isPremium = fontStyle.isPremium,
          isSubscribed = isSubscribed,
          onClick = { onFontChange(fontStyle) },
        )
      }
    }

    Spacer(Modifier.requiredHeight(8.dp))

    FontScaleStepper(
      defaultValue = fontScaleFactor,
      onValueChange = { onFontScaleFactorChange(it) },
    )

    FontLineHeightStepper(
      defaultValue = fontLineHeightFactor,
      onValueChange = { onFontLineHeightFactorChange(it) },
    )
  }
}

@Composable
private fun FontLineHeightStepper(
  defaultValue: Float,
  modifier: Modifier = Modifier,
  onValueChange: (Float) -> Unit,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.requiredSize(20.dp),
      imageVector = TwineIcons.FormatLineSpacing,
      contentDescription = null,
      tint = AppTheme.colorScheme.onSurface,
    )

    val sliderColors =
      SliderDefaults.colors(
        activeTrackColor = AppTheme.colorScheme.primary,
        inactiveTickColor = AppTheme.colorScheme.inverseSurface,
        inactiveTrackColor = AppTheme.colorScheme.surfaceContainerHigh,
      )
    val sliderState = rememberSliderState(value = defaultValue, steps = 9, trackRange = 1f..2f)
    sliderState.value = defaultValue

    Slider(
      modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
      state = sliderState,
      onValueChange = onValueChange,
      thumb = {
        Box(
          modifier =
            Modifier.requiredSize(24.dp)
              .shadow(elevation = 2.dp, shape = CustomizationsPillShape)
              .background(AppTheme.colorScheme.inverseSurface, CustomizationsPillShape)
              .border(1.dp, AppTheme.colorScheme.secondary, CustomizationsPillShape)
        )
      },
      track = {
        SliderDefaults.Track(sliderState = it, thumbTrackGapSize = 0.dp, colors = sliderColors)
      },
    )

    Text(
      modifier = Modifier.requiredWidthIn(min = 40.dp),
      text = defaultValue.roundToDecimals(1).toString(),
      maxLines = 1,
      textAlign = TextAlign.End,
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun FontScaleStepper(
  defaultValue: Float,
  modifier: Modifier = Modifier,
  onValueChange: (Float) -> Unit,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.requiredSize(20.dp),
      imageVector = TwineIcons.FormatSize,
      contentDescription = null,
      tint = AppTheme.colorScheme.onSurface,
    )

    val sliderColors =
      SliderDefaults.colors(
        activeTrackColor = AppTheme.colorScheme.primary,
        inactiveTickColor = AppTheme.colorScheme.inverseSurface,
        inactiveTrackColor = AppTheme.colorScheme.surfaceContainerHigh,
      )
    val sliderState = rememberSliderState(value = defaultValue, steps = 11, trackRange = 0.8f..2f)
    sliderState.value = defaultValue

    Slider(
      modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
      state = sliderState,
      onValueChange = onValueChange,
      thumb = {
        Box(
          modifier =
            Modifier.requiredSize(24.dp)
              .shadow(elevation = 2.dp, shape = CustomizationsPillShape)
              .background(AppTheme.colorScheme.inverseSurface, CustomizationsPillShape)
              .border(1.dp, AppTheme.colorScheme.secondary, CustomizationsPillShape)
        )
      },
      track = {
        SliderDefaults.Track(sliderState = it, thumbTrackGapSize = 0.dp, colors = sliderColors)
      },
    )

    Text(
      modifier = Modifier.requiredWidthIn(min = 40.dp),
      text = "${(defaultValue * 100).roundToInt()}%",
      maxLines = 1,
      textAlign = TextAlign.End,
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun CustomisationsTypefaceHeader() {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.requiredSize(20.dp),
      imageVector = TwineIcons.CustomTypography,
      contentDescription = null,
      tint = AppTheme.colorScheme.onSurface,
    )

    Text(
      text = stringResource(Res.string.readerCustomisationsTypeface),
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.onSurface,
      modifier = Modifier.padding(16.dp),
    )
  }
}

@Composable
private fun TypefaceChip(
  selected: Boolean,
  label: String,
  isPremium: Boolean,
  isSubscribed: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  fontFamily: FontFamily = FontFamily.Default,
) {
  val chipOuterPaddingState = animateDpAsState(if (!selected) 4.dp else 0.dp)
  val chipPaddingState = animateDpAsState(if (selected) 4.dp else 0.dp)
  val selectionProgressState = animateFloatAsState(if (selected) 1f else 0f)
  val translucentStyle = LocalTranslucentStyles.current
  val colorScheme = AppTheme.colorScheme
  val baseTextStyle = LocalTextStyle.current
  val labelStyle =
    remember(baseTextStyle, fontFamily) { baseTextStyle.copy(fontFamily = fontFamily) }
  val contentColorProducer =
    remember(colorScheme, selectionProgressState) {
      ColorProducer {
        val progress = selectionProgressState.value
        when {
          progress <= 0f -> colorScheme.onSurface
          progress >= 1f -> colorScheme.inverseOnSurface
          else -> lerp(colorScheme.onSurface, colorScheme.inverseOnSurface, progress)
        }
      }
    }

  Box(
    modifier =
      Modifier.then(modifier)
        .clip(CustomizationsPillShape)
        .clickable { onClick() }
        .layout { measurable, constraints ->
          val vPadPx = chipOuterPaddingState.value.roundToPx()
          val placeable = measurable.measure(constraints.offset(vertical = -vPadPx * 2))
          layout(placeable.width, placeable.height + vPadPx * 2) {
            placeable.placeRelative(0, vPadPx)
          }
        }
        .drawWithCache {
          val outline = CustomizationsPillShape.createOutline(size, layoutDirection, this)
          val stroke = Stroke(width = 1.dp.toPx())
          onDrawWithContent {
            drawOutline(outline = outline, color = translucentStyle.default.background)
            drawContent()
            drawOutline(outline = outline, color = colorScheme.secondary, style = stroke)
          }
        }
        .layout { measurable, constraints ->
          val padPx = chipPaddingState.value.roundToPx()
          val placeable =
            measurable.measure(constraints.offset(horizontal = -padPx * 2, vertical = -padPx * 2))
          layout(placeable.width + padPx * 2, placeable.height + padPx * 2) {
            placeable.placeRelative(padPx, padPx)
          }
        }
  ) {
    Row(
      modifier =
        Modifier.clip(CustomizationsPillShape)
          .drawBehind {
            val progress = selectionProgressState.value
            if (progress > 0f) {
              val bgColor =
                if (progress >= 1f) {
                  colorScheme.inverseSurface
                } else {
                  lerp(Color.Transparent, colorScheme.inverseSurface, progress)
                }
              drawRect(bgColor)
            }
          }
          .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      BasicText(text = label, style = labelStyle, color = contentColorProducer)

      if (isPremium && !isSubscribed) {
        val starPainter = rememberVectorPainter(TwineIcons.StarShine)
        Spacer(
          modifier =
            Modifier.requiredSize(16.dp).drawBehind {
              with(starPainter) {
                draw(size = size, colorFilter = ColorFilter.tint(contentColorProducer()))
              }
            }
        )
      }
    }
  }
}

@Composable
private fun ColorSchemeHeader() {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.requiredSize(20.dp),
      imageVector = TwineIcons.Palette,
      contentDescription = null,
      tint = AppTheme.colorScheme.onSurface,
    )

    Text(
      text = stringResource(Res.string.themeVariantColorScheme),
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.onSurface,
      modifier = Modifier.padding(16.dp),
    )
  }
}

private fun Float.roundToDecimals(decimals: Int): Float {
  var dotAt = 1
  repeat(decimals) { dotAt *= 10 }
  val roundedValue = (this * dotAt).roundToInt()
  return (roundedValue / dotAt) + (roundedValue % dotAt).toFloat() / dotAt
}
