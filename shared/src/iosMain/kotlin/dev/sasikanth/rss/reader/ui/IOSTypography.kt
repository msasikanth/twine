package dev.sasikanth.rss.reader.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.PlatformParagraphStyle
import androidx.compose.ui.text.PlatformSpanStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle

@Stable
actual fun createDefaultTextStyle(): TextStyle {
  return TextStyle(
    platformStyle = PlatformTextStyle(
      spanStyle = PlatformSpanStyle.Default,
      paragraphStyle = PlatformParagraphStyle.Default
    )
  )
}
