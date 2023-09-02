package dev.sasikanth.rss.reader.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

// Copied from : https://github.com/chrisbanes/tivi

@Composable
internal expect fun font(
  fontName: String,
  resourceId: String,
  weight: FontWeight,
  style: FontStyle = FontStyle.Normal,
): Font

internal val GolosFontFamily: FontFamily
  @Composable
  get() =
    FontFamily(
      font(fontName = "Golos", resourceId = "golos_regular", weight = FontWeight.Normal),
      font(fontName = "Golos", resourceId = "golos_medium", weight = FontWeight.Medium),
    )
