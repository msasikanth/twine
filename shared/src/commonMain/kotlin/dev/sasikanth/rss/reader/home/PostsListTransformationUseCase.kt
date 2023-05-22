/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.home

import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

private const val NUMBER_OF_FEATURED_POSTS = 6

@Inject
class PostsListTransformationUseCase(private val dispatchersProvider: DispatchersProvider) {

  suspend fun transform(
    postsList: List<PostWithMetadata>
  ): Pair<ImmutableList<PostWithMetadata>, ImmutableList<PostWithMetadata>> {
    return withContext(dispatchersProvider.default) {
      val featuredPosts =
        postsList.filter { it.imageUrl != null }.take(NUMBER_OF_FEATURED_POSTS).toImmutableList()
      val posts = (postsList - featuredPosts).toImmutableList()

      featuredPosts to posts
    }
  }
}
