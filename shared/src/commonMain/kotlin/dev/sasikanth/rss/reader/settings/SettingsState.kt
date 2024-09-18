/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.settings

import androidx.compose.runtime.Immutable
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.BrowserType
import dev.sasikanth.rss.reader.data.repository.Period
import dev.sasikanth.rss.reader.opml.OpmlResult

@Immutable
internal data class SettingsState(
  val browserType: BrowserType,
  val enableHomePageBlur: Boolean,
  val showUnreadPostsCount: Boolean,
  val hasFeeds: Boolean,
  val appInfo: AppInfo,
  val opmlResult: OpmlResult?,
  val postsDeletionPeriod: Period?,
  val showReaderView: Boolean,
  val appThemeMode: AppThemeMode,
) {

  companion object {

    fun default(appInfo: AppInfo) =
      SettingsState(
        browserType = BrowserType.Default,
        enableHomePageBlur = false,
        showUnreadPostsCount = false,
        hasFeeds = false,
        appInfo = appInfo,
        opmlResult = null,
        postsDeletionPeriod = null,
        showReaderView = false,
        appThemeMode = AppThemeMode.Auto,
      )
  }
}
