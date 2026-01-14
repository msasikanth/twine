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

interface Notifier {
  fun show(
    title: String,
    content: String,
    notificationId: Int = 1,
  )

  suspend fun requestPermission(): Boolean

  fun openSettings()
}
