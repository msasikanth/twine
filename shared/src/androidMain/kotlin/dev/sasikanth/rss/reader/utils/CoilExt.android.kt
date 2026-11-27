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

package dev.sasikanth.rss.reader.utils

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import coil3.BitmapImage
import coil3.Image
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.asDrawable
import coil3.decode.BitmapFactoryDecoder
import coil3.decode.Decoder
import coil3.decode.StaticImageDecoder
import coil3.fetch.SourceFetchResult
import coil3.request.ImageRequest
import coil3.request.Options
import coil3.svg.SvgDecoder

@OptIn(ExperimentalCoilApi::class)
actual fun Image.toComposeImageBitmap(context: PlatformContext): ImageBitmap {
  if (this is BitmapImage) {
    val src = bitmap
    val readable =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && src.config == Bitmap.Config.HARDWARE) {
        src.copy(Bitmap.Config.ARGB_8888, false) ?: src
      } else {
        src
      }
    return readable.asImageBitmap()
  }
  return asDrawable(context.resources).toBitmap().asImageBitmap()
}

private object StaticFirstFrameDecoderFactory : Decoder.Factory {
  private val svgFactory = SvgDecoder.Factory()
  private val staticFactory: Decoder.Factory? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      StaticImageDecoder.Factory()
    } else {
      null
    }
  private val bitmapFactory = BitmapFactoryDecoder.Factory()

  override fun create(
    result: SourceFetchResult,
    options: Options,
    imageLoader: ImageLoader,
  ): Decoder? {
    svgFactory.create(result, options, imageLoader)?.let {
      return it
    }
    staticFactory?.create(result, options, imageLoader)?.let {
      return it
    }
    return bitmapFactory.create(result, options, imageLoader)
  }
}

actual fun ImageRequest.Builder.disableAnimation(): ImageRequest.Builder =
  decoderFactory(StaticFirstFrameDecoderFactory).memoryCacheKeyExtra("disable_animation", "true")
