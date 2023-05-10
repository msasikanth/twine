package dev.sasikanth.rss.reader.ui

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle

actual fun createDefaultTextStyle(): TextStyle {
  return TextStyle(
    platformStyle = PlatformTextStyle(
      includeFontPadding = false
    )
  )
}
