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

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class AndroidTextToSpeechHandler(private val activity: ComponentActivity) :
  TextToSpeechHandler, TextToSpeech.OnInitListener {

  private var tts: TextToSpeech? = null
  private val _isPlaying = MutableStateFlow(false)
  override val isPlaying: StateFlow<Boolean> = _isPlaying

  init {
    tts = TextToSpeech(activity, this)
    tts?.setOnUtteranceProgressListener(
      object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
          _isPlaying.value = true
        }

        override fun onDone(utteranceId: String?) {
          _isPlaying.value = false
        }

        override fun onError(utteranceId: String?) {
          Logger.e { "TTS Error: $utteranceId" }
          _isPlaying.value = false
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
          super.onError(utteranceId, errorCode)
          Logger.e { "TTS Error: $errorCode" }
        }
      }
    )
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      tts?.language = Locale.US

      val voices = tts?.voices
      val betterVoice = voices?.firstOrNull { it.name == "en-us-x-tpc-network" }

      betterVoice?.let { tts?.voice = it }
    }
  }

  override fun speak(text: String) {
    if (text.isBlank()) return
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "twine_tts")
  }

  override fun stop() {
    tts?.stop()
    _isPlaying.value = false
  }
}
