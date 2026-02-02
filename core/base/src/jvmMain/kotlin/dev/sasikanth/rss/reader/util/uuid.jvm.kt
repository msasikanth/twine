/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.util

import com.benasher44.uuid.UuidHasher
import java.security.MessageDigest

internal actual fun hasher(): UuidHasher {
  return JvmHasher("SHA-1", 5)
}

private class JvmHasher(algorithmName: String, override val version: Int) : UuidHasher {
  private val digest = MessageDigest.getInstance(algorithmName)

  override fun update(input: ByteArray) {
    digest.update(input)
  }

  override fun digest(): ByteArray {
    return digest.digest()
  }
}
