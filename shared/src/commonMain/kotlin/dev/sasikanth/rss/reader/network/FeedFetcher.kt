package dev.sasikanth.rss.reader.network

import dev.sasikanth.rss.reader.models.FeedPayload
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

internal expect fun feedFetcher(): FeedFetcher

internal class FeedFetcher(
  private val httpClient: HttpClient,
  private val feedParser: FeedParser
) {

  suspend fun fetch(url: String): FeedPayload {
    val xml = httpClient.get(url).bodyAsText()
    return feedParser.parse(xml)
  }
}
