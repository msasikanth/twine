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

package dev.sasikanth.rss.reader.database

import app.cash.sqldelight.ColumnAdapter

internal object ListToStringAdapter : ColumnAdapter<List<String>, String> {

  override fun decode(databaseValue: String): List<String> {
    return databaseValue.split(", ")
  }

  override fun encode(value: List<String>): String {
    return value.joinToString()
  }
}
