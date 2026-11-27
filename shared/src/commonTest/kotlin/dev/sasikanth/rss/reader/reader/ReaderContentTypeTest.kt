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

import dev.sasikanth.rss.reader.core.model.local.MediaChapter
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.isSponsorChapterTitle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
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
  fun extractTimestampChaptersHandlesRangesLinksTrailingAndSponsorTags() {
    val notes =
      """
      - [00:00 - 02:30] Intro and housekeeping
      * **[02:30](https://example.com/listen?t=150)** Sponsor: Linear
      3. Architecture deep dive [12:34]
      Ad break (20:00 - 21:30)
      Q&A with listeners (25:00)
      """
        .trimIndent()

    val chapters = extractTimestampChapters(notes)

    assertEquals(
      listOf(
        MediaChapter(
          timestampMs = 0L,
          endTimestampMs = 150_000L,
          title = "Intro and housekeeping",
          isSponsor = false,
        ),
        MediaChapter(timestampMs = 150_000L, title = "Sponsor: Linear", isSponsor = true),
        MediaChapter(timestampMs = 754_000L, title = "Architecture deep dive", isSponsor = false),
        MediaChapter(
          timestampMs = 1_200_000L,
          endTimestampMs = 1_290_000L,
          title = "Ad break",
          isSponsor = true,
        ),
        MediaChapter(timestampMs = 1_500_000L, title = "Q&A with listeners", isSponsor = false),
      ),
      chapters,
    )
  }

  @Test
  fun extractTimestampChaptersSupportsBareTrailingListAndRejectsClockTimes() {
    val bareTrailingNotes =
      """
      Intro - 00:00
      Brought to you by Sentry — 03:15
      Closing thoughts | 42:10
      """
        .trimIndent()

    val chapters = extractTimestampChapters(bareTrailingNotes)
    assertEquals(
      listOf(
        MediaChapter(timestampMs = 0L, title = "Intro", isSponsor = false),
        MediaChapter(timestampMs = 195_000L, title = "Brought to you by Sentry", isSponsor = true),
        MediaChapter(timestampMs = 2_530_000L, title = "Closing thoughts", isSponsor = false),
      ),
      chapters,
    )

    val clockProse =
      """
      Live stream starts at 10:30 AM PST
      Support hours - 09:00
      Doors open until (18:00)
      """
        .trimIndent()

    assertTrue(extractTimestampChapters(clockProse).isEmpty())
  }

  @Test
  fun sponsorChapterTitleClassifierAvoidsFalsePositives() {
    assertTrue(isSponsorChapterTitle("Sponsor: Linear"))
    assertTrue(isSponsorChapterTitle("[Ad] Vanta"))
    assertTrue(isSponsorChapterTitle("Mid-roll break"))
    assertTrue(isSponsorChapterTitle("Brought to you by Sentry"))
    assertTrue(isSponsorChapterTitle("Commercial break"))
    assertTrue(isSponsorChapterTitle("Paid partnership with Acme"))
    assertTrue(isSponsorChapterTitle("A word from our sponsors"))

    assertFalse(isSponsorChapterTitle("Lead engineer"))
    assertFalse(isSponsorChapterTitle("How we built an ad server"))
    assertFalse(isSponsorChapterTitle("Ad-hoc distributed consensus"))
    assertFalse(isSponsorChapterTitle("How to sponsor open source maintainers"))
    assertFalse(isSponsorChapterTitle("Reading list & outro"))
  }

  @Test
  fun extractTimestampChaptersHandlesNbspAndCollapsedInlineYouTubeSequences() {
    val nbspNotes = "0:00\u00A0Welcome\n00:30\u00A0-\u00A001:45\u00A0Deep Dive"
    assertEquals(
      listOf(
        MediaChapter(timestampMs = 0L, title = "Welcome"),
        MediaChapter(timestampMs = 30_000L, endTimestampMs = 105_000L, title = "Deep Dive"),
      ),
      extractTimestampChapters(nbspNotes),
    )

    val collapsedYoutube =
      "Announcement: https://alpo.ge/remark.pdf 0:00 - Kevin Buzzard 0:45 - Ken Ono 1:14 - Elliot Glazer 1:28 - Jordan Ellenberg ------------------ https://3blue1brown.com"
    assertEquals(
      listOf(
        MediaChapter(timestampMs = 0L, title = "Kevin Buzzard"),
        MediaChapter(timestampMs = 45_000L, title = "Ken Ono"),
        MediaChapter(timestampMs = 74_000L, title = "Elliot Glazer"),
        MediaChapter(timestampMs = 88_000L, title = "Jordan Ellenberg"),
      ),
      extractTimestampChapters(collapsedYoutube),
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
