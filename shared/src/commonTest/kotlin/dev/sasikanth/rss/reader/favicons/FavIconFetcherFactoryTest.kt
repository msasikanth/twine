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

@file:OptIn(ExperimentalCoilApi::class, InternalCoilApi::class)

package dev.sasikanth.rss.reader.favicons

import coil3.annotation.ExperimentalCoilApi
import coil3.annotation.InternalCoilApi
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.disk.DiskCache
import coil3.fetch.SourceFetchResult
import coil3.network.CacheNetworkResponse
import coil3.network.CacheStrategy
import coil3.network.HttpException
import coil3.network.NetworkClient
import coil3.network.NetworkHeaders
import coil3.network.NetworkRequest
import coil3.network.NetworkResponse
import coil3.network.NetworkResponseBody
import coil3.size.Size
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.FileSystem
import okio.IOException as OkioIOException

class FavIconFetcherFactoryTest {

  private fun createFactory(): FavIconFetcher.Factory {
    val stubClient =
      object : NetworkClient {
        override suspend fun <T> executeRequest(
          request: NetworkRequest,
          block: suspend (NetworkResponse) -> T,
        ): T {
          throw UnsupportedOperationException()
        }
      }
    return FavIconFetcher.Factory(
      networkClient = { stubClient },
      cacheStrategy = { CacheStrategy.DEFAULT },
    )
  }

  @Test
  fun resolveFavIconUrl_deduplicatesConcurrentRequestsAndCachesResult() = runTest {
    val factory = createFactory()
    val entered = CompletableDeferred<Unit>()
    val gate = CompletableDeferred<Unit>()
    val countMutex = Mutex()
    var invocationCount = 0

    val results =
      withContext(Dispatchers.Default) {
        val deferreds =
          (1..12).map {
            async {
              factory.resolveFavIconUrl("https://example.com") {
                countMutex.withLock { invocationCount++ }
                entered.complete(Unit)
                gate.await()
                FavIconResolution("https://example.com/apple-touch-icon.png")
              }
            }
          }

        entered.await()
        delay(25)
        gate.complete(Unit)
        deferreds.awaitAll()
      }

    assertTrueAllEqual("https://example.com/apple-touch-icon.png", results)
    assertEquals(1, invocationCount)

    val cached =
      factory.resolveFavIconUrl("https://example.com") {
        countMutex.withLock { invocationCount++ }
        FavIconResolution("https://example.com/other.png")
      }
    assertEquals("https://example.com/apple-touch-icon.png", cached)
    assertEquals(1, invocationCount)
  }

  @Test
  fun resolveFavIconUrl_cleansUpInFlightOnFailureAllowingRetry() = runTest {
    val factory = createFactory()
    var attempts = 0

    assertFailsWith<IllegalStateException> {
      factory.resolveFavIconUrl("https://retry.example.com") {
        attempts++
        throw IllegalStateException("network failure")
      }
    }

    val recovered =
      factory.resolveFavIconUrl("https://retry.example.com") {
        attempts++
        FavIconResolution("https://retry.example.com/favicon.ico")
      }

    assertEquals("https://retry.example.com/favicon.ico", recovered)
    assertEquals(2, attempts)
  }

  @Test
  fun resolveFavIconUrl_doesNotCacheUncacheableFallbackAllowingRetry() = runTest {
    val factory = createFactory()
    var attempts = 0

    val offlineFallback =
      factory.resolveFavIconUrl("https://offline.example.com") {
        attempts++
        FavIconResolution(
          url = "https://www.google.com/s2/favicons?domain=offline.example.com&sz=192",
          cacheable = false,
        )
      }
    assertEquals(
      "https://www.google.com/s2/favicons?domain=offline.example.com&sz=192",
      offlineFallback,
    )
    assertEquals(1, attempts)

    val onlineResolved =
      factory.resolveFavIconUrl("https://offline.example.com") {
        attempts++
        FavIconResolution(
          url = "https://offline.example.com/apple-touch-icon.png",
          cacheable = true,
        )
      }
    assertEquals("https://offline.example.com/apple-touch-icon.png", onlineResolved)
    assertEquals(2, attempts)

    val cachedAfterSuccess =
      factory.resolveFavIconUrl("https://offline.example.com") {
        attempts++
        FavIconResolution(url = "https://offline.example.com/other.png", cacheable = true)
      }
    assertEquals("https://offline.example.com/apple-touch-icon.png", cachedAfterSuccess)
    assertEquals(2, attempts)
  }

