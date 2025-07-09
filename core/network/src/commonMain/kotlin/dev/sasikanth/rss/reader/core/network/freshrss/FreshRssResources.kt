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

package dev.sasikanth.rss.reader.core.network.freshrss

import io.ktor.resources.Resource
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Resource("/accounts/ClientLogin") object Authentication

@Resource("/reader/api/0")
class Reader {

  @Resource("user-info")
  class UserInfo(
    val parent: Reader = Reader(),
    val output: String = "json",
  )

  @Resource("tag/list")
  class Tags(
    val parent: Reader = Reader(),
    val output: String = "json",
  )

  @Resource("subscription/list")
  class Subscriptions(
    val parent: Reader = Reader(),
    val output: String = "json",
  )

  @Serializable
  @Resource("stream/contents/reading-list")
  class Articles(
    val parent: Reader = Reader(),
    @SerialName("n") val limit: Int = 1000,
    @SerialName("ot") val newerThan: Long = Instant.DISTANT_PAST.toEpochMilliseconds(),
    @SerialName("c") val continuation: String = ""
  )

  @Resource("edit-tag") class EditTag(val parent: Reader = Reader())

  @Resource("subscription/quickadd")
  class AddFeed(
    val parent: Reader = Reader(),
    val quickadd: String = "",
  )

  @Resource("subscription/edit") class EditFeed(val parent: Reader = Reader())

  @Resource("rename-tag") class RenameTag(val parent: Reader = Reader())

  @Resource("disable-tag") class DisableTag(val parent: Reader = Reader())

  @Serializable
  @Resource("stream/items/ids")
  class ItemIds(
    val parent: Reader = Reader(),
    @SerialName("s") val state: String,
    @SerialName("n") val limit: Int = 10000
  )
}
