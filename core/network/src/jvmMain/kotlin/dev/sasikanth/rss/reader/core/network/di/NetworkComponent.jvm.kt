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

package dev.sasikanth.rss.reader.core.network.di

import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.di.scopes.AppScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit
import me.tatarka.inject.annotations.Provides
import okhttp3.Protocol

actual interface NetworkComponent {

  @Provides
  @AppScope
  fun providesHttpClient(appInfo: AppInfo): HttpClient {
    return httpClient(
      engine = OkHttp,
      appInfo = appInfo,
      config = {
        config {
          connectTimeout(10_000, TimeUnit.MILLISECONDS)
          retryOnConnectionFailure(true)

          protocols(listOf(Protocol.HTTP_1_1, Protocol.HTTP_2))
        }
      }
    )
  }

  @Provides @AppScope fun providesPlatformPageSize(): Long = 4096
}
