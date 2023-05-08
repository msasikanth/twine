package dev.sasikanth.rss.reader.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineDispatcher
import java.time.Duration

internal actual fun feedFetcher(ioDispatcher: CoroutineDispatcher): FeedFetcher {
  val httpClient = HttpClient(OkHttp) {
    engine {
      config {
        retryOnConnectionFailure(true)
        callTimeout(Duration.ofMinutes(2))
      }
    }
  }

  return FeedFetcher(
    httpClient = httpClient,
    feedParser = AndroidFeedParser(ioDispatcher)
  )
}
