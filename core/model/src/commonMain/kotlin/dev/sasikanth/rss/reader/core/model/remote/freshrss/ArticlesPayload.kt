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

package dev.sasikanth.rss.reader.core.model.remote.freshrss

import kotlinx.serialization.Serializable

@Serializable
data class ArticlesPayload(
  val id: String = "",
  val updated: Long = 0,
  val items: List<ArticlePayload> = emptyList(),
  val continuation: String? = null,
)

@Serializable
data class ArticlePayload(
  val id: String,
  val crawlTimeMsec: String = "",
  val timestampUsec: String = "",
  val published: Long = 0,
  val title: String = "",
  val canonical: List<LinksPayload> = emptyList(),
  val alternate: List<LinksPayload> = emptyList(),
  val origin: OriginPayload = OriginPayload(),
  val summary: SummaryPayload = SummaryPayload(),
  val author: String? = null,
)

@Serializable
data class OriginPayload(
  val streamId: String = "",
  val htmlUrl: String = "",
  val title: String = "",
)

@Serializable
data class SummaryPayload(
  val content: String = "",
)

@Serializable
data class LinksPayload(
  val href: String = "",
)
