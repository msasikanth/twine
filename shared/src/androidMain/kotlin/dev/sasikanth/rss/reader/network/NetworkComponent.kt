package dev.sasikanth.rss.reader.network

import dev.sasikanth.rss.reader.di.scopes.AppScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.time.Duration
import me.tatarka.inject.annotations.Provides

actual interface NetworkComponent {

  val AndroidFeedParser.bind: FeedParser
    @Provides @AppScope get() = this

  @Provides
  @AppScope
  fun providesHttpClient(): HttpClient {
    return HttpClient(OkHttp) {
      engine {
        config {
          retryOnConnectionFailure(true)
          callTimeout(Duration.ofMinutes(2))
        }
      }
    }
  }
}
