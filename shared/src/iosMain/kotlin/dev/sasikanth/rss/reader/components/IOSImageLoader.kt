/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalForeignApi::class)

package dev.sasikanth.rss.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import dev.sasikanth.rss.reader.di.scopes.AppScope
import io.github.aakira.napier.log
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.util.toMap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode
import platform.Foundation.NSCachedURLResponse
import platform.Foundation.NSData
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.NSURLCache
import platform.Foundation.NSURLRequest
import platform.Foundation.create

@Composable
internal fun rememberImageLoaderState(url: String?): State<ImageLoaderState> {
  val initialState =
    if (url.isNullOrBlank()) {
      ImageLoaderState.Error
    } else {
      ImageLoaderState.Loading
    }
  val imageLoader = LocalImageLoader.current

  return produceState(initialState, url) {
    value =
      try {
        ImageLoaderState.Loaded(imageLoader?.getImage(url!!, size = null)!!)
      } catch (e: Exception) {
        ImageLoaderState.Error
      }
  }
}

internal sealed interface ImageLoaderState {
  object Idle : ImageLoaderState

  object Loading : ImageLoaderState

  data class Loaded(val image: ImageBitmap) : ImageLoaderState

  object Error : ImageLoaderState
}

@Inject
@AppScope
class IOSImageLoader : ImageLoader {

  private val memoryCacheSize = (10 * 1024 * 1024).toULong() // 10 MB cache size
  private val diskCacheSize = (50 * 1024 * 1024).toULong() // 50 MB cache size
  private val httpClient =
    HttpClient(Darwin) {
      engine {
        configureRequest {
          setTimeoutInterval(60.0)
          setAllowsCellularAccess(true)
        }
      }
    }
  private val urlCache =
    NSURLCache(
      memoryCapacity = memoryCacheSize,
      diskCapacity = diskCacheSize,
      diskPath = "dev_sasikanth_rss_reader_images_cache"
    )

  override suspend fun getImage(url: String, size: Int?): ImageBitmap? {
    return withContext(Dispatchers.IO) {
      val cachedImage = loadCachedImage(url)
      val data =
        if (cachedImage != null) {
          cachedImage
        } else {
          downloadImage(url) ?: return@withContext null
        }

      return@withContext Image.makeFromEncoded(data).toBitmap(size).asComposeImageBitmap()
    }
  }

  private fun hasImageCache(url: String): Boolean {
    val request = createNSURLRequest(url) ?: return false
    return urlCache.cachedResponseForRequest(request) != null
  }

  private fun loadCachedImage(url: String): ByteArray? {
    log { "Loading image from cache: $url" }
    val request = createNSURLRequest(url) ?: return null

    urlCache.cachedResponseForRequest(request)?.let { cachedResponse ->
      val bytes = cachedResponse.data.bytes
      return bytes?.readBytes(cachedResponse.data.length.toInt())
    }

    return null
  }

  private suspend fun downloadImage(url: String): ByteArray? {
    log { "Downloading image: $url" }
    val request = createNSURLRequest(url) ?: return null
    val response = httpClient.get(url)

    return response.readBytes().also { data ->
      val cachedResponse =
        createCachedResponse(httpResponse = response, data = data, requestUrl = url)
      urlCache.storeCachedResponse(cachedResponse = cachedResponse, forRequest = request)
    }
  }

  private fun createNSURLRequest(url: String): NSURLRequest? {
    val nsUrl = NSURL.URLWithString(url) ?: return null
    return NSURLRequest.requestWithURL(nsUrl)
  }

  @Suppress("UNCHECKED_CAST")
  private fun createCachedResponse(
    httpResponse: HttpResponse,
    data: ByteArray,
    requestUrl: String
  ): NSCachedURLResponse {
    val statusCode = httpResponse.status.value
    val headers = httpResponse.headers.toMap().mapValues { it.value.joinToString(", ") }

    val url = NSURL(string = requestUrl)
    val response =
      NSHTTPURLResponse(
        uRL = url,
        statusCode = statusCode.toLong(),
        HTTPVersion = httpResponse.version.toString(),
        headerFields = headers as Map<Any?, *>
      )
    val nsData = data.toNSData()

    return NSCachedURLResponse(response = response, data = nsData)
  }

  private fun ByteArray.toNSData(): NSData =
    this.usePinned { NSData.create(bytes = it.addressOf(0), length = this.size.convert()) }
}

private fun Image.toBitmap(size: Int? = null): Bitmap {
  val width = size ?: this.width
  val height = size ?: this.height
  val bitmap = Bitmap()
  bitmap.allocPixels(ImageInfo.makeN32(width, height, ColorAlphaType.PREMUL))
  val canvas = Canvas(bitmap)
  canvas.drawImageRect(
    this,
    Rect.makeWH(this.width.toFloat(), this.height.toFloat()),
    Rect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat()),
    SamplingMode.DEFAULT,
    null,
    true
  )
  bitmap.setImmutable()
  return bitmap
}
