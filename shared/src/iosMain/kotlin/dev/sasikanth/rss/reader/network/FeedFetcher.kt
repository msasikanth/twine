package dev.sasikanth.rss.reader.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

internal actual fun feedFetcher(): FeedFetcher {
  val httpClient = HttpClient(Darwin) {
    engine {
      configureRequest {
        setTimeoutInterval(60.0)
        setAllowsCellularAccess(true)
      }
    }
  }

  return FeedFetcher(
    httpClient = httpClient,
    feedParser = IOSFeedParser()
  )
}
