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

package dev.sasikanth.rss.reader.data.database

import app.cash.sqldelight.db.SqlDriver
import me.tatarka.inject.annotations.Inject

@Inject
class FeedPrePopulator(
  private val driver: SqlDriver,
  private val database: ReaderDatabase,
  private val prePopulateFeedQueries: Array<String>,
) {

  fun prePopulate(): Boolean {
    return database.transactionWithResult {
      if (database.feedQueries.numberOfFeeds().executeAsOne() == 0L) {
        prePopulateFeedQueries.forEach { query -> driver.execute(null, query, 0) }
        true
      } else {
        false
      }
    }
  }
}
