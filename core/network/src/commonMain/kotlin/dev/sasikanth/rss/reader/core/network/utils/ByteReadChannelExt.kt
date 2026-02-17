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

package dev.sasikanth.rss.reader.core.network.utils

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import korlibs.io.lang.Charset
import korlibs.io.lang.Charsets
import kotlinx.io.readByteArray

private val ENCODING_REGEX = """<?xml.*encoding=["']([^"']+)["'].*?>""".toRegex()

internal suspend fun ByteReadChannel.toCharIterator(
  charset: Charset,
  platformPageSize: Long,
): CharIterator {
  val bytes = readRemaining().readByteArray()

  val firstChunkSize = minOf(bytes.size, platformPageSize.toInt())
  val encodingContent = buildString { Charsets.UTF8.decode(this, bytes, 0, firstChunkSize) }
  val encodingCharset = findEncodingCharset(encodingContent, charset)

  val text =
    buildString { encodingCharset.decode(this, bytes) }
      .replace("&acirc;&#128;&#148;", "&ndash;")
      .replace("&acirc;&#128;&#153;", "&apos;")

  val finalXml =
    if (text.startsWith("\uFEFF")) {
      text.substring(1)
    } else {
      text
    }

  return finalXml.iterator()
}

private fun findEncodingCharset(encodingContent: String, fallbackCharset: Charset) =
  (ENCODING_REGEX.find(encodingContent)?.groupValues?.get(1)?.let { encoding ->
    try {
      Charset.forName(encoding)
    } catch (e: Exception) {
      null
    }
  } ?: fallbackCharset)
