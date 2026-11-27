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

  @Test
  fun isLowResFaviconUrl_shouldDetectLowResAndBlankUrls() {
    assertTrue(UrlUtils.isLowResFaviconUrl(null))
    assertTrue(UrlUtils.isLowResFaviconUrl(""))
    assertTrue(UrlUtils.isLowResFaviconUrl("   "))
    assertTrue(UrlUtils.isLowResFaviconUrl("data:image/png;base64,iVBORw0KGgo="))
    assertTrue(UrlUtils.isLowResFaviconUrl("DATA:image/x-icon;base64,AAAA"))
    assertTrue(UrlUtils.isLowResFaviconUrl("https://icon.horse/icon/example.com"))
    assertTrue(
      UrlUtils.isLowResFaviconUrl("https://www.google.com/s2/favicons?domain=example.com&sz=180")
    )
    assertTrue(UrlUtils.isLowResFaviconUrl("https://t0.gstatic.com/faviconV2?client=SOCIAL"))

    assertFalse(UrlUtils.isLowResFaviconUrl("https://example.com/podcast-cover.jpg"))
    assertFalse(UrlUtils.isLowResFaviconUrl("https://cdn.example.com/favicon-3000.png"))
  }

  @Test
  fun vimeoVideoId_shouldExtractIdsFromSupportedUrlShapes() {
    assertEquals("123456789", UrlUtils.vimeoVideoId("https://vimeo.com/123456789"))
    assertEquals("123456789", UrlUtils.vimeoVideoId("https://vimeo.com/123456789?share=copy"))
    assertEquals(
      "123456789",
      UrlUtils.vimeoVideoId("https://player.vimeo.com/video/123456789?h=abc"),
    )
    assertEquals(
      "123456789",
      UrlUtils.vimeoVideoId("https://vimeo.com/channels/staffpicks/123456789"),
    )
    assertEquals(
      "123456789",
      UrlUtils.vimeoVideoId("https://vimeo.com/groups/shortfilms/videos/123456789"),
    )
    assertEquals("123456789", UrlUtils.vimeoVideoId("https://vimeo.com/album/42/video/123456789"))

    assertNull(UrlUtils.vimeoVideoId(null))
    assertNull(UrlUtils.vimeoVideoId("https://vimeo.com/channels/staffpicks"))
    assertNull(UrlUtils.vimeoVideoId("https://example.com/123456789"))
  }

  @Test
  fun isPeerTubeLink_shouldMatchPeerTubeUrlsOnly() {
    assertTrue(UrlUtils.isPeerTubeLink("https://framatube.org/w/u6s6N8fT6q8j7kL9mN2pQ3"))
    assertTrue(
      UrlUtils.isPeerTubeLink(
        "https://peertube.tv/videos/watch/9c9de5e8-0a1e-4b3a-9c12-1234567890ab"
      )
    )
    assertTrue(
      UrlUtils.isPeerTubeLink(
        "https://tilvids.com/videos/embed/9c9de5e8-0a1e-4b3a-9c12-1234567890ab?autoplay=1"
      )
    )

    assertFalse(UrlUtils.isPeerTubeLink(null))
    assertFalse(UrlUtils.isPeerTubeLink("https://en.wikipedia.org/w/index.php?title=RSS"))
    assertFalse(UrlUtils.isPeerTubeLink("https://example.com/blog/watch/article"))
  }

  @Test
  fun isDirectVideoUrl_and_videoProviderName_shouldClassifyVideoLinks() {
    assertTrue(UrlUtils.isDirectVideoUrl("https://cdn.example.com/video.mp4"))
    assertTrue(UrlUtils.isDirectVideoUrl("https://cdn.example.com/clip.WEBM?token=1#t=5"))
    assertTrue(UrlUtils.isDirectVideoUrl("https://cdn.example.com/stream.m3u8"))
    assertFalse(UrlUtils.isDirectVideoUrl("https://cdn.example.com/episode.mp3"))

    assertEquals(
      "YouTube",
      UrlUtils.videoProviderName("https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
    )
    assertEquals("Vimeo", UrlUtils.videoProviderName("https://vimeo.com/123456789"))
    assertEquals(
      "PeerTube",
      UrlUtils.videoProviderName("https://framatube.org/w/u6s6N8fT6q8j7kL9mN2pQ3"),
    )
    assertEquals("Video", UrlUtils.videoProviderName("https://cdn.example.com/video.mov"))
    assertNull(UrlUtils.videoProviderName("https://www.youtube.com/@TomScottGo"))
    assertNull(UrlUtils.videoProviderName("https://example.com/article"))
  }
}
