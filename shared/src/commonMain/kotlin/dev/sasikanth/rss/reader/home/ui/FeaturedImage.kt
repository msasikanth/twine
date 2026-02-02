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

package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass

@Composable
fun FeaturedImage(
  imageUrl: String?,
  modifier: Modifier = Modifier,
  alignment: Alignment = Alignment.Center,
  unlockAspectRatio: Boolean = false,
) {
  val sizeClass = LocalWindowSizeClass.current
  val imageMaxHeight =
    when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 360.dp
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 250.dp
      else -> Dp.Unspecified
    }

  val adaptiveImageModifier =
    if (unlockAspectRatio) {
      Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small)
    } else {
      Modifier.aspectRatio(16f / 9f)
        .heightIn(max = imageMaxHeight)
        .clip(MaterialTheme.shapes.extraLarge)
    }
  val adaptiveContentScale =
    if (unlockAspectRatio) {
      ContentScale.FillWidth
    } else {
      widthBiasedScale
    }

  imageUrl?.let { imageUrl ->
    val translucentStyle = LocalTranslucentStyles.current

    AsyncImage(
      url = imageUrl,
      modifier =
        Modifier.then(adaptiveImageModifier)
          .background(translucentStyle.prominent.background)
          .then(modifier),
      contentDescription = null,
      contentScale = adaptiveContentScale,
      alignment = alignment,
    )
  }
}

private val widthBiasedScale =
  object : ContentScale {
    override fun computeScaleFactor(srcSize: Size, dstSize: Size): ScaleFactor {
      val heightScale = dstSize.height / srcSize.height
      val widthScale = dstSize.width / srcSize.width
      val scale = maxOf(heightScale, widthScale * 1.2f)

      return ScaleFactor(scale, scale)
    }
  }
