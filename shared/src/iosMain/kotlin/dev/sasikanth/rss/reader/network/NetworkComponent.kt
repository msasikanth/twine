package dev.sasikanth.rss.reader.network

import dev.sasikanth.rss.reader.di.scopes.AppScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import me.tatarka.inject.annotations.Provides

actual interface NetworkComponent {

  val IOSFeedParser.bind: FeedParser
    @Provides @AppScope get() = this

  @Provides
  @AppScope
  fun providesHttpClient(): HttpClient {
    return HttpClient(Darwin) {
      engine {
        configureRequest {
          setTimeoutInterval(60.0)
          setAllowsCellularAccess(true)
        }
      }
    }
  }
}
