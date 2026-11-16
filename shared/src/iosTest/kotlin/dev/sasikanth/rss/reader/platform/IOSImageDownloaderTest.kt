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

package dev.sasikanth.rss.reader.platform

import coil3.ImageLoader
import coil3.PlatformContext
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

class IOSImageDownloaderTest {

  @OptIn(ExperimentalForeignApi::class)
  @Test
  fun downloadsImageToDocumentsDirectory() = runTest {
    val pngBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x01, 0x02)
    val dispatcher = StandardTestDispatcher(testScheduler)
    val dispatchersProvider =
      object : DispatchersProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val databaseRead: CoroutineDispatcher = dispatcher
        override val databaseWrite: CoroutineDispatcher = dispatcher
      }
    val httpClient =
      HttpClient(MockEngine { respond(content = pngBytes, status = HttpStatusCode.OK) })
    val imageLoader = ImageLoader.Builder(PlatformContext.INSTANCE).build()
    val fetcher = ImageDataFetcher(httpClient, imageLoader, dispatchersProvider)
    val downloader = IOSImageDownloader(fetcher, dispatchersProvider)

    val result = downloader.download("https://example.com/ios_download_verify.png")

    assertEquals(ImageDownloadResult.Success, result)

    val fileManager = NSFileManager.defaultManager
    val docsDir =
      fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
      )
    val savedFileUrl = docsDir?.URLByAppendingPathComponent("ios_download_verify.png")
    val savedPath = savedFileUrl?.path
    assertTrue(savedPath != null && fileManager.fileExistsAtPath(savedPath))
    fileManager.removeItemAtPath(savedPath, null)
  }
}
