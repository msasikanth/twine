package dev.sasikanth.rss.reader.markdown

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.model.PlaceholderConfig

class CoilMarkdownTransformer(
  private val shouldBlockImage: Boolean,
) : ImageTransformer {

  @Composable
  override fun transform(link: String): ImageData? {
    if (shouldBlockImage) return null

    return rememberAsyncImagePainter(
        model =
          ImageRequest.Builder(LocalPlatformContext.current)
            .data(link)
            .size(coil3.size.Size.ORIGINAL)
            .build()
      )
      .let {
        ImageData(
          painter = it,
          modifier = Modifier.padding(vertical = 8.dp).clip(MaterialTheme.shapes.large),
          alignment = Alignment.Center,
        )
      }
  }

  @Composable
  override fun intrinsicSize(painter: Painter): Size {
    if (shouldBlockImage) return Size.Zero

    var size by remember(painter) { mutableStateOf(painter.intrinsicSize) }
    if (painter is AsyncImagePainter) {
      val painterState = painter.state.collectAsState()
      val intrinsicSize = painterState.value.painter?.intrinsicSize
      intrinsicSize?.also { size = it }
    }
    return size
  }

  override fun placeholderConfig(
    density: Density,
    containerSize: Size,
    intrinsicImageSize: Size
  ): PlaceholderConfig {
    if (shouldBlockImage) {
      return PlaceholderConfig(size = Size.Zero)
    }

    return super.placeholderConfig(density, containerSize, intrinsicImageSize)
  }
}
