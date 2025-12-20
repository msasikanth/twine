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

package dev.sasikanth.rss.reader.home.ui

import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json

@Serializable
data class PostListKey(
  val postId: String,
  val feedId: String,
) {

  fun encode(): String = json.encodeToString(this)

  companion object {

    fun from(post: PostWithMetadata): PostListKey {
      return PostListKey(postId = post.id, feedId = post.sourceId)
    }

    fun decode(key: String): PostListKey = json.decodeFromString(key)
  }
}
