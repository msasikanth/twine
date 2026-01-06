/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.utils

import dev.sasikanth.rss.reader.di.scopes.AppScope
import me.tatarka.inject.annotations.Inject
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene

@Inject
@AppScope
class IosInAppRating : InAppRating {

  override suspend fun request() {
    val scene =
      UIApplication.sharedApplication.connectedScenes
        .mapNotNull { it as? UIWindowScene }
        .firstOrNull { it.activationState == platform.UIKit.UISceneActivationStateForegroundActive }

    if (scene != null) {
      SKStoreReviewController.requestReviewInScene(scene)
    }
  }
}
