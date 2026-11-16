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
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.SwingUtilities
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class JvmImageDownloader(
  private val imageDataFetcher: ImageDataFetcher,
  private val dispatchersProvider: DispatchersProvider,
) : ImageDownloader {

  override suspend fun download(imageUrl: String): ImageDownloadResult {
    val payload = imageDataFetcher.fetch(imageUrl) ?: return ImageDownloadResult.Failure

    return withContext(dispatchersProvider.io) {
      var selectedDirectory: String? = null
      var selectedFile: String? = null

      SwingUtilities.invokeAndWait {
        val dialog = FileDialog(null as Frame?, null, FileDialog.SAVE)
        val downloadsDir = File(System.getProperty("user.home"), "Downloads")
        dialog.directory =
          if (downloadsDir.isDirectory) downloadsDir.absolutePath
          else System.getProperty("user.home")
        dialog.file = payload.fileName
        dialog.isVisible = true
        selectedDirectory = dialog.directory
        selectedFile = dialog.file
      }

      val dir = selectedDirectory
      val file = selectedFile
      if (dir == null || file == null) {
        return@withContext ImageDownloadResult.Cancelled
      }

      runCatching {
          File(dir, file).writeBytes(payload.bytes)
          ImageDownloadResult.Success
        }
        .getOrDefault(ImageDownloadResult.Failure)
    }
  }
}
