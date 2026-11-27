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
import kotlin.time.Instant

class ReaderContentTypeTest {

  @Test
  fun classifiesStandardArticleLinkAsArticle() {
    val post = samplePost(link = "https://example.com/posts/kotlin-kmp", audioUrl = null)

    assertEquals(
      ReaderContentType.Article,
      post.classifyReaderContent(isAudioPlayerAvailable = true),
    )
  }

  @Test
  fun classifiesAudioPostAsPodcastEvenWhenPlayerUnavailable() {
    val audioUrl = "https://cdn.example.com/episodes/ep-42.mp3"
    val post = samplePost(link = "https://example.com/episodes/42", audioUrl = audioUrl)

    assertEquals(
      ReaderContentType.Podcast(audioUrl = audioUrl, isPlayerAvailable = false),
      post.classifyReaderContent(isAudioPlayerAvailable = false),
    )
    assertEquals(
      ReaderContentType.Podcast(audioUrl = audioUrl, isPlayerAvailable = true),
      post.classifyReaderContent(isAudioPlayerAvailable = true),
    )
  }

  @Test
  fun podcastClassificationTakesPrecedenceOverVideoUrl() {
    val audioUrl = "https://cdn.example.com/episodes/ep-1.mp3"
    val post = samplePost(link = "https://www.youtube.com/watch?v=dQw4w9WgXcQ", audioUrl = audioUrl)

    assertEquals(
      ReaderContentType.Podcast(audioUrl = audioUrl, isPlayerAvailable = true),
      post.classifyReaderContent(isAudioPlayerAvailable = true),
    )
  }

  @Test
  fun classifiesVideoProvidersCorrectly() {
    val youtubeUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
    val vimeoUrl = "https://vimeo.com/123456789"
    val peerTubeUrl = "https://peertube.example.org/videos/watch/abc123_xyz"
    val mp4Url = "https://media.example.org/clips/demo.mp4?token=1"

    assertEquals(
      ReaderContentType.Video(videoUrl = youtubeUrl, providerLabel = "YouTube"),
      samplePost(link = youtubeUrl).classifyReaderContent(isAudioPlayerAvailable = true),
    )
    assertEquals(
      ReaderContentType.Video(videoUrl = vimeoUrl, providerLabel = "Vimeo"),
      samplePost(link = vimeoUrl).classifyReaderContent(isAudioPlayerAvailable = true),
    )
    assertEquals(
      ReaderContentType.Video(videoUrl = peerTubeUrl, providerLabel = "PeerTube"),
      samplePost(link = peerTubeUrl).classifyReaderContent(isAudioPlayerAvailable = true),
    )
    assertEquals(
      ReaderContentType.Video(videoUrl = mp4Url, providerLabel = "Video"),
      samplePost(link = mp4Url).classifyReaderContent(isAudioPlayerAvailable = true),
    )
  }

  @Test
  fun buildVideoUrlAtTimestampHandlesSupportedProviders() {
    assertEquals(
      "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      buildVideoUrlAtTimestamp("https://www.youtube.com/watch?v=dQw4w9WgXcQ", 0L),
    )
    assertEquals(
      "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=95",
      buildVideoUrlAtTimestamp("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=12", 95L),
    )
    assertEquals(
      "https://youtu.be/dQw4w9WgXcQ?t=95",
      buildVideoUrlAtTimestamp("https://youtu.be/dQw4w9WgXcQ", 95L),
    )
    assertEquals(
      "https://www.youtube.com/embed/dQw4w9WgXcQ?start=95",
      buildVideoUrlAtTimestamp("https://www.youtube.com/embed/dQw4w9WgXcQ", 95L),
    )
    assertEquals(
      "https://vimeo.com/123456789#t=95s",
      buildVideoUrlAtTimestamp("https://vimeo.com/123456789", 95L),
    )
    assertEquals(
      "https://peertube.example.org/videos/watch/abc123_xyz?start=95",
      buildVideoUrlAtTimestamp("https://peertube.example.org/videos/watch/abc123_xyz", 95L),
    )
    assertEquals(
      "https://media.example.org/clips/demo.mp4",
      buildVideoUrlAtTimestamp("https://media.example.org/clips/demo.mp4", 95L),
    )
  }

  private fun samplePost(link: String, audioUrl: String? = null): ResolvedPost =
    ResolvedPost(
      id = "post-1",
      sourceId = "source-1",
      title = "Sample Post",
      description = "Sample description",
      imageUrl = null,
      audioUrl = audioUrl,
      date = Instant.fromEpochMilliseconds(0),
      createdAt = Instant.fromEpochMilliseconds(0),
      link = link,
      commentsLink = null,
      flags = emptySet(),
      feedName = "Sample Feed",
      feedIcon = "",
      feedHomepageLink = "https://example.com",
      alwaysFetchFullArticle = false,
      showFeedFavIcon = true,
    )
}
