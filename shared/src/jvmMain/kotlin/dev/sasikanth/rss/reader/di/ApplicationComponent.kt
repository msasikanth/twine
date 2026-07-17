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
package dev.sasikanth.rss.reader.di

import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.app.AppPlatform
import dev.sasikanth.rss.reader.app.isFoss
import dev.sasikanth.rss.reader.app.isGoogleDriveSupported
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.core.base.widget.di.WidgetPlatformComponent
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.utils.NewArticleNotifier
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.initializers.Initializer
import dev.sasikanth.rss.reader.reader.readability.HtmlReadabilityRunner
import dev.sasikanth.rss.reader.reader.redability.ReadabilityRunner
import dev.sasikanth.rss.reader.refresh.FeedsRefreshInitializer
import java.io.File
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

@AppScope
@Component
abstract class ApplicationComponent : SharedApplicationComponent(), WidgetPlatformComponent {

  abstract val rssRepository: RssRepository

  abstract val settingsRepository: SettingsRepository

  abstract val syncCoordinator: SyncCoordinator

  abstract val newArticleNotifier: NewArticleNotifier

  abstract val billingHandler: BillingHandler

  @IntoSet
  @Provides
  @AppScope
  fun providesFeedsRefreshInitializer(bind: FeedsRefreshInitializer): Initializer = bind

  @Provides
  @AppScope
  fun providesReadabilityRunner(readabilityRunner: HtmlReadabilityRunner): ReadabilityRunner =
    readabilityRunner

  @Provides
  @AppScope
  fun providesAppInfo(): AppInfo =
    AppInfo(
      versionCode = 1,
      versionName = "1.0.0",
      isDebugBuild = false,
      isFoss = isFoss,
      isGoogleDriveSupported = isGoogleDriveSupported,
      cachePath = {
        val cachePath = File(System.getProperty("user.home"), ".twine/cache")
        if (!cachePath.exists()) {
          cachePath.mkdirs()
        }
        cachePath.absolutePath
      },
      platform = AppPlatform.Desktop,
    )

  companion object
}