  @Test
  fun fetch_whenOfflineFailureOccurs_retriesHtmlParseOnceOnlineAndCachesAfterSuccess() = runTest {
    val homepageUrl = "https://blog.example.com"
    var isOnline = false
    var htmlNetworkCalls = 0
    val requestedFavIconUrls = mutableListOf<String>()

    val networkClient =
      object : NetworkClient {
        override suspend fun <T> executeRequest(
          request: NetworkRequest,
          block: suspend (NetworkResponse) -> T,
        ): T {
          htmlNetworkCalls++
          if (!isOnline) {
            throw OkioIOException("Offline")
          }
          val html =
            """<html><head><link rel="apple-touch-icon" href="/touch-icon.png"></head></html>"""
          return block(
            NetworkResponse(code = 200, body = NetworkResponseBody(Buffer().writeUtf8(html)))
          )
        }
      }
    val factory =
      FavIconFetcher.Factory(
        networkClient = { networkClient },
        cacheStrategy = { CacheStrategy.DEFAULT },
      )

    withTempDiskCache { diskCache ->
      fun newFetcher(): FavIconFetcher {
        return FavIconFetcher(
          url = homepageUrl,
          networkClient = lazy { networkClient },
          diskCache = lazy { null },
          fetchFavIcon = { favIconUrl ->
            requestedFavIconUrls += favIconUrl
            if (!isOnline) {
              throw OkioIOException("Offline")
            }
            SourceFetchResult(
              source =
                ImageSource(source = Buffer().writeUtf8("img"), fileSystem = diskCache.fileSystem),
              mimeType = "image/png",
              dataSource = DataSource.NETWORK,
            )
          },
          favIconUrlResolver = factory::resolveFavIconUrl,
        )
      }

      assertFailsWith<OkioIOException> { newFetcher().fetch() }
      assertEquals(1, htmlNetworkCalls)
      assertEquals(
        "https://www.google.com/s2/favicons?domain=blog.example.com&sz=180",
        requestedFavIconUrls.single(),
      )

      isOnline = true
      newFetcher().fetch()
      assertEquals(2, htmlNetworkCalls)
      assertEquals("https://blog.example.com/touch-icon.png", requestedFavIconUrls.last())

      newFetcher().fetch()
      assertEquals(2, htmlNetworkCalls)
      assertEquals("https://blog.example.com/touch-icon.png", requestedFavIconUrls.last())
    }
  }

  @Test
  fun fetch_when2xxHtmlHasNoIconLink_cachesFallbackUrlWithoutRefetchingHtml() = runTest {
    val homepageUrl = "https://plain.example.com"
    var htmlNetworkCalls = 0
    val requestedFavIconUrls = mutableListOf<String>()

    val networkClient =
      object : NetworkClient {
        override suspend fun <T> executeRequest(
          request: NetworkRequest,
          block: suspend (NetworkResponse) -> T,
        ): T {
          htmlNetworkCalls++
          val html = "<html><head><title>Plain</title></head><body>Hello</body></html>"
          return block(
            NetworkResponse(code = 200, body = NetworkResponseBody(Buffer().writeUtf8(html)))
          )
        }
      }
    val factory =
      FavIconFetcher.Factory(
        networkClient = { networkClient },
        cacheStrategy = { CacheStrategy.DEFAULT },
      )

    withTempDiskCache { diskCache ->
      fun newFetcher(): FavIconFetcher {
        return FavIconFetcher(
          url = homepageUrl,
          networkClient = lazy { networkClient },
          diskCache = lazy { null },
          fetchFavIcon = { favIconUrl ->
            requestedFavIconUrls += favIconUrl
            SourceFetchResult(
              source =
                ImageSource(source = Buffer().writeUtf8("img"), fileSystem = diskCache.fileSystem),
              mimeType = "image/png",
              dataSource = DataSource.NETWORK,
            )
          },
          favIconUrlResolver = factory::resolveFavIconUrl,
        )
      }

      newFetcher().fetch()
      newFetcher().fetch()

      assertEquals(1, htmlNetworkCalls)
      assertEquals(2, requestedFavIconUrls.size)
      assertTrue(requestedFavIconUrls.all { it.startsWith("https://www.google.com/s2/favicons") })
    }
  }

