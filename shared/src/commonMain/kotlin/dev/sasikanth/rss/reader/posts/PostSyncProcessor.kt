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

package dev.sasikanth.rss.reader.posts

import androidx.compose.ui.graphics.toArgb
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.SyncState
import dev.sasikanth.rss.reader.data.utils.Constants
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.ui.SeedColorExtractor
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class PostSyncProcessor(
  private val rssRepository: RssRepository,
  private val syncCoordinator: SyncCoordinator,
  private val seedColorExtractor: SeedColorExtractor,
  dispatchersProvider: DispatchersProvider,
) {

  private val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.io)

  fun observeSyncState() {
    syncCoordinator.syncState
      .onEach { syncState ->
        if (syncState is SyncState.Complete) {
          calculateSeedColors()
        }
      }
      .launchIn(scope)
  }

  private fun calculateSeedColors() {
    scope.launch {
      val posts =
        rssRepository.postsWithImagesAndNoSeedColor(limit = Constants.NUMBER_OF_FEATURED_POSTS * 2)

      posts.forEach { post ->
        if (post.seedColor == null && !post.imageUrl.isNullOrBlank()) {
          val seedColor = seedColorExtractor.calculateSeedColor(post.imageUrl)
          if (seedColor != null) {
            rssRepository.updateSeedColor(seedColor = seedColor.toArgb(), id = post.id)
          }
        }
      }
    }
  }
}
