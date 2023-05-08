package dev.sasikanth.rss.reader.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.github.aakira.napier.log
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.util.toMap
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import platform.Foundation.NSCachedURLResponse
import platform.Foundation.NSData
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.NSURLCache
import platform.Foundation.NSURLRequest
import platform.Foundation.create

@Composable
actual fun AsyncImage(
  url: String,
  contentDescription: String?,
  contentScale: ContentScale,
  modifier: Modifier,
) {
  val imageState by rememberImageLoaderState(url)

  Box(modifier) {
    when (imageState) {
      is ImageLoaderState.Loaded -> {
        Image(
          modifier = Modifier.matchParentSize(),
          bitmap = (imageState as ImageLoaderState.Loaded).image,
          contentDescription = contentDescription,
          contentScale = contentScale
        )
      }

      else -> {
        // TODO: Handle other cases instead of just showing blank space?
      }
    }
  }
}

@Composable
private fun rememberImageLoaderState(url: String?): State<ImageLoaderState> {
  val initialState = if (url.isNullOrBlank()) {
    ImageLoaderState.Error
  } else {
    ImageLoaderState.Loading
  }

  return produceState(initialState, url) {
    value = try {
      ImageLoaderState.Loaded(ImageLoader.getImage(url!!)!!)
    } catch (e: Exception) {
      ImageLoaderState.Error
    }
  }
}

private sealed interface ImageLoaderState {
  object Idle : ImageLoaderState
  object Loading : ImageLoaderState
  data class Loaded(val image: ImageBitmap) : ImageLoaderState
  object Error : ImageLoaderState
}

private object ImageLoader {

  private val memoryCacheSize = (10 * 1024 * 1024).toULong() // 10 MB cache size
  private val diskCacheSize = (50 * 1024 * 1024).toULong() // 50 MB cache size
  private val httpClient = HttpClient(Darwin) {
    engine {
      configureRequest {
        setTimeoutInterval(60.0)
        setAllowsCellularAccess(true)
      }
    }
  }
  private val urlCache = NSURLCache(
    memoryCapacity = memoryCacheSize,
    diskCapacity = diskCacheSize,
    diskPath = "dev_sasikanth_rss_reader_images_cache"
  )

  suspend fun getImage(url: String): ImageBitmap? {
    return withContext(Dispatchers.IO) {
      val cachedImage = loadCachedImage(url)
      val data = if (cachedImage != null) {
        cachedImage
      } else {
        downloadImage(url) ?: return@withContext null
      }

      return@withContext Image.makeFromEncoded(data).toComposeImageBitmap()
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
      val cachedResponse = createCachedResponse(
        httpResponse = response,
        data = data,
        requestUrl = url
      )
      urlCache.storeCachedResponse(
        cachedResponse = cachedResponse,
        forRequest = request
      )
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
    val headers = httpResponse.headers
      .toMap()
      .mapValues { it.value.joinToString(", ") }

    val url = NSURL(string = requestUrl)
    val response = NSHTTPURLResponse(
      uRL = url,
      statusCode = statusCode.toLong(),
      HTTPVersion = httpResponse.version.toString(),
      headerFields = headers as Map<Any?, *>
    )
    val nsData = data.toNSData()

    return NSCachedURLResponse(
      response = response,
      data = nsData
    )
  }

  private fun ByteArray.toNSData(): NSData = this.usePinned {
    NSData.create(bytes = it.addressOf(0), length = this.size.convert())
  }
}
