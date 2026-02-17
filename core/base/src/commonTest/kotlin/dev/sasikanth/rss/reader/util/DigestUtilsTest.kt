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

import kotlin.test.Test
import kotlin.test.assertEquals

class DigestUtilsTest {

  @Test
  fun sha256_should_return_correct_hash() {
    val data = "abc".encodeToByteArray()
    val hash = sha256(data)
    val hexHash = hash.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
    assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hexHash)
  }

  @Test
  fun sha256_should_return_correct_hash_for_empty_data() {
    val data = "".encodeToByteArray()
    val hash = sha256(data)
    val hexHash = hash.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
    assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hexHash)
  }
}
