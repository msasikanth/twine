package dev.sasikanth.rss.reader.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

// Copied from : https://github.com/chrisbanes/tivi

private val cache: MutableMap<String, Font> = mutableMapOf()

@OptIn(ExperimentalResourceApi::class)
@Composable
internal actual fun font(
  fontName: String,
  resourceId: String,
  weight: FontWeight,
  style: FontStyle
): Font {
  return cache.getOrPut(resourceId) {
    val byteArray = runBlocking { resource("fonts/$resourceId.ttf").readBytes() }
    androidx.compose.ui.text.platform.Font(resourceId, byteArray, weight, style)
  }
}