  @Test
  fun fetch_whenDiskCacheContains404Response_rejectsWithHttpExceptionAndClosesSnapshot() = runTest {
    val homepageUrl = "https://missing-icon.example.com"

    withTempDiskCache { diskCache ->
      val editor = checkNotNull(diskCache.openEditor(homepageUrl))
      diskCache.fileSystem.write(editor.metadata) {
        CacheNetworkResponse.writeTo(
          NetworkResponse(
            code = 404,
            headers = NetworkHeaders.Builder().set("Content-Type", "image/png").build(),
          ),
          this,
        )
      }
      diskCache.fileSystem.write(editor.data) { writeUtf8("default-globe-404-body") }
      editor.commit()

      val fetcher =
        FavIconFetcher(
          url = homepageUrl,
          networkClient =
            lazy {
              object : NetworkClient {
                override suspend fun <T> executeRequest(
                  request: NetworkRequest,
                  block: suspend (NetworkResponse) -> T,
                ): T = throw AssertionError("Should not hit network for cached 404")
              }
            },
          diskCache = lazy { diskCache },
          fetchFavIcon = { throw AssertionError("Should not delegate to networkFetcher") },
        )

      val error = assertFailsWith<HttpException> { fetcher.fetch() }
      assertEquals(404, error.response.code)

      val followUpEditor = diskCache.openEditor(homepageUrl)
      assertNotNull(followUpEditor)
      followUpEditor.abort()
    }
  }

  @Test
  fun fetch_whenDiskCacheContains200Response_returnsDiskResultWithCachedMimeType() = runTest {
    val homepageUrl = "https://valid-cached.example.com"

    withTempDiskCache { diskCache ->
      val editor = checkNotNull(diskCache.openEditor(homepageUrl))
      diskCache.fileSystem.write(editor.metadata) {
        CacheNetworkResponse.writeTo(
          NetworkResponse(
            code = 200,
            headers = NetworkHeaders.Builder().set("Content-Type", "image/svg+xml").build(),
          ),
          this,
        )
      }
      diskCache.fileSystem.write(editor.data) { writeUtf8("<svg/>") }
      editor.commit()

      val fetcher =
        FavIconFetcher(
          url = homepageUrl,
          networkClient =
            lazy {
              object : NetworkClient {
                override suspend fun <T> executeRequest(
                  request: NetworkRequest,
                  block: suspend (NetworkResponse) -> T,
                ): T = throw AssertionError("Should not hit network")
              }
            },
          diskCache = lazy { diskCache },
          fetchFavIcon = { throw AssertionError("Should not delegate to networkFetcher") },
        )

      val result = assertIs<SourceFetchResult>(fetcher.fetch())
      assertEquals(DataSource.DISK, result.dataSource)
      assertEquals("image/svg+xml", result.mimeType)
      result.source.close()

      val followUpEditor = diskCache.openEditor(homepageUrl)
      assertNotNull(followUpEditor)
      followUpEditor.abort()
    }
  }

