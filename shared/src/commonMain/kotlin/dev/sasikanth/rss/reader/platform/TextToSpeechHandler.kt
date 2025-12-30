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

package dev.sasikanth.rss.reader.platform

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.StateFlow

interface TextToSpeechHandler {
  val isPlaying: StateFlow<Boolean>

  fun speak(text: String)

  fun stop()
}

val LocalTextToSpeechHandler =
  staticCompositionLocalOf<TextToSpeechHandler> {
    error("CompositionLocal LocalTextToSpeechHandler not present")
  }
