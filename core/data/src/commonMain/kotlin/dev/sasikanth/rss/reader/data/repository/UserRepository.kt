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

package dev.sasikanth.rss.reader.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.sasikanth.rss.reader.core.model.local.User
import dev.sasikanth.rss.reader.data.database.UserQueries
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UserRepository(
  private val userQueries: UserQueries,
  private val dispatchersProvider: DispatchersProvider,
) {

  suspend fun saveUser(
    id: String,
    name: String,
    email: String,
    avatarUrl: String?,
    token: String,
    refreshToken: String,
    serverUrl: String? = null,
  ) {
    withContext(dispatchersProvider.databaseWrite) {
      userQueries.insert(
        id = id,
        name = name,
        email = email,
        avatarUrl = avatarUrl,
        token = token,
        refreshToken = refreshToken,
        serverUrl = serverUrl,
        lastSyncStatus = "IDLE"
      )
    }
  }

  suspend fun updateLastSyncStatus(lastSyncStatus: String) {
    withContext(dispatchersProvider.databaseWrite) {
      userQueries.updateLastSyncStatus(lastSyncStatus)
    }
  }

  suspend fun updateToken(token: String) {
    withContext(dispatchersProvider.databaseWrite) { userQueries.updateToken(token) }
  }

  suspend fun updateRefreshToken(refreshToken: String) {
    withContext(dispatchersProvider.databaseWrite) { userQueries.updateRefreshToken(refreshToken) }
  }

  fun user(): Flow<User?> {
    return userQueries
      .user(
        mapper = { id, name, email, avatarUrl, token, refreshToken, serverUrl, lastSyncStatus ->
          User(
            id = id,
            name = name,
            email = email,
            avatarUrl = avatarUrl,
            token = token,
            refreshToken = refreshToken,
            serverUrl = serverUrl,
            lastSyncStatus = lastSyncStatus
          )
        }
      )
      .asFlow()
      .mapToOneOrNull(dispatchersProvider.databaseRead)
  }

  fun userBlocking(): User? {
    return with(dispatchersProvider.databaseRead) {
      userQueries
        .user(
          mapper = { id, name, email, avatarUrl, token, refreshToken, serverUrl, lastSyncStatus ->
            User(
              id = id,
              name = name,
              email = email,
              avatarUrl = avatarUrl,
              token = token,
              refreshToken = refreshToken,
              serverUrl = serverUrl,
              lastSyncStatus = lastSyncStatus
            )
          }
        )
        .executeAsOneOrNull()
    }
  }

  suspend fun deleteUser() {
    withContext(dispatchersProvider.databaseWrite) { userQueries.delete() }
  }
}
