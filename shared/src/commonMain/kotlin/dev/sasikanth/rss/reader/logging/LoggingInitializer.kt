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

package dev.sasikanth.rss.reader.logging

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.NoTagFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.bugsnag.BugsnagLogWriter
import co.touchlab.kermit.platformLogWriter
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.initializers.Initializer
import me.tatarka.inject.annotations.Inject

@Inject
class LoggingInitializer(private val appInfo: AppInfo) : Initializer {

  @OptIn(ExperimentalKermitApi::class)
  override fun initialize() {
    val loggers =
      if (appInfo.isDebugBuild) {
        listOf(platformLogWriter(NoTagFormatter))
      } else {
        listOf(platformLogWriter(NoTagFormatter), BugsnagLogWriter())
      }

    Logger.setLogWriters(loggers)
    Logger.setMinSeverity(Severity.Info)
  }
}
