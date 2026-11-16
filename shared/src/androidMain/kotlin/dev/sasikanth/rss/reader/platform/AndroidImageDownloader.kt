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

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import java.io.File
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class AndroidImageDownloader(
  private val activity: ComponentActivity,
  private val imageDataFetcher: ImageDataFetcher,
  private val dispatchersProvider: DispatchersProvider,
) : ImageDownloader {

  override suspend fun download(imageUrl: String): ImageDownloadResult {
    val payload = imageDataFetcher.fetch(imageUrl) ?: return ImageDownloadResult.Failure

    return withContext(dispatchersProvider.io) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        saveWithMediaStore(payload)
      } else {
        saveLegacy(payload)
      }
    }
  }

  private fun saveWithMediaStore(payload: ImagePayload): ImageDownloadResult {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return ImageDownloadResult.Failure

    val resolver = activity.contentResolver
    val values =
      ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, payload.fileName)
        put(MediaStore.Downloads.MIME_TYPE, payload.mimeType)
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        put(MediaStore.Downloads.IS_PENDING, 1)
      }

    val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val uri = resolver.insert(collection, values) ?: return ImageDownloadResult.Failure

    return try {
      resolver.openOutputStream(uri)?.use { outputStream ->
        outputStream.write(payload.bytes)
        outputStream.flush()
      } ?: error("Unable to open output stream")

      val completedValues = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
      resolver.update(uri, completedValues, null, null)
      ImageDownloadResult.Success
    } catch (_: Exception) {
      resolver.delete(uri, null, null)
      ImageDownloadResult.Failure
    }
  }

  @Suppress("DEPRECATION")
  private fun saveLegacy(payload: ImagePayload): ImageDownloadResult {
    val hasPermission =
      ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
        PackageManager.PERMISSION_GRANTED
    if (!hasPermission) {
      return ImageDownloadResult.PermissionDenied
    }

    return try {
      val downloadsDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).apply {
          mkdirs()
        }
      val targetFile = uniqueFile(downloadsDir, payload.fileName)
      targetFile.writeBytes(payload.bytes)
      MediaScannerConnection.scanFile(
        activity,
        arrayOf(targetFile.absolutePath),
        arrayOf(payload.mimeType),
        null,
      )
      ImageDownloadResult.Success
    } catch (_: Exception) {
      ImageDownloadResult.Failure
    }
  }

  private fun uniqueFile(dir: File, fileName: String): File {
    var candidate = File(dir, fileName)
    if (!candidate.exists()) return candidate

    val base = fileName.substringBeforeLast('.')
    val ext = fileName.substringAfterLast('.', "")
    var index = 1
    while (candidate.exists()) {
      val nextName = if (ext.isNotEmpty()) "${base}_$index.$ext" else "${base}_$index"
      candidate = File(dir, nextName)
      index++
    }
    return candidate
  }
}
