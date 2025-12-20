/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.core.network.utils

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import korlibs.io.lang.Charset
import korlibs.io.lang.Charsets
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlinx.io.readByteArray

internal fun ByteReadChannel.toCharIterator(
  charset: Charset,
  platformPageSize: Long,
  context: CoroutineContext = EmptyCoroutineContext
): CharIterator {
  return object : CharIterator() {

    private var encodingCharset: Charset? = null
    private var currentIndex = 0
    private var currentBuffer = ""
    private var isFirstRead = true

    override fun hasNext(): Boolean {
      if (currentIndex < currentBuffer.length) return true
      if (this@toCharIterator.isClosedForRead) return false

      val packet = runBlocking(context) { this@toCharIterator.readRemaining(platformPageSize) }
      val bytes = packet.readByteArray()
      val encodingRegex = """<?xml.*encoding=["']([^"']+)["'].*?>""".toRegex()
      if (encodingCharset == null) {
        val encodingContent = buildString { Charsets.UTF8.decode(this, bytes) }
        encodingCharset = findEncodingCharset(encodingRegex, encodingContent, charset)
      }

      currentBuffer =
        buildString { (encodingCharset ?: charset).decode(this, bytes) }
          .replace("&acirc;&#128;&#148;", "&ndash;")
          .replace("&acirc;&#128;&#153;", "&apos;")

      if (isFirstRead && currentBuffer.startsWith("\uFEFF")) {
        currentBuffer = currentBuffer.substring(1)
      }
      isFirstRead = false

      packet.close()
      currentIndex = 0
      return currentBuffer.isNotEmpty()
    }

    private fun findEncodingCharset(
      encodingRegex: Regex,
      encodingContent: String,
      fallbackCharset: Charset,
    ) =
      (encodingRegex.find(encodingContent)?.groupValues?.get(1)?.let { encoding ->
        try {
          Charset.forName(encoding)
        } catch (e: Exception) {
          null
        }
      }
        ?: fallbackCharset)

    override fun nextChar(): Char {
      if (!hasNext()) throw NoSuchElementException()
      return currentBuffer[currentIndex++]
    }
  }
}
