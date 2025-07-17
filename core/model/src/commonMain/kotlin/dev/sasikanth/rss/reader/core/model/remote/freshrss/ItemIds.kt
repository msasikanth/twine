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

@Serializable data class ItemIds(val itemRefs: List<ItemRef>)

@Serializable data class ItemRef(val id: String)
