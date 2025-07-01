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

package dev.sasikanth.rss.reader.utils

object ExternalUriHandler {
  private var cached: String? = null

  var listener: ((uri: String) -> Unit)? = null
    set(value) {
      field = value
      if (value != null) {
        cached?.let { value.invoke(it) }
        cached = null
      }
    }

  fun onNewUri(uri: String) {
    cached = uri
    listener?.let {
      it.invoke(uri)
      cached = null
    }
  }
}
