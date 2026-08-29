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

package dev.sasikanth.rss.reader.core.network.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UrlUtilsTest {

  @Test
  fun isUnconstrainedMedia_shouldReturnTrueForSupportedPrefixes() {
    assertTrue(UrlUtils.isUnconstrainedMedia("https://imgs.xkcd.com/comics/some_comic.png"))
    assertTrue(UrlUtils.isUnconstrainedMedia("https://preview.redd.it/some_image.jpg"))
    assertTrue(UrlUtils.isUnconstrainedMedia("https://i.redd.it/some_image.png"))
    assertTrue(UrlUtils.isUnconstrainedMedia("https://external-preview.redd.it/some_image.jpg"))
  }

  @Test
  fun isUnconstrainedMedia_shouldReturnFalseForUnsupportedPrefixes() {
    assertFalse(UrlUtils.isUnconstrainedMedia("https://example.com/image.png"))
    assertFalse(UrlUtils.isUnconstrainedMedia("https://www.reddit.com/r/pics/"))
  }

  @Test
  fun isYouTubeLink_shouldReturnTrueForYouTubeLinks() {
    assertTrue(UrlUtils.isYouTubeLink("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
    assertTrue(UrlUtils.isYouTubeLink("https://youtu.be/dQw4w9WgXcQ"))
    assertTrue(UrlUtils.isYouTubeLink("https://m.youtube.com/watch?v=dQw4w9WgXcQ"))
    assertTrue(UrlUtils.isYouTubeLink("https://www.youtube.com/embed/dQw4w9WgXcQ"))
  }

  @Test
  fun isYouTubeLink_shouldReturnFalseForNonYouTubeLinks() {
    assertFalse(UrlUtils.isYouTubeLink("https://www.google.com"))
    assertFalse(UrlUtils.isYouTubeLink("https://vimeo.com/12345"))
  }

  @Test
  fun youTubeVideoId_shouldExtractIdFromSupportedLinkShapes() {
    val expectedId = "dQw4w9WgXcQ"

    assertEquals(expectedId, UrlUtils.youTubeVideoId("https://www.youtube.com/watch?v=$expectedId"))
    assertEquals(expectedId, UrlUtils.youTubeVideoId("https://m.youtube.com/watch?v=$expectedId"))
    assertEquals(expectedId, UrlUtils.youTubeVideoId("https://youtube.com/watch?v=$expectedId"))
    assertEquals(expectedId, UrlUtils.youTubeVideoId("https://youtu.be/$expectedId"))
    assertEquals(expectedId, UrlUtils.youTubeVideoId("https://www.youtube.com/embed/$expectedId"))
    assertEquals(expectedId, UrlUtils.youTubeVideoId("https://www.youtube.com/shorts/$expectedId"))
    assertEquals(expectedId, UrlUtils.youTubeVideoId("https://www.youtube.com/live/$expectedId"))
    assertEquals(
      expectedId,
      UrlUtils.youTubeVideoId("https://www.youtube-nocookie.com/embed/$expectedId"),
    )
  }

  @Test
  fun youTubeVideoId_shouldExtractIdWhenLinkHasExtraParams() {
    val expectedId = "tDLbO9KeddY"

    assertEquals(
      expectedId,
      UrlUtils.youTubeVideoId("https://www.youtube.com/watch?v=$expectedId&t=42s"),
    )
    assertEquals(
      expectedId,
      UrlUtils.youTubeVideoId("https://www.youtube.com/watch?app=desktop&v=$expectedId"),
    )
    assertEquals(expectedId, UrlUtils.youTubeVideoId("https://youtu.be/$expectedId?t=42"))
  }

  @Test
  fun youTubeVideoId_shouldReturnNullForNonVideoLinks() {
    assertNull(UrlUtils.youTubeVideoId(null))
    assertNull(UrlUtils.youTubeVideoId(""))
    assertNull(UrlUtils.youTubeVideoId("https://www.youtube.com"))
    assertNull(UrlUtils.youTubeVideoId("https://www.youtube.com/@TomScottGo"))
    assertNull(UrlUtils.youTubeVideoId("https://vimeo.com/12345"))
    assertNull(UrlUtils.youTubeVideoId("https://www.youtube.com/watch?v=tooshort"))
  }

  @Test
  fun youTubeThumbnail_shouldBuildThumbnailForVideoLinks() {
    assertEquals(
      "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
      UrlUtils.youTubeThumbnail("https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
    )
    assertNull(UrlUtils.youTubeThumbnail("https://example.com/article"))
  }

  @Test
  fun prefersFeedProvidedIcon_shouldReturnTrueForSourcesWithSharedFavIcons() {
    assertTrue(
      UrlUtils.prefersFeedProvidedIcon(
        "https://www.youtube.com/feeds/videos.xml?playlist_id=UULFBJycsmduvYEL83R_U4JriQ"
      )
    )
    assertTrue(UrlUtils.prefersFeedProvidedIcon("https://www.reddit.com/r/formula1/.rss"))
    assertTrue(UrlUtils.prefersFeedProvidedIcon("https://www.reddit.com/user/spez/.rss"))
    assertTrue(UrlUtils.prefersFeedProvidedIcon("https://mastodon.social/@Gargron.rss"))
    assertTrue(UrlUtils.prefersFeedProvidedIcon("https://hachyderm.io/@some_user.rss"))
  }

  @Test
  fun prefersFeedProvidedIcon_shouldReturnFalseForOrdinaryFeeds() {
    assertFalse(UrlUtils.prefersFeedProvidedIcon("https://sasikanth.dev/rss.xml"))
    assertFalse(UrlUtils.prefersFeedProvidedIcon("https://www.youtube.com/@mkbhd"))
    assertFalse(UrlUtils.prefersFeedProvidedIcon("https://www.reddit.com/r/formula1/"))
    assertFalse(UrlUtils.prefersFeedProvidedIcon("https://example.com/feed/@notahandle/posts.xml"))
    assertFalse(UrlUtils.prefersFeedProvidedIcon(""))
  }

  @Test
  fun upgradeYouTubeThumbnail_shouldUpgradeLowResVariants() {
    val expected = "https://i3.ytimg.com/vi/v-_d2e7x4KA/maxresdefault.jpg"

    assertEquals(expected, UrlUtils.upgradeYouTubeThumbnail(expected))
    assertEquals(
      expected,
      UrlUtils.upgradeYouTubeThumbnail("https://i3.ytimg.com/vi/v-_d2e7x4KA/hqdefault.jpg"),
    )
    assertEquals(
      expected,
      UrlUtils.upgradeYouTubeThumbnail("https://i3.ytimg.com/vi/v-_d2e7x4KA/mqdefault.jpg"),
    )
    assertEquals(
      expected,
      UrlUtils.upgradeYouTubeThumbnail("https://i3.ytimg.com/vi/v-_d2e7x4KA/sddefault.jpg"),
    )
  }

  @Test
  fun upgradeYouTubeThumbnail_shouldLeaveOtherImagesUntouched() {
    val otherImage = "https://example.com/media/hqdefault.jpg"

    assertEquals(otherImage, UrlUtils.upgradeYouTubeThumbnail(otherImage))
    assertNull(UrlUtils.upgradeYouTubeThumbnail(null))
  }

  @Test
  fun youTubeThumbnailFallback_shouldDowngradeOnlyMaxResThumbnails() {
    assertEquals(
      "https://i3.ytimg.com/vi/v-_d2e7x4KA/hqdefault.jpg",
      UrlUtils.youTubeThumbnailFallback("https://i3.ytimg.com/vi/v-_d2e7x4KA/maxresdefault.jpg"),
    )
    assertNull(
      UrlUtils.youTubeThumbnailFallback("https://i3.ytimg.com/vi/v-_d2e7x4KA/hqdefault.jpg")
    )
    assertNull(UrlUtils.youTubeThumbnailFallback("https://example.com/image.jpg"))
    assertNull(UrlUtils.youTubeThumbnailFallback(null))
  }
}
