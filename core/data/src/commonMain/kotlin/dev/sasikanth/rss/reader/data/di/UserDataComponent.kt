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

package dev.sasikanth.rss.reader.data.di

import dev.sasikanth.rss.reader.core.model.local.User
import dev.sasikanth.rss.reader.data.repository.UserRepository
import me.tatarka.inject.annotations.Provides

interface UserDataComponent {

  @Provides
  fun providesUserProvider(userRepository: UserRepository): User? {
    return userRepository.userBlocking()
  }
}
