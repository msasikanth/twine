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

package dev.sasikanth.rss.reader.data.database.adapter

import app.cash.sqldelight.ColumnAdapter
import dev.sasikanth.rss.reader.core.model.local.PostFlag

object PostFlagsAdapter : ColumnAdapter<Set<PostFlag>, Long> {
  override fun decode(databaseValue: Long): Set<PostFlag> {
    val flags = mutableSetOf<PostFlag>()
    PostFlag.entries.forEach { flag ->
      if ((databaseValue and (1L shl flag.ordinal)) != 0L) {
        flags.add(flag)
      }
    }
    return flags
  }

  override fun encode(value: Set<PostFlag>): Long {
    var databaseValue = 0L
    value.forEach { flag -> databaseValue = databaseValue or (1L shl flag.ordinal) }
    return databaseValue
  }
}
