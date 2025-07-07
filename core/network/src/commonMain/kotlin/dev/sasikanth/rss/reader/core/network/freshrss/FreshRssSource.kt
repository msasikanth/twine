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

import dev.sasikanth.rss.reader.core.model.remote.freshrss.ArticlesPayload
import dev.sasikanth.rss.reader.core.model.remote.freshrss.ItemIds
import dev.sasikanth.rss.reader.core.model.remote.freshrss.SubscriptionsPayload
import dev.sasikanth.rss.reader.core.model.remote.freshrss.TagsPayload
import dev.sasikanth.rss.reader.core.model.remote.freshrss.UserInfoPayload
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import kotlinx.coroutines.withContext

class FreshRssSource(
  httpClient: HttpClient,
  baseEndPoint: String,
  private val authToken: String?,
  private val dispatchersProvider: DispatchersProvider,
) {

  companion object {
    private const val USER_STATE_READ = "user/-/state/com.google/read"
    private const val USER_STATE_STARRED = "user/-/state/com.google/starred"
  }

  private val httpClient =
    httpClient.config {
      followRedirects = true
      defaultRequest {
        url(baseEndPoint)

        if (!(authToken.isNullOrBlank())) {
          header("Authorization", "GoogleLogin auth=$authToken")
        }
      }
    }

  /** @return: auth token for the account */
  suspend fun login(username: String, password: String): String? {
    return withContext(dispatchersProvider.io) {
      httpClient
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

  suspend fun userInfo(): UserInfoPayload {
    return withContext(dispatchersProvider.io) { httpClient.get(Reader.UserInfo()).body() }
  }

  suspend fun tags(): TagsPayload {
    return withContext(dispatchersProvider.io) { httpClient.get(Reader.Tags()).body() }
  }

  suspend fun subscriptions(): SubscriptionsPayload {
    return withContext(dispatchersProvider.io) { httpClient.get(Reader.Subscriptions()).body() }
  }

  suspend fun articles(limit: Int, newerThan: Long, continuation: String? = null): ArticlesPayload {
    return withContext(dispatchersProvider.io) {
      httpClient
        .get(
          Reader.Articles(
            limit = limit,
            newerThan = newerThan,
            continuation = continuation.orEmpty()
          )
        )
        .body()
    }
  }

  suspend fun markArticlesAsRead(ids: List<String>) {
    withContext(dispatchersProvider.io) {
      httpClient.post(Reader.EditTag()) {
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
      httpClient.post(Reader.EditTag()) {
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
    withContext(dispatchersProvider.io) { httpClient.post(Reader.AddFeed(url = url)) }
  }

  suspend fun editFeedName(feedId: String, name: String) {
    withContext(dispatchersProvider.io) {
      httpClient.post(Reader.EditFeed()) {
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
      httpClient.post(Reader.EditFeed()) {
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
      httpClient.post(Reader.EditFeed()) {
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
      httpClient.post(Reader.EditFeed()) {
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
      httpClient.post(Reader.RenameTag()) {
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
      httpClient.post(Reader.DisableTag()) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(FormDataContent(parameters { append("s", tagId) }))
      }
    }
  }

  suspend fun addBookmarks(ids: List<String>) {
    withContext(dispatchersProvider.io) {
      httpClient.post(Reader.EditTag()) {
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
      httpClient.post(Reader.EditTag()) {
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
    return withContext(dispatchersProvider.io) {
      httpClient.get(Reader.ItemIds(state = USER_STATE_READ)).body<ItemIds>().itemRefs.map { (id) ->
        return@map toIdString(id)
      }
    }
  }

  suspend fun bookmarkIds(): List<String> {
    return withContext(dispatchersProvider.io) {
      httpClient.get(Reader.ItemIds(state = USER_STATE_STARRED)).body<ItemIds>().itemRefs.map { (id)
        ->
        return@map toIdString(id)
      }
    }
  }

  private fun toIdString(id: String): String {
    val signedId = id.toLong()
    return signedId.toString(radix = 16).padStart(16, '0')
  }
}
