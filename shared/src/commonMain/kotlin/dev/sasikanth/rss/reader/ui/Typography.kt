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
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

expect fun createDefaultTextStyle(): TextStyle

private val figmaLineHeightStyle =
  LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None)

@Stable val defaultTextStyle = createDefaultTextStyle()

fun typography(fontFamily: FontFamily) =
  Typography(
    displayLarge =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    displayMedium =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    displaySmall =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    headlineLarge =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    headlineMedium =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    headlineSmall =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    titleLarge =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    titleMedium =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    titleSmall =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    bodyLarge =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    bodyMedium =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    bodySmall =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.4.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    labelLarge =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    labelMedium =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
    labelSmall =
      defaultTextStyle.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        lineHeightStyle = figmaLineHeightStyle
      ),
  )
