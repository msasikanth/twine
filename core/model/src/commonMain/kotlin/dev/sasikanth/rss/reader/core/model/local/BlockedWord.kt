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

package dev.sasikanth.rss.reader.core.model.local

import androidx.compose.runtime.Stable
import com.benasher44.uuid.Uuid
import kotlin.time.Instant

@Stable
data class BlockedWord(
  val id: Uuid,
  val content: String,
  val isDeleted: Boolean = false,
  val updatedAt: Instant = Instant.DISTANT_PAST,
)
