/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.sasikanth.rss.reader.core.network.miniflux

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.User
import dev.sasikanth.rss.reader.core.model.remote.miniflux.MinifluxCategory
import dev.sasikanth.rss.reader.core.model.remote.miniflux.MinifluxCreateFeedResponse
import dev.sasikanth.rss.reader.core.model.remote.miniflux.MinifluxEntriesPayload
import dev.sasikanth.rss.reader.core.model.remote.miniflux.MinifluxEntryContent
import dev.sasikanth.rss.reader.core.model.remote.miniflux.MinifluxError
import dev.sasikanth.rss.reader.core.model.remote.miniflux.MinifluxFeed
import dev.sasikanth.rss.reader.core.model.remote.miniflux.MinifluxUser
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import me.tatarka.inject.annotations.Inject

@Inject
class MinifluxSource(
  private val appHttpClient: HttpClient,
  private val user: suspend () -> User?,
  private val dispatchersProvider: DispatchersProvider,
) {

  private val httpClientMutex = Mutex()
  private val defaultHttpClient by lazy {
    appHttpClient.config {
      followRedirects = true

      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
            useAlternativeNames = false
          }
        )
      }
    }
  }

  private var _authenticatedHttpClient: HttpClient? = null
  private var cachedUserId: String? = null

  suspend fun verify(endpoint: String, token: String): MinifluxUser? {
    return withContext(dispatchersProvider.io) {
      try {
        val response =
          defaultHttpClient
            .config {
              defaultRequest {
                url(endpoint)
                header("X-Auth-Token", token.trim())
              }
            }
            .get(MinifluxApi.Me())

        if (response.status == HttpStatusCode.OK) {
          response.body<MinifluxUser>()
        } else {
          null
        }
      } catch (e: Exception) {
        null
      }
    }
  }

  suspend fun feeds(): List<MinifluxFeed> {
    return withContext(dispatchersProvider.io) {
      authenticatedHttpClient().get(MinifluxApi.Feeds()).body<List<MinifluxFeed>>()
    }
  }

  suspend fun feed(feedId: Long): MinifluxFeed {
    return withContext(dispatchersProvider.io) {
      authenticatedHttpClient().get(MinifluxApi.Feed(feedId = feedId)).body<MinifluxFeed>()
    }
  }

  suspend fun entries(
    status: List<String>? = null,
    limit: Int? = null,
    offset: Int? = null,
    after: Long? = null,
    starred: Boolean? = null,
    feedId: Long? = null,
  ): MinifluxEntriesPayload {
    return withContext(dispatchersProvider.io) {
      if (feedId == null) {
        authenticatedHttpClient()
          .get(
            MinifluxApi.Entries(
              status = status,
              limit = limit,
              offset = offset,
              after = after,
              starred = starred?.toString(),
            )
          )
          .body<MinifluxEntriesPayload>()
      } else {
        authenticatedHttpClient()
          .get(
            MinifluxApi.Feed.Entries(
              parent = MinifluxApi.Feed(feedId = feedId),
              status = status,
              limit = limit,
              offset = offset,
              after = after,
              starred = starred?.toString(),
            )
          )
          .body<MinifluxEntriesPayload>()
      }
    }
  }

  suspend fun fetchEntryContent(entryId: Long): MinifluxEntryContent {
    return withContext(dispatchersProvider.io) {
      authenticatedHttpClient().get(MinifluxApi.FetchContent(entryId = entryId)).body()
    }
  }

  suspend fun categories(): List<MinifluxCategory> {
    return withContext(dispatchersProvider.io) {
      authenticatedHttpClient().get(MinifluxApi.Categories()).body<List<MinifluxCategory>>()
    }
  }

  suspend fun addFeed(url: String, categoryId: Long): MinifluxFeed {
    return withContext(dispatchersProvider.io) {
      val response =
        authenticatedHttpClient().post(MinifluxApi.Feeds()) {
          contentType(ContentType.Application.Json)
          setBody(
            buildJsonObject {
              put("feed_url", url)
              put("category_id", categoryId)
            }
          )
        }

      if (response.status == HttpStatusCode.Created) {
        val createFeedResponse = response.body<MinifluxCreateFeedResponse>()
        feed(createFeedResponse.feedId)
      } else if (response.status == HttpStatusCode.BadRequest) {
        val error = response.body<MinifluxError>()
        if (error.errorMessage == "This feed already exists.") {
          feeds().find { it.feedUrl == url }
            ?: throw Exception("Feed already exists but could not be found")
        } else {
          throw Exception(error.errorMessage)
        }
      } else {
        response.body<MinifluxFeed>()
      }
    }
  }

  suspend fun updateFeed(feedId: Long, title: String, categoryId: Long) {
    withContext(dispatchersProvider.io) {
      val response =
        authenticatedHttpClient().put(MinifluxApi.Feed(feedId = feedId)) {
          contentType(ContentType.Application.Json)
          setBody(
            buildJsonObject {
              put("title", title)
              put("category_id", categoryId)
            }
          )
        }

      if (!response.status.isSuccess()) {
        throw Exception("Failed to update feed: ${response.status}")
      }
    }
  }

  suspend fun deleteFeed(feedId: Long) {
    withContext(dispatchersProvider.io) {
      val response = authenticatedHttpClient().delete(MinifluxApi.Feed(feedId = feedId))

      if (response.status == HttpStatusCode.NotFound) {
        // no-op: feed does not exist, or must have been deleted remotely
      } else if (!response.status.isSuccess()) {
        throw Exception("Failed to delete feed: ${response.status}")
      }
    }
  }

  suspend fun markEntriesAsRead(ids: List<Long>) {
    updateEntriesStatus(ids, "read")
  }

  suspend fun markEntriesAsUnread(ids: List<Long>) {
    updateEntriesStatus(ids, "unread")
  }

  suspend fun addBookmarks(ids: List<Long>) {
    withContext(dispatchersProvider.io) {
      ids.forEach { entryId ->
        try {
          authenticatedHttpClient().put(MinifluxApi.ToggleEntryBookmark(entryId = entryId))
        } catch (e: Exception) {
          Logger.e(e) { "Failed to add bookmark for entry: $entryId" }
          throw e
        }
      }
    }
  }

  suspend fun removeBookmarks(ids: List<Long>) {
    withContext(dispatchersProvider.io) {
      ids.forEach { entryId ->
        try {
          authenticatedHttpClient().put(MinifluxApi.ToggleEntryBookmark(entryId = entryId))
        } catch (e: Exception) {
          Logger.e(e) { "Failed to remove bookmark for entry: $entryId" }
          throw e
        }
      }
    }
  }

  private suspend fun updateEntriesStatus(ids: List<Long>, status: String) {
    withContext(dispatchersProvider.io) {
      val response =
        authenticatedHttpClient().put(MinifluxApi.UpdateEntries()) {
          contentType(ContentType.Application.Json)
          setBody(
            buildJsonObject {
              putJsonArray("entry_ids") { ids.forEach { add(JsonPrimitive(it)) } }
              put("status", status)
            }
          )
        }

      if (!response.status.isSuccess()) {
        throw Exception("Failed to update entries status: ${response.status}")
      }
    }
  }

  suspend fun addCategory(title: String): MinifluxCategory {
    return withContext(dispatchersProvider.io) {
      val response =
        authenticatedHttpClient().post(MinifluxApi.Categories()) {
          contentType(ContentType.Application.Json)
          setBody(buildJsonObject { put("title", title) })
        }

      if (response.status == HttpStatusCode.Created) {
        response.body<MinifluxCategory>()
      } else if (response.status == HttpStatusCode.BadRequest) {
        val error = response.body<MinifluxError>()
        if (error.errorMessage == "This category already exists.") {
          categories().find { it.title == title }
            ?: throw Exception("Category already exists but could not be found")
        } else {
          throw Exception(error.errorMessage)
        }
      } else {
        response.body<MinifluxCategory>()
      }
    }
  }

  suspend fun updateCategory(categoryId: Long, title: String) {
    withContext(dispatchersProvider.io) {
      val response =
        authenticatedHttpClient().put(MinifluxApi.Category(categoryId = categoryId)) {
          contentType(ContentType.Application.Json)
          setBody(buildJsonObject { put("title", title) })
        }

      if (response.status == HttpStatusCode.NotFound) {
        // no-op: category does not exist, or must have been deleted remotely
      } else if (!response.status.isSuccess()) {
        throw Exception("Failed to update category: ${response.status}")
      }
    }
  }

  suspend fun deleteCategory(categoryId: Long) {
    withContext(dispatchersProvider.io) {
      val response = authenticatedHttpClient().delete(MinifluxApi.Category(categoryId = categoryId))
      if (response.status == HttpStatusCode.NotFound) {
        // no-op
      } else if (!response.status.isSuccess()) {
        throw Exception("Failed to delete category: ${response.status}")
      }
    }
  }

  private suspend fun authenticatedHttpClient(): HttpClient {
    return httpClientMutex.withLock {
      val user = user()
      if (user == null) {
        _authenticatedHttpClient = null
        cachedUserId = null
        return@withLock defaultHttpClient
      }

      if (_authenticatedHttpClient != null && cachedUserId == user.id) {
        return@withLock _authenticatedHttpClient!!
      }

      defaultHttpClient
        .config {
          defaultRequest {
            val baseEndPoint = user.serverUrl
            if (!(baseEndPoint.isNullOrBlank())) {
              url(baseEndPoint)
            }

            val authToken = user.token
            if (!(authToken.isNullOrBlank())) {
              header("X-Auth-Token", authToken.trim())
            }
          }
        }
        .also {
          _authenticatedHttpClient = it
          cachedUserId = user.id
        }
    }
  }
}
