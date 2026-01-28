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
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom

internal object UuidAdapter : ColumnAdapter<Uuid, String> {

  override fun decode(databaseValue: String): Uuid {
    return uuidFrom(databaseValue)
  }

  override fun encode(value: Uuid): String {
    return value.toString()
  }
}
