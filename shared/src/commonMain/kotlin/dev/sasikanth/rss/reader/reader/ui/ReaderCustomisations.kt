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

package dev.sasikanth.rss.reader.reader.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatLineSpacing
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import dev.sasikanth.rss.reader.resources.icons.CustomTypography
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.readerCustomisationsTypeface

@Composable
internal fun ReaderCustomisationsContent(
  selectedFont: ReaderFont,
  fontScaleFactor: Float,
  fontLineHeightFactor: Float,
  onFontChange: (ReaderFont) -> Unit,
  onFontScaleFactorChange: (Float) -> Unit,
  onFontLineHeightFactorChange: (Float) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
    CustomisationsTypefaceHeader()

    LazyRow(
      contentPadding =
        PaddingValues(
          start = 28.dp,
          top = 8.dp,
          end = 28.dp,
          bottom = 16.dp,
        ),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      items(ReaderFont.entries) { fontStyle ->
        TypefaceChip(
          selected = fontStyle == selectedFont,
          label = fontStyle.value,
          onClick = { onFontChange(fontStyle) }
        )
      }
    }

    FontScaleStepper(
      modifier = Modifier.padding(top = 8.dp),
      defaultValue = fontScaleFactor,
      onValueChange = { onFontScaleFactorChange(it) }
    )

    FontLineHeightStepper(
      modifier = Modifier.padding(top = 8.dp),
      defaultValue = fontLineHeightFactor,
      onValueChange = { onFontLineHeightFactorChange(it) }
    )
  }
}

@Composable
private fun FontLineHeightStepper(
  defaultValue: Float,
  modifier: Modifier = Modifier,
  onValueChange: (Float) -> Unit
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.requiredSize(20.dp),
      imageVector = Icons.Rounded.FormatLineSpacing,
      contentDescription = null,
      tint = AppTheme.colorScheme.onSurface,
    )

    val sliderColors =
      SliderDefaults.colors(
        activeTrackColor = AppTheme.colorScheme.primary,
        inactiveTickColor = AppTheme.colorScheme.inverseSurface,
        inactiveTrackColor = AppTheme.colorScheme.surfaceContainerHigh,
      )
    Slider(
      modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
      value = defaultValue,
      onValueChange = onValueChange,
      valueRange = 1f..2f,
      steps = 9,
      thumb = {
        Box(
          modifier =
            Modifier.requiredSize(24.dp)
              .shadow(elevation = 2.dp, shape = CircleShape)
              .background(AppTheme.colorScheme.inverseSurface, CircleShape)
              .border(1.dp, AppTheme.colorScheme.secondary, CircleShape),
        )
      },
      track = {
        SliderDefaults.Track(
          sliderState = it,
          thumbTrackGapSize = 0.dp,
          colors = sliderColors,
        )
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
      imageVector = Icons.Rounded.FormatSize,
      contentDescription = null,
      tint = AppTheme.colorScheme.onSurface,
    )

    val sliderColors =
      SliderDefaults.colors(
        activeTrackColor = AppTheme.colorScheme.primary,
        inactiveTickColor = AppTheme.colorScheme.inverseSurface,
        inactiveTrackColor = AppTheme.colorScheme.surfaceContainerHigh,
      )
    Slider(
      modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
      value = defaultValue,
      onValueChange = onValueChange,
      valueRange = 0.8f..2f,
      steps = 11,
      thumb = {
        Box(
          modifier =
            Modifier.requiredSize(24.dp)
              .shadow(elevation = 2.dp, shape = CircleShape)
              .background(AppTheme.colorScheme.inverseSurface, CircleShape)
              .border(1.dp, AppTheme.colorScheme.secondary, CircleShape),
        )
      },
      track = {
        SliderDefaults.Track(
          sliderState = it,
          thumbTrackGapSize = 0.dp,
          colors = sliderColors,
        )
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
      modifier = Modifier.padding(16.dp)
    )
  }
}

@Composable
private fun TypefaceChip(
  selected: Boolean,
  label: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  val chipOuterPadding by animateDpAsState(if (!selected) 4.dp else 0.dp)
  val chipPadding by animateDpAsState(if (selected) 4.dp else 0.dp)

  Box(
    modifier =
      Modifier.then(modifier)
        .clip(RoundedCornerShape(50))
        .clickable { onClick() }
        .padding(vertical = chipOuterPadding)
        .background(AppTheme.colorScheme.secondary.copy(alpha = 0.08f), RoundedCornerShape(50))
        .border(1.dp, AppTheme.colorScheme.secondary, RoundedCornerShape(50))
        .padding(chipPadding)
  ) {
    val background by
      animateColorAsState(if (selected) AppTheme.colorScheme.inverseSurface else Color.Transparent)
    val contentColor by
      animateColorAsState(
        if (selected) AppTheme.colorScheme.inverseOnSurface else AppTheme.colorScheme.onSurface
      )

    Box(
      modifier =
        Modifier.background(background, RoundedCornerShape(50))
          .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      Text(text = label, color = contentColor)
    }
  }
}

private fun Float.roundToDecimals(decimals: Int): Float {
  var dotAt = 1
  repeat(decimals) { dotAt *= 10 }
  val roundedValue = (this * dotAt).roundToInt()
  return (roundedValue / dotAt) + (roundedValue % dotAt).toFloat() / dotAt
}
