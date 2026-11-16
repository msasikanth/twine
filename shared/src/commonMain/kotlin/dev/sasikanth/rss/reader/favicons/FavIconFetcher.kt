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

@file:OptIn(ExperimentalCoilApi::class)

package dev.sasikanth.rss.reader.favicons

import androidx.collection.lruCache
import coil3.Extras
import coil3.ImageLoader
import coil3.Uri
import coil3.annotation.ExperimentalCoilApi
import coil3.annotation.InternalCoilApi
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.disk.DiskCache
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.getExtra
import coil3.intercept.Interceptor
import coil3.network.CacheNetworkResponse
import coil3.network.CacheStrategy
import coil3.network.ConnectivityChecker
import coil3.network.DeDupeConcurrentRequestStrategy
import coil3.network.HttpException
import coil3.network.NetworkClient
import coil3.network.NetworkFetcher
import coil3.network.NetworkHeaders
import coil3.network.NetworkRequest
import coil3.network.NetworkRequestBody
import coil3.network.NetworkResponse
import coil3.network.httpHeaders
import coil3.request.ImageResult
import coil3.request.Options
import coil3.size.Dimension
import coil3.size.Size
import coil3.util.MimeTypeMap
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.parseSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.okio.asKotlinxIoRawSource
import okio.FileSystem
import okio.IOException as OkioIOException
import okio.buffer

private const val CACHE_CONTROL = "Cache-Control"
private const val CONTENT_TYPE = "Content-Type"
private const val HTTP_METHOD_GET = "GET"
private const val HTTP_RESPONSE_NOT_MODIFIED = 304
private const val MIME_TYPE_TEXT_PLAIN = "text/plain"
private const val MAX_HTML_READ_SIZE = 64 * 1024L // 64 KB
internal const val DEFAULT_FAVICON_SIZE_PX = 192

private val httpMethodKey = Extras.Key(default = HTTP_METHOD_GET)
private val httpBodyKey = Extras.Key<NetworkRequestBody?>(default = null)

internal data class FavIconResolution(val url: String, val cacheable: Boolean = true)

internal data class NormalizedFavIconParams(val size: Size, val memoryCacheKey: String)

internal fun normalizeFavIconParams(
  memoryCacheKey: String?,
  diskCacheKey: String?,
  dataKey: String,
  resolvedSize: Size,
): NormalizedFavIconParams {
  val targetSize =
    Size(
      width = resolvedSize.width.coerceAtLeastFavIconMin(),
      height = resolvedSize.height.coerceAtLeastFavIconMin(),
    )
  val resolvedSizeSuffix = "#${resolvedSize.width}x${resolvedSize.height}"
  val targetSizeSuffix = "#${targetSize.width}x${targetSize.height}"
  val targetMemoryCacheKey =
    when {
      memoryCacheKey == null -> "${diskCacheKey ?: dataKey}$targetSizeSuffix"
      memoryCacheKey.endsWith(targetSizeSuffix) -> memoryCacheKey
      memoryCacheKey.endsWith(resolvedSizeSuffix) ->
        "${memoryCacheKey.removeSuffix(resolvedSizeSuffix)}$targetSizeSuffix"
      else -> "$memoryCacheKey$targetSizeSuffix"
    }
  return NormalizedFavIconParams(size = targetSize, memoryCacheKey = targetMemoryCacheKey)
}

private fun Dimension.coerceAtLeastFavIconMin(): Dimension {
  return when (this) {
    is Dimension.Pixels -> Dimension(px.coerceAtLeast(DEFAULT_FAVICON_SIZE_PX))
    Dimension.Undefined -> Dimension.Undefined
  }
}

