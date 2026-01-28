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

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.UuidHasher
import com.benasher44.uuid.uuidOf
import kotlin.experimental.and
import kotlin.experimental.or

// Number of bytes in a UUID
internal const val UUID_BYTES = 16

fun nameBasedUuidOf(value: String): Uuid {
  val hasher = hasher()
  hasher.update(value.encodeToByteArray())
  val hashedBytes = hasher.digest()
  hashedBytes[6] =
    hashedBytes[6]
      .and(0b00001111) // clear the 4 most sig bits
      .or(hasher.version.shl(4).toByte())
  hashedBytes[8] =
    hashedBytes[8]
      .and(0b00111111) // clear the 2 most sig bits
      .or(-0b10000000) // set 2 most sig to 10
  return uuidOf(hashedBytes.copyOf(UUID_BYTES))
}

internal expect fun hasher(): UuidHasher
