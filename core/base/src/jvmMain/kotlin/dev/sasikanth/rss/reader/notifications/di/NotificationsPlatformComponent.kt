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

package dev.sasikanth.rss.reader.notifications.di

import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.notifications.JvmNotifier
import dev.sasikanth.rss.reader.notifications.Notifier
import me.tatarka.inject.annotations.Provides

actual interface NotificationsPlatformComponent {
  @Provides @AppScope fun JvmNotifier.bind(): Notifier = this
}
