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

package dev.sasikanth.rss.reader.notifications

import me.tatarka.inject.annotations.Inject

@Inject
class JvmNotifier : Notifier {
  override fun show(title: String, content: String, notificationId: Int) {
    // no-op
  }

  override suspend fun requestPermission(): Boolean = true

  override fun openSettings() {
    // no-op
  }
}
