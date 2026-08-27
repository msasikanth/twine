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

package dev.sasikanth.rss.reader.components.image

import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import coil3.request.ImageResult
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils

/**
 * Pre HD YouTube uploads have no `maxresdefault` thumbnail and respond with a 404, so retry those
 * with the smaller variant that every video has.
 */
internal class YouTubeThumbnailInterceptor : Interceptor {

  override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
    val result = chain.proceed()
    if (result !is ErrorResult) return result

    val fallbackUrl =
      UrlUtils.youTubeThumbnailFallback(chain.request.data as? String) ?: return result

    val fallbackRequest =
      chain.request
        .newBuilder()
        .data(fallbackUrl)
        .diskCacheKey(fallbackUrl)
        .memoryCacheKey(fallbackUrl)
        .build()

    return chain.withRequest(fallbackRequest).proceed()
  }
}
