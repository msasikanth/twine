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

package dev.sasikanth.rss.reader.markdown

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import dev.sasikanth.rss.reader.reader.ui.LocalOnImageClick

object CoilMarkdownTransformer : ImageTransformer {

  @Composable
  override fun transform(link: String): ImageData? {
    val onImageClick = LocalOnImageClick.current
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
          modifier =
            Modifier.padding(vertical = 8.dp).clip(MaterialTheme.shapes.large).clickable {
              onImageClick(link)
            },
          alignment = Alignment.Center,
        )
      }
  }

  @Composable
  override fun intrinsicSize(painter: Painter): Size {
    var size by remember(painter) { mutableStateOf(painter.intrinsicSize) }
    if (painter is AsyncImagePainter) {
      val painterState = painter.state.collectAsState()
      val intrinsicSize = painterState.value.painter?.intrinsicSize
      intrinsicSize?.also { size = it }
    }
    return size
  }
}
