/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.core.network.freshrss

import dev.sasikanth.rss.reader.core.model.local.User
import dev.sasikanth.rss.reader.core.model.remote.freshrss.ArticlesPayload
import dev.sasikanth.rss.reader.core.model.remote.freshrss.ItemIds
import dev.sasikanth.rss.reader.core.model.remote.freshrss.SubscriptionsPayload
import dev.sasikanth.rss.reader.core.model.remote.freshrss.TagsPayload
import dev.sasikanth.rss.reader.core.model.remote.freshrss.UserInfoPayload
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
class FreshRssSource(
  private val appHttpClient: HttpClient,
  private val user: suspend () -> User?,
  private val dispatchersProvider: DispatchersProvider,
) {

  companion object {
    const val USER_STATE_READ = "user/-/state/com.google/read"
    const val USER_STATE_UNREAD = "user/-/state/com.google/reading-list"
    const val USER_STATE_STARRED = "user/-/state/com.google/starred"
  }

  private val httpClientMutex = Mutex()
  private val defaultHttpClient by lazy {
    appHttpClient.config {
      followRedirects = true

      install(ContentNegotiation) {
        val json = Json { ignoreUnknownKeys = true }

        json(json)
        register(ContentType.Text.Html, KotlinxSerializationConverter(json))
      }
    }
  }

  private var _authenticatedHttpClient: HttpClient? = null
  private var cachedUserId: String? = null

  /** @return: auth token for the account if successful or else return null */
  suspend fun login(endpoint: String, username: String, password: String): String? {
    return withContext(dispatchersProvider.io) {
      defaultHttpClient
        .config { defaultRequest { url(endpoint) } }
        .post(Authentication) {
          contentType(ContentType.Application.FormUrlEncoded)
          setBody(
            FormDataContent(
              parameters {
                append("Email", username)
                append("Passwd", password)
              }
            )
          )
        }
        .run {
          if (status != HttpStatusCode.OK) return@withContext null

          val authRegex = Regex("Auth=([^&]+)")
          val matchResult = authRegex.find(bodyAsText())

          if (matchResult == null) {
            return@withContext null
          }

          matchResult.groupValues[1]
        }
    }
  }

  suspend fun userInfo(endPoint: String, authToken: String): UserInfoPayload {
    return withContext(dispatchersProvider.io) {
      defaultHttpClient
        .config {
          defaultRequest {
            url(endPoint)
            header("Authorization", "GoogleLogin auth=${authToken}")
          }
        }
        .get(Reader.UserInfo())
        .body()
    }
  }

  suspend fun tags(): TagsPayload {
    return withContext(dispatchersProvider.io) {
      authenticatedHttpClient().get(Reader.Tags()).body()
    }
  }

  suspend fun subscriptions(): SubscriptionsPayload {
    return withContext(dispatchersProvider.io) {
      authenticatedHttpClient().get(Reader.Subscriptions()).body()
    }
  }

  suspend fun articles(
    streamId: String = "user/-/state/com.google/reading-list",
    limit: Int = 1000,
    newerThan: Long = Instant.DISTANT_PAST.toEpochMilliseconds(),
    continuation: String? = null,
    excludeState: String? = null,
  ): ArticlesPayload {
    return withContext(dispatchersProvider.io) {
      authenticatedHttpClient()
        .get(
          Reader.Articles(
            streamId = streamId,
            limit = limit,
            newerThan = newerThan,
            continuation = continuation ?: "",
            excludeState = excludeState
          )
        )
        .body()
    }
  }

  suspend fun markArticlesAsRead(ids: List<String>) {
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient().post(Reader.EditTag()) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
          FormDataContent(
            parameters {
              ids.forEach { id -> append("i", id) }
              append("a", USER_STATE_READ)
            }
          )
        )
      }
    }
  }

  suspend fun markArticlesAsUnRead(ids: List<String>) {
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient().post(Reader.EditTag()) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
          FormDataContent(
            parameters {
              ids.forEach { id -> append("i", id) }
              append("r", USER_STATE_READ)
            }
          )
        )
      }
    }
  }

  suspend fun addFeed(url: String) {
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient().post(Reader.AddFeed(quickadd = url))
    }
  }

  suspend fun editFeedName(feedId: String, name: String) {
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient().post(Reader.EditFeed()) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
          FormDataContent(
            parameters {
              append("ac", "edit")
              append("s", feedId)
              append("t", name)
            }
          )
        )
      }
    }
  }

  suspend fun deleteFeed(feedId: String) {
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient().post(Reader.EditFeed()) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
          FormDataContent(
            parameters {
              append("ac", "unsubscribe")
              append("s", feedId)
            }
          )
        )
      }
    }
  }

  suspend fun addTagToFeed(feedId: String, tagId: String) {
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient().post(Reader.EditFeed()) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
          FormDataContent(
            parameters {
              append("ac", "edit")
              append("s", feedId)
              append("a", tagId)
            }
          )
        )
      }
    }
  }

  suspend fun addTag(tagName: String) {
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient().post(Reader.EditFeed()) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
          FormDataContent(
            parameters {
              append("ac", "edit")
              append("s", "")
              append("a", "user/-/label/$tagName")
            }
          )
        )
      }
    }
  }

  suspend fun editTag(tagId: String, newName: String) {
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient().post(Reader.RenameTag()) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
          FormDataContent(
            parameters {
              append("s", tagId)
              append("dest", "user/-/label/$newName")
            }
          )
        )
      }
    }
  }

  suspend fun deleteTag(tagId: String) {
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient().post(Reader.DisableTag()) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(FormDataContent(parameters { append("s", tagId) }))
      }
    }
  }

  suspend fun addBookmarks(ids: List<String>) {
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient().post(Reader.EditTag()) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
          FormDataContent(
            parameters {
              ids.forEach { id -> append("i", id) }
              append("a", USER_STATE_STARRED)
            }
          )
        )
      }
    }
  }

  suspend fun removeBookmarks(ids: List<String>) {
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient().post(Reader.EditTag()) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
          FormDataContent(
            parameters {
              ids.forEach { id -> append("i", id) }
              append("r", USER_STATE_STARRED)
            }
          )
        )
      }
    }
  }

  suspend fun readIds(): List<String> {
    return fetchIds(state = USER_STATE_READ)
  }

  suspend fun unreadIds(): List<String> {
    return fetchIds(state = USER_STATE_UNREAD, excludeState = USER_STATE_READ)
  }

  suspend fun bookmarkIds(): List<String> {
    return fetchIds(state = USER_STATE_STARRED)
  }

  private suspend fun fetchIds(state: String, excludeState: String? = null): List<String> =
    withContext(dispatchersProvider.io) {
      authenticatedHttpClient()
        .get(Reader.ItemIds(state = state, excludeState = excludeState))
        .body<ItemIds>()
        .itemRefs
        .map { (id) ->
          return@map toIdString(id)
        }
    }

  suspend fun authenticatedHttpClient(): HttpClient {
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
              header("Authorization", "GoogleLogin auth=${authToken.trim()}")
            }
          }
        }
        .also {
          _authenticatedHttpClient = it
          cachedUserId = user.id
        }
    }
  }

  private fun toIdString(id: String): String {
    val signedId = id.toLong()
    return "tag:google.com,2005:reader/item/" + signedId.toString(radix = 16).padStart(16, '0')
  }
}