@OptIn(InternalCoilApi::class)
class FavIconFetcher
internal constructor(
  private val url: String,
  private val diskCacheKey: String = url,
  private val diskReadEnabled: Boolean = true,
  private val diskWriteEnabled: Boolean = true,
  private val networkReadEnabled: Boolean = true,
  private val httpHeaders: NetworkHeaders = NetworkHeaders.EMPTY,
  private val httpMethod: String = HTTP_METHOD_GET,
  private val httpBody: NetworkRequestBody? = null,
  private val fallbackFileSystem: FileSystem? = null,
  private val networkClient: Lazy<NetworkClient>,
  private val diskCache: Lazy<DiskCache?>,
  private val readFromCacheStrategy:
    suspend (
      cacheResponse: NetworkResponse, networkRequest: NetworkRequest,
    ) -> CacheStrategy.ReadResult =
    { cacheResponse, _ ->
      CacheStrategy.ReadResult(cacheResponse)
    },
  private val fetchFavIcon: suspend (url: String) -> FetchResult,
  private val favIconUrlResolver:
    suspend (key: String, resolve: suspend () -> FavIconResolution) -> String =
    { _, resolve ->
      resolve().url
    },
) : Fetcher {

  constructor(
    url: String,
    options: Options,
    networkClient: Lazy<NetworkClient>,
    diskCache: Lazy<DiskCache?>,
    cacheStrategy: Lazy<CacheStrategy>,
    networkFetcher: (url: String) -> NetworkFetcher,
  ) : this(
    url = url,
    diskCacheKey = options.diskCacheKey ?: url,
    diskReadEnabled = options.diskCachePolicy.readEnabled,
    diskWriteEnabled = options.diskCachePolicy.writeEnabled,
    networkReadEnabled = options.networkCachePolicy.readEnabled,
    httpHeaders = options.httpHeaders,
    httpMethod = options.getExtra(httpMethodKey),
    httpBody = options.getExtra(httpBodyKey),
    fallbackFileSystem = options.fileSystem,
    networkClient = networkClient,
    diskCache = diskCache,
    readFromCacheStrategy = { cacheResponse, networkRequest ->
      cacheStrategy.value.read(cacheResponse, networkRequest, options)
    },
    fetchFavIcon = { favIconUrl -> networkFetcher(favIconUrl).fetch() },
  )

  override suspend fun fetch(): FetchResult {
    var snapshot = readFromDiskCache()
    try {
      // Fast path: fetch the fav icon from the disk cache without performing a network request.
      var output: CacheStrategy.ReadResult? = null
      if (snapshot != null) {
        var cacheResponse = snapshot.toCacheResponse()
        if (cacheResponse != null) {
          output = readFromCacheStrategy(cacheResponse, newRequest())
          cacheResponse = output.response
        }
        if (cacheResponse != null) {
          throwIfFailureResponseCode(cacheResponse)
          return SourceFetchResult(
            source = snapshot.toImageSource(),
            mimeType = getMimeType(url, cacheResponse.headers[CONTENT_TYPE]),
            dataSource = DataSource.DISK,
          )
        }
        snapshot.closeQuietly()
        snapshot = null
      }

      // Slow path: fetch the fav icon by parsing response HTML
      val networkRequest = output?.request ?: newRequest()
      val favIconUrl =
        favIconUrlResolver(diskCacheKey) {
          val okioBuffer =
            try {
              executeNetworkRequest(networkRequest) { response ->
                val responseBody = checkNotNull(response.body) { "body == null" }
                val buffer = okio.Buffer()
                val sink =
                  object : okio.Sink {
                      var bytesWritten = 0L
                      var limitReached = false

                      override fun write(source: okio.Buffer, byteCount: Long) {
                        if (limitReached) {
                          source.skip(byteCount)
                          return
                        }

                        val toWrite = minOf(byteCount, MAX_HTML_READ_SIZE - bytesWritten)
                        if (toWrite > 0) {
                          buffer.write(source, toWrite)
                          bytesWritten += toWrite
                        }

                        if (bytesWritten >= MAX_HTML_READ_SIZE) {
                          limitReached = true
                          throw LimitExceededException()
                        }
                      }

                      override fun flush() {}

                      override fun timeout() = okio.Timeout.NONE

                      override fun close() {}
                    }
                    .buffer()

                try {
                  responseBody.use { it.writeTo(sink) }
                } catch (_: LimitExceededException) {
                  // Success, we reached the limit
                } finally {
                  sink.close()
                }

                buffer
              }
            } catch (e: CancellationException) {
              throw e
            } catch (_: Exception) {
              null
            }

          if (okioBuffer != null) {
            val resolvedUrl =
              if (okioBuffer.size > 0) {
                withContext(Dispatchers.Default) {
                  val document =
                    Ksoup.parseSource(source = okioBuffer.asKotlinxIoRawSource(), baseUri = url)
                  parseFaviconUrl(document) ?: fallbackFaviconUrl(url)
                }
              } else {
                fallbackFaviconUrl(url)
              }
            FavIconResolution(url = resolvedUrl, cacheable = true)
          } else {
            FavIconResolution(url = fallbackFaviconUrl(url), cacheable = false)
          }
        }

      return fetchFavIcon(favIconUrl)
    } catch (e: Exception) {
      snapshot?.closeQuietly()
      throw e
    }
  }

  private class LimitExceededException : OkioIOException()

  private fun parseFaviconUrl(document: Document): String? {
    val faviconUrl =
      linkRelTag(document, "apple-touch-icon")
        ?: linkRelTag(document, "apple-touch-icon-precomposed")
        ?: linkRelTag(document, "shortcut icon")
        ?: linkRelTag(document, "icon")

    return faviconUrl
  }

  private fun fallbackFaviconUrl(url: String): String {
    val domain =
      url.removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore("/")
    return "https://www.google.com/s2/favicons?domain=${domain}&sz=180"
  }

  /** From Unfurl https://github.com/saket/unfurl */
  private fun linkRelTag(document: Document, rel: String): String? {
    val elements = document.head().select("link[rel=$rel]")
    var largestSizeUrl = elements.firstOrNull()?.attr("abs:href") ?: return null
    var largestSize = 0

    for (element in elements) {
      // Some websites have multiple icons for different sizes. Find the largest one.
      val sizes = element.attr("sizes")
      if (sizes.contains("x")) {
        val size = sizes.split("x")[0].toIntOrNull()
        if (size != null && size > largestSize) {
          largestSize = size
          largestSizeUrl = element.attr("abs:href")
        }
      }
    }
    return largestSizeUrl
  }

  private fun readFromDiskCache(): DiskCache.Snapshot? {
    return if (diskReadEnabled) {
      diskCache.value?.openSnapshot(diskCacheKey)
    } else {
      null
    }
  }

  private fun newRequest(url: String? = null): NetworkRequest {
    val headers = httpHeaders.newBuilder()
    headers["User-Agent"] =
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    val diskRead = diskReadEnabled
    val networkRead = networkReadEnabled
    when {
      !networkRead && diskRead -> {
        headers[CACHE_CONTROL] = "only-if-cached, max-stale=2147483647"
      }
      networkRead && !diskRead ->
        if (diskWriteEnabled) {
          headers[CACHE_CONTROL] = "no-cache"
        } else {
          headers[CACHE_CONTROL] = "no-cache, no-store"
        }
      !networkRead && !diskRead -> {
        // This causes the request to fail with a 504 Unsatisfiable Request.
        headers[CACHE_CONTROL] = "no-cache, only-if-cached"
      }
    }

    return NetworkRequest(
      url = url ?: this.url,
      method = httpMethod,
      headers = headers.build(),
      body = httpBody,
    )
  }

  private suspend fun <T> executeNetworkRequest(
    request: NetworkRequest,
    block: suspend (NetworkResponse) -> T,
  ): T {
    return networkClient.value.executeRequest(request) { response ->
      throwIfFailureResponseCode(response)
      block(response)
    }
  }

  private fun throwIfFailureResponseCode(response: NetworkResponse) {
    if (response.code !in 200 until 300 && response.code != HTTP_RESPONSE_NOT_MODIFIED) {
      throw HttpException(response)
    }
  }

  /**
   * Parse the response's `content-type` header.
   *
   * "text/plain" is often used as a default/fallback MIME type. Attempt to guess a better MIME type
   * from the file extension.
   */
  @InternalCoilApi
  private fun getMimeType(url: String, contentType: String?): String? {
    if (contentType == null || contentType.startsWith(MIME_TYPE_TEXT_PLAIN)) {
      MimeTypeMap.getMimeTypeFromUrl(url)?.let {
        return it
      }
    }
    return contentType?.substringBefore(';')
  }

  private fun DiskCache.Snapshot.toCacheResponse(): NetworkResponse? {
    return try {
      fileSystem.read(metadata) { CacheNetworkResponse.readFrom(this) }
    } catch (_: Exception) {
      // If we can't parse the metadata, ignore this entry.
      null
    }
  }

  private fun DiskCache.Snapshot.toImageSource(): ImageSource {
    return ImageSource(
      file = data,
      fileSystem = fileSystem,
      diskCacheKey = diskCacheKey,
      closeable = this,
    )
  }

  private fun AutoCloseable.closeQuietly() {
    try {
      close()
    } catch (e: RuntimeException) {
      throw e
    } catch (_: Exception) {}
  }

  private val fileSystem: FileSystem
    get() = diskCache.value?.fileSystem ?: checkNotNull(fallbackFileSystem)

  class Factory(networkClient: () -> NetworkClient, cacheStrategy: () -> CacheStrategy) :
    Fetcher.Factory<Uri> {

    private val networkClientLazy = lazy(networkClient)
    private val cacheStrategyLazy = lazy(cacheStrategy)
    private val connectivityCheckerLazy = lazy { ConnectivityChecker.ONLINE }
    private val concurrentRequestStrategyLazy = lazy { DeDupeConcurrentRequestStrategy() }
    private val resolverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val resolverMutex = Mutex()
    private val resolvedFavIconUrls = lruCache<String, String>(maxSize = 256)
    private val inFlightResolutions = mutableMapOf<String, Deferred<FavIconResolution>>()

    internal suspend fun resolveFavIconUrl(
      key: String,
      resolve: suspend () -> FavIconResolution,
    ): String {
      val deferred =
        resolverMutex.withLock {
          resolvedFavIconUrls[key]?.let {
            return it
          }
          inFlightResolutions[key]
            ?: resolverScope
              .async {
                try {
                  val resolution = resolve()
                  if (resolution.cacheable) {
                    resolverMutex.withLock { resolvedFavIconUrls.put(key, resolution.url) }
                  }
                  resolution
                } finally {
                  resolverMutex.withLock { inFlightResolutions.remove(key) }
                }
              }
              .also { inFlightResolutions[key] = it }
        }
      return deferred.await().url
    }

    override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
      if (!isApplicable(data)) return null
      val diskCacheLazy = lazy { imageLoader.diskCache }
      val targetUrl = data.toString()

      return FavIconFetcher(
        url = targetUrl,
        diskCacheKey = options.diskCacheKey ?: targetUrl,
        diskReadEnabled = options.diskCachePolicy.readEnabled,
        diskWriteEnabled = options.diskCachePolicy.writeEnabled,
        networkReadEnabled = options.networkCachePolicy.readEnabled,
        httpHeaders = options.httpHeaders,
        httpMethod = options.getExtra(httpMethodKey),
        httpBody = options.getExtra(httpBodyKey),
        fallbackFileSystem = options.fileSystem,
        networkClient = networkClientLazy,
        diskCache = diskCacheLazy,
        readFromCacheStrategy = { cacheResponse, networkRequest ->
          cacheStrategyLazy.value.read(cacheResponse, networkRequest, options)
        },
        fetchFavIcon = { url ->
          NetworkFetcher(
              url = url,
              options = options.copy(diskCacheKey = targetUrl),
              networkClient = networkClientLazy,
              diskCache = diskCacheLazy,
              cacheStrategy = cacheStrategyLazy,
              connectivityChecker = connectivityCheckerLazy,
              concurrentRequestStrategy = concurrentRequestStrategyLazy,
            )
            .fetch()
        },
        favIconUrlResolver = ::resolveFavIconUrl,
      )
    }

    private fun isApplicable(data: Uri): Boolean {
      return data.scheme == "http" || data.scheme == "https"
    }
  }

  object SizeInterceptor : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
      val request = chain.request
      val resolvedSize = chain.size
      val normalized =
        normalizeFavIconParams(
          memoryCacheKey = request.memoryCacheKey,
          diskCacheKey = request.diskCacheKey,
          dataKey = request.data.toString(),
          resolvedSize = resolvedSize,
        )
      if (normalized.size == resolvedSize && normalized.memoryCacheKey == request.memoryCacheKey) {
        return chain.proceed()
      }
      val updatedRequest =
        request.newBuilder().size(normalized.size).memoryCacheKey(normalized.memoryCacheKey).build()
      return chain.withRequest(updatedRequest).withSize(normalized.size).proceed()
    }
  }
}
