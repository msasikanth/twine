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

import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Inject
import platform.AVFoundation.AVSpeechBoundary
import platform.AVFoundation.AVSpeechSynthesisVoice
import platform.AVFoundation.AVSpeechSynthesizer
import platform.AVFoundation.AVSpeechSynthesizerDelegateProtocol
import platform.AVFoundation.AVSpeechUtterance
import platform.darwin.NSObject

@Inject
@ActivityScope
class IOSTextToSpeechHandler : TextToSpeechHandler {

  private val synthesizer = AVSpeechSynthesizer()
  private val _isPlaying = MutableStateFlow(false)
  override val isPlaying: StateFlow<Boolean> = _isPlaying

  private val delegate =
    object : NSObject(), AVSpeechSynthesizerDelegateProtocol {
      override fun speechSynthesizer(
        synthesizer: AVSpeechSynthesizer,
        didStartSpeechUtterance: AVSpeechUtterance
      ) {
        _isPlaying.value = true
      }

      override fun speechSynthesizer(
        synthesizer: AVSpeechSynthesizer,
        didFinishSpeechUtterance: AVSpeechUtterance
      ) {
        _isPlaying.value = false
      }

      override fun speechSynthesizer(
        synthesizer: AVSpeechSynthesizer,
        didCancelSpeechUtterance: AVSpeechUtterance
      ) {
        _isPlaying.value = false
      }
    }

  init {
    synthesizer.delegate = delegate
  }

  override fun speak(text: String) {
    if (text.isBlank()) return
    val utterance = AVSpeechUtterance(string = text)
    // Select a natural voice if available
    utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage(null)
    synthesizer.speakUtterance(utterance)
  }

  override fun stop() {
    synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    _isPlaying.value = false
  }
}
