package dev.sasikanth.rss.reader.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.compose.fontFamilyResource
import dev.sasikanth.rss.reader.CommonRes

private val figmaLineHeightStyle = LineHeightStyle(
  alignment = LineHeightStyle.Alignment.Center,
  trim = LineHeightStyle.Trim.None
)

fun typography(fontFamily: FontFamily) = Typography(
  displayLarge = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  displayMedium = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  displaySmall = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  headlineLarge = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  headlineMedium = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  headlineSmall = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  titleLarge = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  titleMedium = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  titleSmall = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  bodyLarge = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  bodyMedium = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  bodySmall = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  labelLarge = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  labelMedium = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
  labelSmall = TextStyle.Default.copy(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
    lineHeightStyle = figmaLineHeightStyle
  ),
)
