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

import androidx.compose.runtime.staticCompositionLocalOf
import coil3.ImageLoader
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

enum class ImageDownloadResult {
  Success,
  Cancelled,
  PermissionDenied,
  Failure,
}

interface ImageDownloader {
  suspend fun download(imageUrl: String): ImageDownloadResult
}

val LocalImageDownloader = staticCompositionLocalOf<ImageDownloader> { NoopImageDownloader }

private object NoopImageDownloader : ImageDownloader {
  override suspend fun download(imageUrl: String) = ImageDownloadResult.Failure
}

data class ImagePayload(val bytes: ByteArray, val mimeType: String, val fileName: String) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as ImagePayload
    return mimeType == other.mimeType &&
      fileName == other.fileName &&
      bytes.contentEquals(other.bytes)
  }

  override fun hashCode(): Int {
    var result = bytes.contentHashCode()
    result = 31 * result + mimeType.hashCode()
    result = 31 * result + fileName.hashCode()
    return result
  }
}

@Inject
class ImageDataFetcher(
  private val httpClient: HttpClient,
  private val imageLoader: ImageLoader,
  private val dispatchersProvider: DispatchersProvider,
) {

  suspend fun fetch(imageUrl: String): ImagePayload? =
    withContext(dispatchersProvider.io) {
      if (imageUrl.isBlank()) return@withContext null

      if (imageUrl.startsWith("data:", ignoreCase = true)) {
        return@withContext decodeDataUri(imageUrl)
      }

      var headerMimeType: String? = null
      val bytes =
        readFromDiskCache(imageUrl)
          ?: runCatching {
              val response = httpClient.get(imageUrl)
              if (!response.status.isSuccess()) return@runCatching null
              headerMimeType =
                response.contentType()?.let { "${it.contentType}/${it.contentSubtype}" }
              response.bodyAsBytes()
            }
            .getOrNull()

      if (bytes == null || bytes.isEmpty()) return@withContext null

      val (mimeType, extension) = resolveImageType(bytes, headerMimeType, imageUrl)
      val fileName = buildFileName(imageUrl, extension)
      ImagePayload(bytes = bytes, mimeType = mimeType, fileName = fileName)
    }

  private fun readFromDiskCache(imageUrl: String): ByteArray? {
    val diskCache = imageLoader.diskCache ?: return null
    return runCatching {
        diskCache.openSnapshot(imageUrl)?.use { snapshot ->
          diskCache.fileSystem.read(snapshot.data) { readByteArray() }
        }
      }
      .getOrNull()
  }

  @OptIn(ExperimentalEncodingApi::class)
  private fun decodeDataUri(dataUri: String): ImagePayload? {
    val commaIndex = dataUri.indexOf(',')
    if (commaIndex == -1) return null
    val metadata = dataUri.substring(5, commaIndex)
    if (!metadata.contains(";base64", ignoreCase = true)) return null
    val mimeType = metadata.substringBefore(';').takeIf { it.startsWith("image/") }
    val bytes = runCatching { Base64.decode(dataUri.substring(commaIndex + 1)) }.getOrNull()
    if (bytes == null || bytes.isEmpty()) return null

    val (resolvedMime, extension) = resolveImageType(bytes, mimeType, dataUri)
    return ImagePayload(
      bytes = bytes,
      mimeType = resolvedMime,
      fileName = "twine_image_${Clock.System.now().toEpochMilliseconds()}.$extension",
    )
  }

  private fun resolveImageType(
    bytes: ByteArray,
    headerMimeType: String?,
    imageUrl: String,
  ): Pair<String, String> {
    detectMagicType(bytes)?.let {
      return it
    }

    val normalizedHeader = headerMimeType?.lowercase()
    if (normalizedHeader != null && normalizedHeader.startsWith("image/")) {
      val ext = extensionForMimeType(normalizedHeader)
      if (ext != null) return normalizedHeader to ext
    }

    val urlExtension =
      imageUrl
        .substringBefore('?')
        .substringBefore('#')
        .substringAfterLast('/', "")
        .substringAfterLast('.', "")
        .lowercase()
    mimeTypeForExtension(urlExtension)?.let {
      return it to urlExtension
    }

    return "image/jpeg" to "jpg"
  }

  private fun detectMagicType(bytes: ByteArray): Pair<String, String>? {
    if (
      bytes.size >= 8 &&
        bytes[0] == 0x89.toByte() &&
        bytes[1] == 0x50.toByte() &&
        bytes[2] == 0x4E.toByte() &&
        bytes[3] == 0x47.toByte() &&
        bytes[4] == 0x0D.toByte() &&
        bytes[5] == 0x0A.toByte() &&
        bytes[6] == 0x1A.toByte() &&
        bytes[7] == 0x0A.toByte()
    ) {
      return "image/png" to "png"
    }
    if (
      bytes.size >= 3 &&
        bytes[0] == 0xFF.toByte() &&
        bytes[1] == 0xD8.toByte() &&
        bytes[2] == 0xFF.toByte()
    ) {
      return "image/jpeg" to "jpg"
    }
    if (
      bytes.size >= 6 &&
        bytes[0] == 'G'.code.toByte() &&
        bytes[1] == 'I'.code.toByte() &&
        bytes[2] == 'F'.code.toByte() &&
        bytes[3] == '8'.code.toByte()
    ) {
      return "image/gif" to "gif"
    }
    if (
      bytes.size >= 12 &&
        bytes[0] == 'R'.code.toByte() &&
        bytes[1] == 'I'.code.toByte() &&
        bytes[2] == 'F'.code.toByte() &&
        bytes[3] == 'F'.code.toByte() &&
        bytes[8] == 'W'.code.toByte() &&
        bytes[9] == 'E'.code.toByte() &&
        bytes[10] == 'B'.code.toByte() &&
        bytes[11] == 'P'.code.toByte()
    ) {
      return "image/webp" to "webp"
    }
    if (bytes.size >= 2 && bytes[0] == 'B'.code.toByte() && bytes[1] == 'M'.code.toByte()) {
      return "image/bmp" to "bmp"
    }
    if (
      bytes.size >= 12 &&
        bytes[4] == 'f'.code.toByte() &&
        bytes[5] == 't'.code.toByte() &&
        bytes[6] == 'y'.code.toByte() &&
        bytes[7] == 'p'.code.toByte()
    ) {
      val brand = bytes.decodeToString(startIndex = 8, endIndex = 12)
      when (brand) {
        "avif",
        "avis" -> return "image/avif" to "avif"
        "heic",
        "heix",
        "hevc",
        "hevx",
        "mif1" -> return "image/heic" to "heic"
      }
    }
    val prefixLength = minOf(bytes.size, 256)
    val prefix = bytes.decodeToString(startIndex = 0, endIndex = prefixLength).trimStart()
    if (
      prefix.startsWith("<svg", ignoreCase = true) ||
        (prefix.startsWith("<?xml", ignoreCase = true) &&
          prefix.contains("<svg", ignoreCase = true))
    ) {
      return "image/svg+xml" to "svg"
    }
    return null
  }

  private fun extensionForMimeType(mimeType: String): String? =
    when (mimeType) {
      "image/jpeg",
      "image/jpg" -> "jpg"
      "image/png" -> "png"
      "image/gif" -> "gif"
      "image/webp" -> "webp"
      "image/bmp" -> "bmp"
      "image/avif" -> "avif"
      "image/heic",
      "image/heif" -> "heic"
      "image/svg+xml" -> "svg"
      else -> null
    }

  private fun mimeTypeForExtension(extension: String): String? =
    when (extension) {
      "jpg",
      "jpeg" -> "image/jpeg"
      "png" -> "image/png"
      "gif" -> "image/gif"
      "webp" -> "image/webp"
      "bmp" -> "image/bmp"
      "avif" -> "image/avif"
      "heic",
      "heif" -> "image/heic"
      "svg" -> "image/svg+xml"
      else -> null
    }

  private fun buildFileName(imageUrl: String, extension: String): String {
    val rawName =
      imageUrl
        .substringBefore('?')
        .substringBefore('#')
        .substringAfterLast('/', "")
        .replace(Regex("[^A-Za-z0-9._-]"), "_")
        .trim('.')
    val baseName =
      rawName.substringBeforeLast('.', missingDelimiterValue = rawName).take(64).ifBlank {
        "twine_image_${Clock.System.now().toEpochMilliseconds()}"
      }
    return "$baseName.$extension"
  }
}