  @Test
  fun fetch_whenDiskCacheResponseIsNull_closesSnapshotBeforeFallingThroughToSlowPath() = runTest {
    val corruptUrl = "https://corrupt-cache.example.com"
    val expiredUrl = "https://expired-cache.example.com"

    withTempDiskCache { diskCache ->
      val corruptEditor = checkNotNull(diskCache.openEditor(corruptUrl))
      diskCache.fileSystem.write(corruptEditor.metadata) { writeUtf8("not-valid-metadata") }
      diskCache.fileSystem.write(corruptEditor.data) { writeUtf8("stale") }
      corruptEditor.commit()

      val expiredEditor = checkNotNull(diskCache.openEditor(expiredUrl))
      diskCache.fileSystem.write(expiredEditor.metadata) {
        CacheNetworkResponse.writeTo(NetworkResponse(code = 200), this)
      }
      diskCache.fileSystem.write(expiredEditor.data) { writeUtf8("expired") }
      expiredEditor.commit()

      val htmlClient =
        object : NetworkClient {
          override suspend fun <T> executeRequest(
            request: NetworkRequest,
            block: suspend (NetworkResponse) -> T,
          ): T {
            val html = """<html><head><link rel="icon" href="/icon.png"></head></html>"""
            return block(
              NetworkResponse(code = 200, body = NetworkResponseBody(Buffer().writeUtf8(html)))
            )
          }
        }

      var canEditCorruptDuringSlowPath = false
      val corruptFetcher =
        FavIconFetcher(
          url = corruptUrl,
          networkClient = lazy { htmlClient },
          diskCache = lazy { diskCache },
          fetchFavIcon = {
            val probeEditor = diskCache.openEditor(corruptUrl)
            canEditCorruptDuringSlowPath = probeEditor != null
            probeEditor?.abort()
            SourceFetchResult(
              source =
                ImageSource(
                  source = Buffer().writeUtf8("fresh"),
                  fileSystem = diskCache.fileSystem,
                ),
              mimeType = "image/png",
              dataSource = DataSource.NETWORK,
            )
          },
        )
      corruptFetcher.fetch()
      assertTrue(canEditCorruptDuringSlowPath)

      var canEditExpiredDuringSlowPath = false
      val expiredFetcher =
        FavIconFetcher(
          url = expiredUrl,
          networkClient = lazy { htmlClient },
          diskCache = lazy { diskCache },
          readFromCacheStrategy = { _, networkRequest ->
            CacheStrategy.ReadResult(request = networkRequest)
          },
          fetchFavIcon = {
            val probeEditor = diskCache.openEditor(expiredUrl)
            canEditExpiredDuringSlowPath = probeEditor != null
            probeEditor?.abort()
            SourceFetchResult(
              source =
                ImageSource(
                  source = Buffer().writeUtf8("fresh"),
                  fileSystem = diskCache.fileSystem,
                ),
              mimeType = "image/png",
              dataSource = DataSource.NETWORK,
            )
          },
        )
      expiredFetcher.fetch()
      assertTrue(canEditExpiredDuringSlowPath)
    }
  }

  @Test
  fun normalizeFavIconParams_upgradesSmallSizesTo192PxAndScopesCacheKey() {
    val fromTinyUnkeyed =
      normalizeFavIconParams(
        memoryCacheKey = null,
        diskCacheKey = "https://example.com",
        dataKey = "https://example.com",
        resolvedSize = Size(16, 16),
      )
    assertEquals(Size(192, 192), fromTinyUnkeyed.size)
    assertEquals(
      "https://example.com#${Size(192, 192).width}x${Size(192, 192).height}",
      fromTinyUnkeyed.memoryCacheKey,
    )

    val fromTinyKeyed =
      normalizeFavIconParams(
        memoryCacheKey = "https://example.com#${Size(24, 24).width}x${Size(24, 24).height}",
        diskCacheKey = "https://example.com",
        dataKey = "https://example.com",
        resolvedSize = Size(24, 24),
      )
    assertEquals(Size(192, 192), fromTinyKeyed.size)
    assertEquals(fromTinyUnkeyed.memoryCacheKey, fromTinyKeyed.memoryCacheKey)
  }

  @Test
  fun normalizeFavIconParams_preservesLargerAndOriginalSizesWithDistinctCacheKeys() {
    val defaultSized =
      normalizeFavIconParams(
        memoryCacheKey = "https://example.com",
        diskCacheKey = "https://example.com",
        dataKey = "https://example.com",
        resolvedSize = Size(192, 192),
      )
    val largeSized =
      normalizeFavIconParams(
        memoryCacheKey = "https://example.com",
        diskCacheKey = "https://example.com",
        dataKey = "https://example.com",
        resolvedSize = Size(256, 256),
      )
    val originalSized =
      normalizeFavIconParams(
        memoryCacheKey = "https://example.com",
        diskCacheKey = "https://example.com",
        dataKey = "https://example.com",
        resolvedSize = Size.ORIGINAL,
      )

    assertEquals(Size(256, 256), largeSized.size)
    assertEquals(Size.ORIGINAL, originalSized.size)
    assertNotEquals(defaultSized.memoryCacheKey, largeSized.memoryCacheKey)
    assertNotEquals(defaultSized.memoryCacheKey, originalSized.memoryCacheKey)
  }

  private suspend fun withTempDiskCache(block: suspend (DiskCache) -> Unit) {
    val dir =
      FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve(
        "favicon_fetcher_test_${Random.nextLong().toULong()}"
      )
    val diskCache = DiskCache.Builder().directory(dir).maxSizeBytes(1024 * 1024).build()
    try {
      block(diskCache)
    } finally {
      diskCache.shutdown()
      diskCache.fileSystem.deleteRecursively(dir)
    }
  }

  private fun assertTrueAllEqual(expected: String, actuals: List<String>) {
    for (actual in actuals) {
      assertEquals(expected, actual)
    }
  }
}
