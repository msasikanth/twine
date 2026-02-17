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

package dev.sasikanth.rss.reader.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
actual fun sha256(data: ByteArray): ByteArray {
  return ByteArray(CC_SHA256_DIGEST_LENGTH).also { bytes ->
    bytes.usePinned { digestPin ->
      if (data.isNotEmpty()) {
        data.usePinned { dataPin ->
          CC_SHA256(dataPin.addressOf(0), data.size.toUInt(), digestPin.addressOf(0).reinterpret())
        }
      } else {
        CC_SHA256(null, 0u, digestPin.addressOf(0).reinterpret())
      }
    }
  }
}
