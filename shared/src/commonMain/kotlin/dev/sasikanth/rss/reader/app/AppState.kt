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

package dev.sasikanth.rss.reader.app

import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.HomeViewMode

data class AppState(
  val appThemeMode: AppThemeMode,
  val useAmoled: Boolean,
  val dynamicColorEnabled: Boolean,
  val showFeedFavIcon: Boolean,
  val homeViewMode: HomeViewMode,
  val showReaderView: Boolean,
  val activePostIndex: Int,
  val activePostId: String?,
  val blockImages: Boolean,
) {

  companion object {
    val DEFAULT =
      AppState(
        appThemeMode = AppThemeMode.Auto,
        useAmoled = false,
        dynamicColorEnabled = true,
        showFeedFavIcon = true,
        homeViewMode = HomeViewMode.Default,
        showReaderView = false,
        activePostIndex = 0,
        activePostId = null,
        blockImages = false,
      )
  }
}
