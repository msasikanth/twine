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

package dev.sasikanth.rss.reader.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.dynamiccolor.DynamicColor
import com.materialkolor.dynamiccolor.MaterialDynamicColors
import com.materialkolor.hct.Hct
import com.materialkolor.ktx.getColor
import com.materialkolor.scheme.DynamicScheme
import com.materialkolor.scheme.SchemeContent
import com.materialkolor.scheme.SchemeTonalSpot

internal object TwineDynamicColors {

  private val dynamicColors = MaterialDynamicColors()

  private val bottomSheet: DynamicColor =
    DynamicColor.fromPalette(
      name = "bottomSheet",
      palette = { s: DynamicScheme -> s.primaryPalette },
      tone = { s: DynamicScheme -> if (s.isDark) 0.0 else 5.0 },
      isBackground = true,
    )

  private val bottomSheetInverse: DynamicColor =
    DynamicColor.fromPalette(
      name = "bottomSheet",
      palette = { s: DynamicScheme -> s.primaryPalette },
      tone = { s: DynamicScheme -> if (s.isDark) 5.0 else 0.0 },
      isBackground = true,
    )

  private val backdrop: DynamicColor =
    DynamicColor.fromPalette(
      name = "backdrop",
      palette = { s: DynamicScheme -> s.neutralPalette },
      tone = { s: DynamicScheme -> if (s.isDark) 5.0 else 95.0 },
      isBackground = true,
    )

  private val bottomSheetBorder: DynamicColor =
    DynamicColor.fromPalette(
      name = "bottomSheetBorder",
      palette = { s: DynamicScheme -> s.neutralPalette },
      tone = { 20.0 },
    )

  fun calculateColorScheme(
    seedColor: Color,
    useDarkTheme: Boolean,
    useTonalSpotScheme: Boolean,
    defaultColorScheme: AppColorScheme,
  ): AppColorScheme {
    val sourceColorHct = Hct.fromInt(seedColor.toArgb())
    val scheme =
      if (useTonalSpotScheme) {
        SchemeTonalSpot(sourceColorHct = sourceColorHct, isDark = useDarkTheme, contrastLevel = 0.0)
      } else {
        SchemeContent(sourceColorHct = sourceColorHct, isDark = useDarkTheme, contrastLevel = 0.0)
      }

    return AppColorScheme(
      primary = dynamicColors.primary().getColor(scheme),
      onPrimary = dynamicColors.onPrimary().getColor(scheme),
      secondary = dynamicColors.secondary().getColor(scheme),
      outline = dynamicColors.outline().getColor(scheme),
      outlineVariant = dynamicColors.outlineVariant().getColor(scheme),
      primaryContainer = dynamicColors.primaryContainer().getColor(scheme),
      onPrimaryContainer = dynamicColors.onPrimaryContainer().getColor(scheme),
      surface = dynamicColors.surface().getColor(scheme),
      onSurface = dynamicColors.onSurface().getColor(scheme),
      onSurfaceVariant = dynamicColors.onSurfaceVariant().getColor(scheme),
      surfaceContainer = dynamicColors.surfaceContainer().getColor(scheme),
      surfaceContainerLow = dynamicColors.surfaceContainerLow().getColor(scheme),
      surfaceContainerLowest = dynamicColors.surfaceContainerLowest().getColor(scheme),
      surfaceContainerHigh = dynamicColors.surfaceContainerHigh().getColor(scheme),
      surfaceContainerHighest = dynamicColors.surfaceContainerHighest().getColor(scheme),
      inversePrimary = dynamicColors.inversePrimary().getColor(scheme),
      inverseSurface = dynamicColors.inverseSurface().getColor(scheme),
      inverseOnSurface = dynamicColors.inverseOnSurface().getColor(scheme),
      error = dynamicColors.error().getColor(scheme),
      textEmphasisHigh = defaultColorScheme.textEmphasisHigh,
      textEmphasisMed = defaultColorScheme.textEmphasisMed,
      backdrop = backdrop.getColor(scheme),
      bottomSheet = bottomSheet.getColor(scheme),
      bottomSheetInverse = bottomSheetInverse.getColor(scheme),
      bottomSheetBorder = bottomSheetBorder.getColor(scheme),
      tintedBackground = bottomSheet.getColor(scheme),
      tintedSurface = dynamicColors.surfaceContainerLow().getColor(scheme),
      tintedForeground = dynamicColors.primary().getColor(scheme),
      tintedHighlight = dynamicColors.outline().getColor(scheme),
    )
  }
}
