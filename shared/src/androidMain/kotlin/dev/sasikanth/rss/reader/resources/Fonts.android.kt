package dev.sasikanth.rss.reader.resources

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

// Copied from : https://github.com/chrisbanes/tivi

private val idCache = mutableMapOf<String, Int>()

@SuppressLint("DiscouragedApi")
@Composable
internal actual fun font(
  fontName: String,
  resourceId: String,
  weight: FontWeight,
  style: FontStyle
): Font {
  val context = LocalContext.current
  val id =
    idCache.getOrPut(resourceId) {
      context.resources.getIdentifier(resourceId, "fonts", context.packageName)
    }
  return Font(resId = id, weight = weight, style = style)
}
