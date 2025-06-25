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
package dev.sasikanth.rss.reader.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sasikanth.rss.reader.utils.toSp
import org.jetbrains.compose.resources.Font
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.golos_bold
import twine.shared.generated.resources.golos_medium
import twine.shared.generated.resources.golos_regular
import twine.shared.generated.resources.lora_bold
import twine.shared.generated.resources.lora_medium
import twine.shared.generated.resources.lora_regular
import twine.shared.generated.resources.merriweather_bold
import twine.shared.generated.resources.merriweather_medium
import twine.shared.generated.resources.merriweather_regular
import twine.shared.generated.resources.robot_serif_bold
import twine.shared.generated.resources.roboto_serif_medium
import twine.shared.generated.resources.roboto_serif_regular

expect fun createDefaultTextStyle(): TextStyle

private val figmaLineHeightStyle =
  LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None)

@Stable
private val defaultTextStyle =
  createDefaultTextStyle()
    .copy(
      textDirection = TextDirection.ContentOrLtr,
    )

internal fun typography(
  fontFamily: FontFamily,
  fontScalingFactor: Float = 1f,
  lineHeightScalingFactor: Float = 1f,
) =
  Typography(
    displayLarge =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp * fontScalingFactor,
        lineHeight = 64.sp * lineHeightScalingFactor,
        lineHeightStyle = figmaLineHeightStyle,
      ),
    displayMedium =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp * fontScalingFactor,
        lineHeight = 52.sp * lineHeightScalingFactor,
        lineHeightStyle = figmaLineHeightStyle
      ),
    displaySmall =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp * fontScalingFactor,
        lineHeight = 44.sp * lineHeightScalingFactor,
        lineHeightStyle = figmaLineHeightStyle
      ),
    headlineLarge =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp * fontScalingFactor,
        lineHeight = 40.sp * lineHeightScalingFactor,
        lineHeightStyle = figmaLineHeightStyle
      ),
    headlineMedium =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp * fontScalingFactor,
        lineHeight = 36.sp * lineHeightScalingFactor,
        lineHeightStyle = figmaLineHeightStyle
      ),
    headlineSmall =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp * fontScalingFactor,
        lineHeight = 32.sp * lineHeightScalingFactor,
        lineHeightStyle = figmaLineHeightStyle
      ),
    titleLarge =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp * fontScalingFactor,
        lineHeight = 28.sp * lineHeightScalingFactor,
        lineHeightStyle = figmaLineHeightStyle
      ),
    titleMedium =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp * fontScalingFactor,
        lineHeight = 24.sp * lineHeightScalingFactor,
        letterSpacing = 0.15.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    titleSmall =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp * fontScalingFactor,
        lineHeight = 20.sp * lineHeightScalingFactor,
        letterSpacing = 0.1.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    bodyLarge =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp * fontScalingFactor,
        lineHeight = 24.sp * lineHeightScalingFactor,
        letterSpacing = 0.15.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    bodyMedium =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp * fontScalingFactor,
        lineHeight = 20.sp * lineHeightScalingFactor,
        letterSpacing = 0.25.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    bodySmall =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp * fontScalingFactor,
        lineHeight = 16.sp * lineHeightScalingFactor,
        letterSpacing = 0.4.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    labelLarge =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp * fontScalingFactor,
        lineHeight = 20.sp * lineHeightScalingFactor,
        letterSpacing = 0.1.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    labelMedium =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp * fontScalingFactor,
        lineHeight = 16.sp * lineHeightScalingFactor,
        letterSpacing = 0.5.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    labelSmall =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp * fontScalingFactor,
        lineHeight = 16.sp * lineHeightScalingFactor,
        letterSpacing = 0.5.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
  )

internal val Typography.bottomSheetItemLabel
  @Composable
  @ReadOnlyComposable
  get() = labelSmall.copy(fontSize = 10.dp.toSp(), lineHeight = 24.dp.toSp())

internal val GolosFontFamily: FontFamily
  @Composable
  get() =
    FontFamily(
      Font(Res.font.golos_regular, weight = FontWeight.Normal),
      Font(Res.font.golos_medium, weight = FontWeight.Medium),
      Font(Res.font.golos_bold, weight = FontWeight.Bold),
    )

internal val LoraFontFamily: FontFamily
  @Composable
  get() =
    FontFamily(
      Font(Res.font.lora_regular, weight = FontWeight.Normal),
      Font(Res.font.lora_medium, weight = FontWeight.Medium),
      Font(Res.font.lora_bold, weight = FontWeight.Bold),
    )

internal val MerriWeatherFontFamily: FontFamily
  @Composable
  get() =
    FontFamily(
      Font(Res.font.merriweather_regular, weight = FontWeight.Normal),
      Font(Res.font.merriweather_medium, weight = FontWeight.Medium),
      Font(Res.font.merriweather_bold, weight = FontWeight.Bold),
    )

internal val RobotoSerifFontFamily: FontFamily
  @Composable
  get() =
    FontFamily(
      Font(Res.font.roboto_serif_regular, weight = FontWeight.Normal),
      Font(Res.font.roboto_serif_medium, weight = FontWeight.Medium),
      Font(Res.font.robot_serif_bold, weight = FontWeight.Bold),
    )
