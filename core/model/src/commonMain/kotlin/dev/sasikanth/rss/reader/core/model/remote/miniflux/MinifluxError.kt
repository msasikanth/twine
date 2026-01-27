/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.core.model.remote.miniflux

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class MinifluxError(@SerialName("error_message") val errorMessage: String)
