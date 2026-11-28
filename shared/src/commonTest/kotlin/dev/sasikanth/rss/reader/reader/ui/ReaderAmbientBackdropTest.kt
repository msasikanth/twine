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

package dev.sasikanth.rss.reader.reader.ui

import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ReaderAmbientBackdropTest {

  private fun post(
    id: String = "post-1",
    link: String = "https://example.com/post",
    imageUrl: String? = null,
    audioUrl: String? = null,
    feedCoverArtUrl: String? = null,
  ) =
    ResolvedPost(
      id = id,
      sourceId = "source-1",
      title = "Title",
      description = "Description",
      link = link,
      imageUrl = imageUrl,
      audioUrl = audioUrl,
      date = Instant.fromEpochMilliseconds(0),
      createdAt = Instant.fromEpochMilliseconds(0),
      commentsLink = null,
      flags = emptySet(),
      feedName = "Feed",
      feedIcon = "https://example.com/icon.png",
      feedHomepageLink = "https://example.com",
      alwaysFetchFullArticle = false,
      showFeedFavIcon = true,
      feedContentReadingTime = 3,
      feedCoverArtUrl = feedCoverArtUrl,
    )

  @Test
  fun resolveReaderAmbientBackdropSpec_podcastCases() {
    val podcastWithEpisodeArt =
      post(
          imageUrl = "https://example.com/episode.jpg",
          audioUrl = "https://example.com/audio.mp3",
          feedCoverArtUrl = "https://example.com/feed.jpg",
        )
        .resolveReaderAmbientBackdropSpec(isDarkTheme = false)
    assertEquals("https://example.com/episode.jpg", podcastWithEpisodeArt.imageUrl)
    assertEquals(1f, podcastWithEpisodeArt.maxAlpha)
    assertTrue(podcastWithEpisodeArt.allowTonalFallback)

    val podcastWithEnrichedArt =
      post(audioUrl = "https://example.com/audio.mp3")
        .resolveReaderAmbientBackdropSpec(
          isDarkTheme = true,
          enrichedCoverArtUrl = "https://example.com/enriched.jpg",
        )
    assertEquals("https://example.com/enriched.jpg", podcastWithEnrichedArt.imageUrl)
    assertEquals(1f, podcastWithEnrichedArt.maxAlpha)
    assertTrue(podcastWithEnrichedArt.allowTonalFallback)

    val podcastWithoutArt =
      post(audioUrl = "https://example.com/audio.mp3")
        .resolveReaderAmbientBackdropSpec(isDarkTheme = true)
    assertNull(podcastWithoutArt.imageUrl)
    assertEquals(1f, podcastWithoutArt.maxAlpha)
    assertTrue(podcastWithoutArt.allowTonalFallback)
  }

  @Test
  fun resolveReaderAmbientBackdropSpec_videoCases() {
    val youtubePost = post(link = "https://www.youtube.com/watch?v=dQw4w9WgXcQ")

    val darkSpec = youtubePost.resolveReaderAmbientBackdropSpec(isDarkTheme = true)
    assertEquals("https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg", darkSpec.imageUrl)
    assertEquals(0.65f, darkSpec.maxAlpha)
    assertTrue(darkSpec.allowTonalFallback)

    val lightSpec = youtubePost.resolveReaderAmbientBackdropSpec(isDarkTheme = false)
    assertEquals("https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg", lightSpec.imageUrl)
    assertEquals(0.65f, lightSpec.maxAlpha)
    assertTrue(lightSpec.allowTonalFallback)
  }

  @Test
  fun resolveReaderAmbientBackdropSpec_articleCases() {
    val articleWithImage = post(imageUrl = "  https://example.com/hero.jpg  ")
    val darkArticle = articleWithImage.resolveReaderAmbientBackdropSpec(isDarkTheme = true)
    assertEquals("https://example.com/hero.jpg", darkArticle.imageUrl)
    assertEquals(0.65f, darkArticle.maxAlpha)
    assertFalse(darkArticle.allowTonalFallback)

    val lightArticle = articleWithImage.resolveReaderAmbientBackdropSpec(isDarkTheme = false)
    assertEquals("https://example.com/hero.jpg", lightArticle.imageUrl)
    assertEquals(0.65f, lightArticle.maxAlpha)
    assertFalse(lightArticle.allowTonalFallback)

    val textOnlyArticle = post(imageUrl = null).resolveReaderAmbientBackdropSpec(isDarkTheme = true)
    assertNull(textOnlyArticle.imageUrl)
    assertEquals(0f, textOnlyArticle.maxAlpha)
    assertFalse(textOnlyArticle.allowTonalFallback)

    val blankImageArticle =
      post(imageUrl = "   ").resolveReaderAmbientBackdropSpec(isDarkTheme = false)
    assertNull(blankImageArticle.imageUrl)
    assertEquals(0f, blankImageArticle.maxAlpha)
    assertFalse(blankImageArticle.allowTonalFallback)
  }

  @Test
  fun resolveReaderBackdropScrollVisibility_computesScrollFadeForListAndScrollState() {
    assertEquals(
      1f,
      resolveReaderBackdropScrollVisibility(
        firstVisibleItemIndex = 0,
        firstVisibleItemScrollOffset = 0,
        fadeDistancePx = 200f,
      ),
    )
    assertEquals(
      0.5f,
      resolveReaderBackdropScrollVisibility(
        firstVisibleItemIndex = 0,
        firstVisibleItemScrollOffset = 100,
        fadeDistancePx = 200f,
      ),
    )
    assertEquals(
      0f,
      resolveReaderBackdropScrollVisibility(
        firstVisibleItemIndex = 0,
        firstVisibleItemScrollOffset = 200,
        fadeDistancePx = 200f,
      ),
    )
    assertEquals(
      0f,
      resolveReaderBackdropScrollVisibility(
        firstVisibleItemIndex = 0,
        firstVisibleItemScrollOffset = 350,
        fadeDistancePx = 200f,
      ),
    )
    assertEquals(
      0f,
      resolveReaderBackdropScrollVisibility(
        firstVisibleItemIndex = 1,
        firstVisibleItemScrollOffset = 0,
        fadeDistancePx = 200f,
      ),
    )
    assertEquals(
      0f,
      resolveReaderBackdropScrollVisibility(
        firstVisibleItemIndex = 0,
        firstVisibleItemScrollOffset = 0,
        fadeDistancePx = 0f,
      ),
    )

    assertEquals(
      1f,
      resolveReaderBackdropScrollVisibility(scrollOffsetPx = 0, fadeDistancePx = 200f),
    )
    assertEquals(
      0.5f,
      resolveReaderBackdropScrollVisibility(scrollOffsetPx = 100, fadeDistancePx = 200f),
    )
    assertEquals(
      0f,
      resolveReaderBackdropScrollVisibility(scrollOffsetPx = 250, fadeDistancePx = 200f),
    )
    assertEquals(
      0f,
      resolveReaderBackdropScrollVisibility(scrollOffsetPx = 0, fadeDistancePx = -10f),
    )
  }

  @Test
  fun resolveReaderAmbientBlendState_handlesEmptyAndSinglePage() {
    val empty =
      resolveReaderAmbientBlendState(exactPage = 0f, itemCount = 0) {
        ReaderAmbientPageTarget(1f, 0f)
      }
    assertEquals(0f, empty.containerBlurredAlpha)
    assertEquals(0f, empty.fallbackAlpha)
    assertEquals(-1, empty.leftPage)
    assertEquals(-1, empty.rightPage)
    assertEquals(0f, empty.imageAlphaForPage(0))

    val single =
      resolveReaderAmbientBlendState(exactPage = 0f, itemCount = 1) {
        ReaderAmbientPageTarget(blurredAlpha = 0.45f, fallbackAlpha = 0.1f)
      }
    assertEquals(0.45f, single.containerBlurredAlpha, 1e-5f)
    assertEquals(0.1f, single.fallbackAlpha, 1e-5f)
    assertEquals(0, single.leftPage)
    assertEquals(1f, single.imageAlphaForPage(0), 1e-5f)
  }

  @Test
  fun resolveReaderAmbientBlendState_settledAndCrossfadeCases() {
    val settledLeft =
      resolveReaderAmbientBlendState(exactPage = 0f, itemCount = 2) { page ->
        if (page == 0) ReaderAmbientPageTarget(1f, 0f) else ReaderAmbientPageTarget(0.45f, 0f)
      }
    assertEquals(1f, settledLeft.containerBlurredAlpha, 1e-5f)
    assertEquals(1f, settledLeft.imageAlphaForPage(0), 1e-5f)
    assertEquals(0f, settledLeft.imageAlphaForPage(1), 1e-5f)

    val settledRight =
      resolveReaderAmbientBlendState(exactPage = 1f, itemCount = 2) { page ->
        if (page == 0) ReaderAmbientPageTarget(1f, 0f) else ReaderAmbientPageTarget(0.45f, 0f)
      }
    assertEquals(0.45f, settledRight.containerBlurredAlpha, 1e-5f)
    assertEquals(0f, settledRight.imageAlphaForPage(0), 1e-5f)
    assertEquals(1f, settledRight.imageAlphaForPage(1), 1e-5f)

    val equalMid =
      resolveReaderAmbientBlendState(exactPage = 0.5f, itemCount = 2) {
        ReaderAmbientPageTarget(1f, 0f)
      }
    assertEquals(1f, equalMid.containerBlurredAlpha, 1e-5f)
    assertEquals(1f, equalMid.imageAlphaForPage(0), 1e-5f)
    assertEquals(0.5f, equalMid.imageAlphaForPage(1), 1e-5f)

    val mixedMid =
      resolveReaderAmbientBlendState(exactPage = 0.5f, itemCount = 2) { page ->
        if (page == 0) ReaderAmbientPageTarget(1f, 0f) else ReaderAmbientPageTarget(0.45f, 0f)
      }
    assertEquals(0.725f, mixedMid.containerBlurredAlpha, 1e-5f)
    assertEquals(1f, mixedMid.imageAlphaForPage(0), 1e-5f)
    assertEquals(0.45f / 1.45f, mixedMid.imageAlphaForPage(1), 1e-5f)
  }

  @Test
  fun resolveReaderAmbientBlendState_imageToTextAndScrolledAwayAndFallback() {
    val imageToText =
      resolveReaderAmbientBlendState(exactPage = 0.5f, itemCount = 2) { page ->
        if (page == 0) ReaderAmbientPageTarget(1f, 0f) else ReaderAmbientPageTarget(0f, 0f)
      }
    assertEquals(0.5f, imageToText.containerBlurredAlpha, 1e-5f)
    assertEquals(1f, imageToText.imageAlphaForPage(0), 1e-5f)
    assertEquals(0f, imageToText.imageAlphaForPage(1), 1e-5f)

    val textToImage =
      resolveReaderAmbientBlendState(exactPage = 0.5f, itemCount = 2) { page ->
        if (page == 0) ReaderAmbientPageTarget(0f, 0f) else ReaderAmbientPageTarget(0.45f, 0f)
      }
    assertEquals(0.225f, textToImage.containerBlurredAlpha, 1e-5f)
    assertEquals(0f, textToImage.imageAlphaForPage(0), 1e-5f)
    assertEquals(1f, textToImage.imageAlphaForPage(1), 1e-5f)

    val scrolledLeftToUnscrolledRight =
      resolveReaderAmbientBlendState(exactPage = 0.5f, itemCount = 2) { page ->
        if (page == 0) ReaderAmbientPageTarget(0.01f, 0f) else ReaderAmbientPageTarget(1f, 0f)
      }
    assertEquals(0.505f, scrolledLeftToUnscrolledRight.containerBlurredAlpha, 1e-5f)
    assertEquals(1f, scrolledLeftToUnscrolledRight.imageAlphaForPage(0), 1e-5f)
    assertEquals(1f / 1.01f, scrolledLeftToUnscrolledRight.imageAlphaForPage(1), 1e-5f)

    val fallbackToImage =
      resolveReaderAmbientBlendState(exactPage = 0.5f, itemCount = 2) { page ->
        if (page == 0) ReaderAmbientPageTarget(0f, 1f) else ReaderAmbientPageTarget(0.45f, 0f)
      }
    assertEquals(0.225f, fallbackToImage.containerBlurredAlpha, 1e-5f)
    assertEquals(0.5f, fallbackToImage.fallbackAlpha, 1e-5f)
    assertEquals(0f, fallbackToImage.imageAlphaForPage(0), 1e-5f)
    assertEquals(1f, fallbackToImage.imageAlphaForPage(1), 1e-5f)
  }
}
