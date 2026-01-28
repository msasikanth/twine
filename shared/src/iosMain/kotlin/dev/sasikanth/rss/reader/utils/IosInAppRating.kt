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

package dev.sasikanth.rss.reader.utils

import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import me.tatarka.inject.annotations.Inject
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene

@Inject
@AppScope
class IosInAppRating(
  private val settingsRepository: SettingsRepository,
) : InAppRating {

  override suspend fun request() {
    val scene =
      UIApplication.sharedApplication.connectedScenes
        .mapNotNull { it as? UIWindowScene }
        .firstOrNull { it.activationState == platform.UIKit.UISceneActivationStateForegroundActive }

    val now = Clock.System.now()
    val installDate = settingsRepository.installDate.firstOrNull() ?: now
    val lastPromptDate = settingsRepository.lastReviewPromptDate.firstOrNull() ?: now
    val sessionCount = settingsRepository.userSessionCount.first()
    val canShowReviewPrompt =
      canShowReviewPrompt(
        currentTime = Clock.System.now(),
        installDate = installDate,
        lastPromptDate = lastPromptDate,
        sessionCount = sessionCount,
      )

    if (scene != null && canShowReviewPrompt) {
      SKStoreReviewController.requestReviewInScene(scene)
    }
  }
}
