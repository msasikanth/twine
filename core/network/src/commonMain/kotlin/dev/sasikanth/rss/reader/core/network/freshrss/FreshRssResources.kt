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

@Resource("/accounts/ClientLogin") object Authentication

@Resource("/reader/api/0")
class Reader {

  @Resource("user-info?output=json") class UserInfo(val parent: Reader = Reader())

  @Resource("tag/list?output=json") class Tags(val parent: Reader = Reader())

  @Resource("subscription/list?output=json") class Subscriptions(val parent: Reader = Reader())

  @Resource("stream/contents/reading-list?n={limit}&ot={newerThan}&c={continuation}")
  class Articles(
    val parent: Reader = Reader(),
    val limit: Int = 1000,
    val newerThan: Long = Instant.DISTANT_PAST.toEpochMilliseconds(),
    val continuation: String = ""
  )

  @Resource("edit-tag") class EditTag(val parent: Reader = Reader())

  @Resource("subscription/quickadd?quickadd={url}")
  class AddFeed(val parent: Reader = Reader(), val url: String)

  @Resource("subscription/edit") class EditFeed(val parent: Reader = Reader())

  @Resource("rename-tag") class RenameTag(val parent: Reader = Reader())

  @Resource("disable-tag") class DisableTag(val parent: Reader = Reader())

  @Resource("stream/items/ids?s={state}&n={limit}")
  class ItemIds(val state: String, val limit: Int = 10000)
}
