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

package dev.sasikanth.rss.reader.reader

import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class PodcastArtworkResolverTest {

  @Test
  fun prefersEpisodeImageOverEnrichedAndFeedCoverArt() {
    val post =
      podcastPost(
        imageUrl = "https://cdn.example.com/episode-art.jpg",
        feedCoverArtUrl = "https://cdn.example.com/show-cover.jpg",
      )

    assertEquals(
      "https://cdn.example.com/episode-art.jpg",
      post.resolvePodcastHeroArtworkUrl("https://cdn.example.com/enriched.jpg"),
    )
  }

  @Test
  fun rejectsLowResFaviconsAndFallsBackToEnrichedThenFeedCoverArt() {
    val enrichedPost =
      podcastPost(
        imageUrl = "https://icon.horse/icon/example.com",
        feedCoverArtUrl = "https://cdn.example.com/show-cover.jpg",
      )
    assertEquals(
      "https://cdn.example.com/enriched.jpg",
      enrichedPost.resolvePodcastHeroArtworkUrl("https://cdn.example.com/enriched.jpg"),
    )

    val feedCoverPost =
      podcastPost(
        imageUrl = "data:image/png;base64,AAAA",
        feedCoverArtUrl = "https://cdn.example.com/show-cover.jpg",
      )
    assertEquals(
      "https://cdn.example.com/show-cover.jpg",
      feedCoverPost.resolvePodcastHeroArtworkUrl("https://www.google.com/s2/favicons?domain=ex.com"),
    )
  }

  @Test
  fun returnsNullWhenAllCandidatesAreLowResOrBlank() {
    val post =
      podcastPost(
        imageUrl = "https://t1.gstatic.com/faviconV2?client=SOCIAL",
        feedCoverArtUrl = "   ",
        feedIcon = "https://icon.horse/icon/example.com",
      )

    assertNull(post.resolvePodcastHeroArtworkUrl("data:image/svg+xml,<svg/>"))
  }

  @Test
  fun dynamicThemeSeedUrlFallsBackToFeedIconOnlyForPodcasts() {
    val podcastWithoutArt =
      podcastPost(
        imageUrl = null,
        feedCoverArtUrl = null,
        feedIcon = "https://icon.horse/icon/example.com",
      )
    assertEquals(
      "https://icon.horse/icon/example.com",
      podcastWithoutArt.resolveDynamicThemeSeedUrl(),
    )

    val podcastWithArt =
      podcastPost(
        imageUrl = null,
        feedCoverArtUrl = "https://cdn.example.com/show-cover.jpg",
        feedIcon = "https://icon.horse/icon/example.com",
      )
    assertEquals(
      "https://cdn.example.com/show-cover.jpg",
      podcastWithArt.resolveDynamicThemeSeedUrl(),
    )

    val articleWithoutImage =
      podcastPost(
          imageUrl = null,
          feedCoverArtUrl = "https://cdn.example.com/show-cover.jpg",
          feedIcon = "https://icon.horse/icon/example.com",
        )
        .copy(audioUrl = null)
    assertNull(articleWithoutImage.resolveDynamicThemeSeedUrl())
  }

  private fun podcastPost(
    imageUrl: String?,
    feedCoverArtUrl: String? = null,
    feedIcon: String = "",
  ): ResolvedPost =
    ResolvedPost(
      id = "pod-1",
      sourceId = "feed-1",
      title = "Episode 1",
      description = "Show notes",
      imageUrl = imageUrl,
      audioUrl = "https://cdn.example.com/ep1.mp3",
      date = Instant.fromEpochMilliseconds(0),
      createdAt = Instant.fromEpochMilliseconds(0),
      link = "https://example.com/ep1",
      commentsLink = null,
      flags = emptySet(),
      feedName = "Podcast Show",
      feedIcon = feedIcon,
      feedHomepageLink = "https://example.com",
      alwaysFetchFullArticle = false,
      showFeedFavIcon = false,
      feedCoverArtUrl = feedCoverArtUrl,
      feedLink = "https://example.com/feed.xml",
    )
}
