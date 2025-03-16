/*
 * Copyright 2025 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.util

import com.benasher44.uuid.UuidHasher
import java.security.MessageDigest

internal actual fun hasher(): UuidHasher {
  return JvmHasher("SHA-1", 5)
}

// Copied from:
// https://github.com/benasher44/uuid/blob/f3768dd19fdd58ac01711733923d7db5a433ac79/src/jvmMain/kotlin/namebased.kt#L33
private class JvmHasher(
  algorithmName: String,
  override val version: Int,
) : UuidHasher {
  private val digest = MessageDigest.getInstance(algorithmName)

  override fun update(input: ByteArray) {
    digest.update(input)
  }

  override fun digest(): ByteArray {
    return digest.digest()
  }
}
