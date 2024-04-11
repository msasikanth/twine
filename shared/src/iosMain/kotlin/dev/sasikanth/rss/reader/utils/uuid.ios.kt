/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.utils

import com.benasher44.uuid.UuidHasher
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA1
import platform.CoreCrypto.CC_SHA1_DIGEST_LENGTH

internal actual fun hasher(): UuidHasher {
  return AppleHasher(AppleHasher.Companion::sha1Digest, 5)
}

// Copied from:
// https://github.com/benasher44/uuid/blob/f3768dd19fdd58ac01711733923d7db5a433ac79/src/appleMain/kotlin/namebased.kt#L40
@OptIn(ExperimentalForeignApi::class)
private class AppleHasher(
  private val digestFunc: (ByteArray) -> ByteArray,
  override val version: Int,
) : UuidHasher {
  private var data = ByteArray(0)

  override fun update(input: ByteArray) {
    val prevLength = data.size
    data = data.copyOf(data.size + input.size)
    input.copyInto(data, prevLength)
  }

  override fun digest(): ByteArray {
    return digestFunc(data)
  }

  companion object {
    fun sha1Digest(data: ByteArray): ByteArray {
      return ByteArray(CC_SHA1_DIGEST_LENGTH).also { bytes ->
        bytes.usePinned { digestPin ->
          data.usePinned { dataPin ->
            CC_SHA1(dataPin.addressOf(0), data.size.toUInt(), digestPin.addressOf(0).reinterpret())
          }
        }
      }
    }
  }
}
