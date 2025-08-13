package dev.sasikanth.rss.reader.core.network

import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class FullArticleFetcher(
  private val httpClient: HttpClient,
  private val dispatchersProvider: DispatchersProvider,
) {

  private val fullArticleHttpClient by lazy {
    httpClient.config {
      followRedirects = true

      install(UserAgent) {
        agent =
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
      }
    }
  }

  suspend fun fetch(url: String): Result<String> {
    return withContext(dispatchersProvider.io) {
      try {
        val htmlContent =
          fullArticleHttpClient
            .get(url) {
              headers {
                append(
                  HttpHeaders.Accept,
                  "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                append(HttpHeaders.AcceptLanguage, "en-US,en;q=0.5")
                append(HttpHeaders.Connection, "keep-alive")
              }
            }
            .bodyAsText()
        Result.success(htmlContent)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }
  }
}
