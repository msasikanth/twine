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

import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToURL

@Inject
@ActivityScope
class IOSImageDownloader(
  private val imageDataFetcher: ImageDataFetcher,
  private val dispatchersProvider: DispatchersProvider,
) : ImageDownloader {

  @OptIn(ExperimentalForeignApi::class)
  override suspend fun download(imageUrl: String): ImageDownloadResult {
    val payload = imageDataFetcher.fetch(imageUrl) ?: return ImageDownloadResult.Failure

    return withContext(dispatchersProvider.io) {
      val fileManager = NSFileManager.defaultManager
      val documentsDir =
        fileManager.URLForDirectory(
          directory = NSDocumentDirectory,
          inDomain = NSUserDomainMask,
          appropriateForURL = null,
          create = true,
          error = null,
        ) ?: return@withContext ImageDownloadResult.Failure

      val targetUrl = uniqueFileUrl(fileManager, documentsDir, payload.fileName)
      val nsData = payload.bytes.toNSData() ?: return@withContext ImageDownloadResult.Failure
      val written = nsData.writeToURL(targetUrl, atomically = true)
      if (written) ImageDownloadResult.Success else ImageDownloadResult.Failure
    }
  }

  private fun uniqueFileUrl(fileManager: NSFileManager, directory: NSURL, fileName: String): NSURL {
    var candidate = directory.URLByAppendingPathComponent(fileName)!!
    if (!fileManager.fileExistsAtPath(candidate.path!!)) return candidate

    val base = fileName.substringBeforeLast('.')
    val ext = fileName.substringAfterLast('.', "")
    var index = 1
    while (fileManager.fileExistsAtPath(candidate.path!!)) {
      val nextName = if (ext.isNotEmpty()) "${base}_$index.$ext" else "${base}_$index"
      candidate = directory.URLByAppendingPathComponent(nextName)!!
      index++
    }
    return candidate
  }

  @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
  private fun ByteArray.toNSData(): NSData? {
    if (isEmpty()) return null
    return usePinned { pinned ->
      NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
  }
}
