/*
 * Copyright 2024 Sasikanth Miriyampalli
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

@file:OptIn(ExperimentalCoilApi::class)

package dev.sasikanth.rss.reader.favicons

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
import coil3.network.CacheResponse
import coil3.network.CacheStrategy
import coil3.network.HttpException
import coil3.network.NetworkClient
import coil3.network.NetworkFetcher
import coil3.network.NetworkRequest
import coil3.network.NetworkRequestBody
import coil3.network.NetworkResponse
import coil3.network.NetworkResponseBody
import coil3.network.httpHeaders
import coil3.request.Options
import coil3.util.MimeTypeMap
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.ported.BufferReader
import okio.Buffer
import okio.FileSystem
import okio.IOException

private const val CACHE_CONTROL = "Cache-Control"
private const val CONTENT_TYPE = "Content-Type"
private const val HTTP_METHOD_GET = "GET"
private const val MIME_TYPE_TEXT_PLAIN = "text/plain"

@OptIn(InternalCoilApi::class)
class FavIconFetcher(
  private val url: String,
  private val options: Options,
  private val networkClient: Lazy<NetworkClient>,
  private val diskCache: Lazy<DiskCache?>,
  private val cacheStrategy: Lazy<CacheStrategy>,
  private val networkFetcher: (url: String) -> NetworkFetcher,
) : Fetcher {

  override suspend fun fetch(): FetchResult {
    val snapshot = readFromDiskCache()
    try {
      // Fast path: fetch the fav icon from the disk cache without performing a network request.
      var output: CacheStrategy.Output? = null
      if (snapshot != null) {
        var cacheResponse = snapshot.toCacheResponse()
        if (cacheResponse != null) {
          val input = CacheStrategy.Input(cacheResponse, newRequest(), options)
          output = cacheStrategy.value.compute(input)
          cacheResponse = output.cacheResponse
        }
        if (cacheResponse != null) {
          return SourceFetchResult(
            source = snapshot.toImageSource(),
            mimeType = getMimeType(url, cacheResponse.responseHeaders[CONTENT_TYPE]),
            dataSource = DataSource.DISK,
          )
        }
      }

      // Slow path: fetch the fav icon by parsing response HTML
      val networkRequest = output?.networkRequest ?: newRequest()
      return executeNetworkRequest(networkRequest) { response ->
        // Write the response to the disk cache then open a new snapshot.
        val responseBody = checkNotNull(response.body) { "body == null" }
        val responseBodyBuffer = responseBody.readBuffer()

        val document =
          Ksoup.parse(
            bufferReader = BufferReader(responseBodyBuffer),
            baseUri = url,
            charsetName = null
          )
        val favIconUrl = parseFaviconUrl(document) ?: fallbackFaviconUrl(url)

        return@executeNetworkRequest networkFetcher(favIconUrl).fetch()
      }
    } catch (e: Exception) {
      snapshot?.closeQuietly()
      throw e
    }
  }

  private fun parseFaviconUrl(document: Document): String? {
    val faviconUrl =
      linkRelTag(document, "apple-touch-icon")
        ?: linkRelTag(document, "apple-touch-icon-precomposed")
          ?: linkRelTag(document, "shortcut icon") ?: linkRelTag(document, "icon")

    return faviconUrl
  }

  private fun fallbackFaviconUrl(url: String): String {
    // Setting size as 180px, since that's the most commonly used apple touch icon size in the HTML,
    // if a icon is not found, it will fallback to default fav icon
    return "https://www.google.com/s2/favicons?domain=${url}&sz=180"
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
        val size = sizes.split("x")[0].toInt()
        if (size > largestSize) {
          largestSize = size
          largestSizeUrl = element.attr("abs:href")
        }
      }
    }
    return largestSizeUrl
  }

  private fun readFromDiskCache(): DiskCache.Snapshot? {
    return if (options.diskCachePolicy.readEnabled) {
      diskCache.value?.openSnapshot(diskCacheKey)
    } else {
      null
    }
  }

  private fun newRequest(url: String? = null): NetworkRequest {
    val headers = options.httpHeaders.newBuilder()
    val diskRead = options.diskCachePolicy.readEnabled
    val networkRead = options.networkCachePolicy.readEnabled
    when {
      !networkRead && diskRead -> {
        headers[CACHE_CONTROL] = "only-if-cached, max-stale=2147483647"
      }
      networkRead && !diskRead ->
        if (options.diskCachePolicy.writeEnabled) {
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
      method = options.httpMethod,
      headers = headers.build(),
      body = options.httpBody,
    )
  }

  private suspend fun <T> executeNetworkRequest(
    request: NetworkRequest,
    block: suspend (NetworkResponse) -> T,
  ): T {
    return networkClient.value.executeRequest(request) { response ->
      if (response.code !in 200 until 300 && response.code != 304) {
        throw HttpException(response)
      }
      block(response)
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

  private fun DiskCache.Snapshot.toCacheResponse(): CacheResponse? {
    return try {
      fileSystem.read(metadata) { CacheResponse(this) }
    } catch (_: IOException) {
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

  private suspend fun NetworkResponseBody.readBuffer(): Buffer = use { body ->
    val buffer = Buffer()
    body.writeTo(buffer)
    return buffer
  }

  private val httpMethodKey = Extras.Key(default = HTTP_METHOD_GET)
  private val httpBodyKey = Extras.Key<NetworkRequestBody?>(default = null)

  private val Options.httpMethod: String
    get() = getExtra(httpMethodKey)

  private val Options.httpBody: NetworkRequestBody?
    get() = getExtra(httpBodyKey)

  private val diskCacheKey: String
    get() = options.diskCacheKey ?: url

  private val fileSystem: FileSystem
    get() = diskCache.value?.fileSystem ?: options.fileSystem

  class Factory(
    networkClient: () -> NetworkClient,
    cacheStrategy: () -> CacheStrategy,
  ) : Fetcher.Factory<Uri> {

    private val networkClientLazy = lazy(networkClient)
    private val cacheStrategyLazy = lazy(cacheStrategy)

    override fun create(
      data: Uri,
      options: Options,
      imageLoader: ImageLoader,
    ): Fetcher? {
      if (!isApplicable(data)) return null
      val diskCacheLazy = lazy { imageLoader.diskCache }

      return FavIconFetcher(
        url = data.toString(),
        options = options,
        networkClient = networkClientLazy,
        diskCache = diskCacheLazy,
        cacheStrategy = cacheStrategyLazy,
        networkFetcher = { url ->
          NetworkFetcher(
            url = url,
            options = options.copy(diskCacheKey = data.toString()),
            networkClient = networkClientLazy,
            diskCache = diskCacheLazy,
            cacheStrategy = cacheStrategyLazy,
          )
        }
      )
    }

    private fun isApplicable(data: Uri): Boolean {
      return data.scheme == "http" || data.scheme == "https"
    }
  }
}
