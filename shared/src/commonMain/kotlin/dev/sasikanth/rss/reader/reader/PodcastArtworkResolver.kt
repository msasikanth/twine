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
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils

fun ResolvedPost.resolvePodcastHeroArtworkUrl(enrichedCoverArtUrl: String? = null): String? {
  return sequenceOf(imageUrl, enrichedCoverArtUrl, feedCoverArtUrl)
    .mapNotNull { candidate -> candidate?.trim()?.takeIf { it.isNotEmpty() } }
    .firstOrNull { candidate -> !UrlUtils.isLowResFaviconUrl(candidate) }
}

fun ResolvedPost.resolveDynamicThemeSeedUrl(enrichedCoverArtUrl: String? = null): String? {
  return if (!audioUrl.isNullOrBlank()) {
    resolvePodcastHeroArtworkUrl(enrichedCoverArtUrl) ?: feedIcon.trim().takeIf { it.isNotEmpty() }
  } else {
    imageUrl?.trim()?.takeIf { it.isNotEmpty() }
  }
}
