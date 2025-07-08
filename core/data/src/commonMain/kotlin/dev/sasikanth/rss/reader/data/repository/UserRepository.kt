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
import app.cash.sqldelight.coroutines.mapToOne
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

  suspend fun createUser(
    id: String,
    name: String,
    profileId: String,
    email: String,
    token: String,
    serverUrl: String,
  ) {
    withContext(dispatchersProvider.databaseWrite) {
      val user = userBlocking()
      if (user == null) return@withContext

      userQueries.insert(id, name, profileId, email, token, serverUrl)
    }
  }

  fun user(): Flow<User?> {
    return userQueries.user(mapper = ::User).asFlow().mapToOne(dispatchersProvider.databaseRead)
  }

  fun userBlocking(): User? {
    return userQueries.user(mapper = ::User).executeAsOneOrNull()
  }

  suspend fun deleteUser() {
    withContext(dispatchersProvider.databaseWrite) { userQueries.delete() }
  }
}
