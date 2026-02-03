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

package dev.sasikanth.rss.reader.media

import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.*
import platform.CoreMedia.*
import platform.Foundation.*
import platform.MediaPlayer.*
import platform.UIKit.UIImage
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Inject
@AppScope
class IOSAudioPlayer(private val dispatchersProvider: DispatchersProvider) : AudioPlayer {

  private val _playbackState = MutableStateFlow(PlaybackState.Idle)
  override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

  private val player = AVPlayer()
  private val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
  private var progressJob: Job? = null
  private var playingUrl: String? = null
  private var sleepTimerJob: Job? = null
  private var sleepTimerRemainingMillis: Long? = null
  private var selectedSleepTimerOption: SleepTimerOption = SleepTimerOption.None

  private val fileManager = NSFileManager.defaultManager
  private val cacheDirectory =
    fileManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask).first() as NSURL

  private var currentSpeed: Float = 1.0f

  init {
    setupRemoteCommands()

    val audioSession = AVAudioSession.sharedInstance()
    audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
    audioSession.setActive(true, null)

    NSNotificationCenter.defaultCenter.addObserverForName(
      AVPlayerItemDidPlayToEndTimeNotification,
      null,
      null,
    ) {
      if (_playbackState.value.sleepTimerRemaining == -1L) {
        setSleepTimer(SleepTimerOption.None)
      }
      pause()
      seekTo(0)
    }
  }

  override fun play(url: String, title: String, artist: String, coverUrl: String?) {
    scope.launch {
      playingUrl = url

      val fileName = "${nameBasedUuidOf(url)}.mp3"
      val localUrl = cacheDirectory.URLByAppendingPathComponent(fileName)!!

      val isDownloaded =
        withContext(dispatchersProvider.io) { fileManager.fileExistsAtPath(localUrl.path!!) }

      if (isDownloaded) {
        playInternal(localUrl, title, artist, coverUrl)
      } else {
        downloadAndPlay(url, localUrl, title, artist, coverUrl)
      }
    }
  }

  private fun downloadAndPlay(
    remoteUrl: String,
    localUrl: NSURL,
    title: String,
    artist: String,
    coverUrl: String?,
  ) {
    val nsUrl = NSURL.URLWithString(remoteUrl) ?: return
    val task =
      NSURLSession.sharedSession.downloadTaskWithURL(nsUrl) { location, _, _ ->
        if (location != null) {
          fileManager.moveItemAtURL(location, localUrl, null)
          dispatch_async(dispatch_get_main_queue()) {
            playInternal(localUrl, title, artist, coverUrl)
          }
        }
      }
    task.resume()

    // While downloading, we can start playing from the remote URL if we want streaming + caching,
    // but AVPlayer can't easily switch from remote to local seamlessly or use a shared cache.
    // For simplicity, we play the remote URL first, and subsequent plays will use the local file.
    playInternal(nsUrl, title, artist, coverUrl)
  }

  private fun playInternal(url: NSURL, title: String, artist: String, coverUrl: String?) {
    val playerItem = AVPlayerItem(uRL = url)
    player.replaceCurrentItemWithPlayerItem(playerItem)
    player.playImmediatelyAtRate(currentSpeed)

    updateNowPlayingInfo(title, artist, coverUrl)
    startProgressUpdate()
  }

  override fun pause() {
    player.pause()
    stopProgressUpdate()
    updatePlaybackState()
  }

  override fun resume() {
    player.playImmediatelyAtRate(currentSpeed)
    startProgressUpdate()
    updatePlaybackState()
  }

  override fun seekTo(position: Long) {
    val cmTime = CMTimeMakeWithSeconds(position.toDouble() / 1000.0, 1000)
    player.seekToTime(cmTime)
    updatePlaybackState()
  }

  override fun setPlaybackSpeed(speed: Float) {
    currentSpeed = speed
    if (player.timeControlStatus == AVPlayerTimeControlStatusPlaying) {
      player.playImmediatelyAtRate(speed)
    }
    updatePlaybackState()
  }

  override fun setSleepTimer(option: SleepTimerOption) {
    sleepTimerJob?.cancel()
    sleepTimerRemainingMillis = null
    selectedSleepTimerOption = option

    when (option) {
      SleepTimerOption.None -> {
        // No-op
      }
      SleepTimerOption.EndOfTrack -> {
        sleepTimerRemainingMillis = -1L
      }
      is SleepTimerOption.Minutes -> {
        val millis = option.minutes * 60 * 1000L
        sleepTimerRemainingMillis = millis
        sleepTimerJob =
          scope.launch {
            while (sleepTimerRemainingMillis!! > 0) {
              delay(1000)
              sleepTimerRemainingMillis = sleepTimerRemainingMillis!! - 1000
              updatePlaybackState()
            }
            pause()
            setSleepTimer(SleepTimerOption.None)
          }
      }
    }
    updatePlaybackState()
  }

  private fun setupRemoteCommands() {
    val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
    commandCenter.playCommand.setEnabled(true)
    commandCenter.playCommand.addTargetWithHandler {
      resume()
      MPRemoteCommandHandlerStatusSuccess
    }

    commandCenter.pauseCommand.setEnabled(true)
    commandCenter.pauseCommand.addTargetWithHandler {
      pause()
      MPRemoteCommandHandlerStatusSuccess
    }

    commandCenter.changePlaybackPositionCommand.setEnabled(true)
    commandCenter.changePlaybackPositionCommand.addTargetWithHandler { event ->
      val position = (event as? MPChangePlaybackPositionCommandEvent)?.positionTime ?: 0.0
      seekTo((position * 1000).toLong())
      MPRemoteCommandHandlerStatusSuccess
    }
  }

  private fun updateNowPlayingInfo(title: String, artist: String, coverUrl: String?) {
    val nowPlayingInfo =
      (MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo ?: emptyMap<Any?, Any?>())
        .toMutableMap()
    nowPlayingInfo[MPMediaItemPropertyTitle] = title
    nowPlayingInfo[MPMediaItemPropertyArtist] = artist

    val durationSeconds =
      CMTimeGetSeconds(player.currentItem?.duration ?: CMTimeMakeWithSeconds(0.0, 1))
    if (!durationSeconds.isNaN() && durationSeconds > 0) {
      nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = durationSeconds
    }

    nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] =
      CMTimeGetSeconds(player.currentTime())
    nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] =
      if (player.timeControlStatus == AVPlayerTimeControlStatusPlaying) 1.0 else 0.0

    MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = nowPlayingInfo

    if (coverUrl != null) {
      loadArtwork(coverUrl)
    }
  }

  private fun loadArtwork(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    val task =
      NSURLSession.sharedSession.dataTaskWithURL(nsUrl) { data, _, _ ->
        if (data != null) {
          val image = UIImage.imageWithData(data)
          if (image != null) {
            val artwork = MPMediaItemArtwork(image.size) { _ -> image }
            dispatch_async(dispatch_get_main_queue()) {
              val nowPlayingInfo =
                (MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo ?: emptyMap<Any?, Any?>())
                  .toMutableMap()
              nowPlayingInfo[MPMediaItemPropertyArtwork] = artwork
              MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = nowPlayingInfo
            }
          }
        }
      }
    task.resume()
  }

  private fun updatePlaybackState() {
    val currentItem = player.currentItem
    val durationSeconds = if (currentItem != null) CMTimeGetSeconds(currentItem.duration) else 0.0
    val duration = if (durationSeconds.isNaN()) 0L else (durationSeconds * 1000).toLong()
    val currentPositionSeconds = CMTimeGetSeconds(player.currentTime())
    val currentPosition = (currentPositionSeconds * 1000).toLong()

    val isPlaying =
      player.timeControlStatus == AVPlayerTimeControlStatusPlaying ||
        player.timeControlStatus == AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
    val buffering =
      player.timeControlStatus == AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate

    _playbackState.update {
      it.copy(
        isPlaying = isPlaying,
        currentPosition = currentPosition,
        duration = duration,
        playingUrl = playingUrl,
        buffering = buffering,
        playbackSpeed = currentSpeed,
        sleepTimerRemaining = sleepTimerRemainingMillis,
        selectedSleepTimerOption = selectedSleepTimerOption,
      )
    }

    // Update system media player
    val nowPlayingInfo =
      (MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo ?: emptyMap<Any?, Any?>())
        .toMutableMap()
    nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentPositionSeconds
    if (!durationSeconds.isNaN() && durationSeconds > 0) {
      nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = durationSeconds
    }
    nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = if (isPlaying) 1.0 else 0.0
    MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = nowPlayingInfo
  }

  private fun startProgressUpdate() {
    progressJob?.cancel()
    progressJob =
      scope.launch {
        while (true) {
          updatePlaybackState()
          delay(1000)
        }
      }
  }

  private fun stopProgressUpdate() {
    progressJob?.cancel()
    progressJob = null
  }
}
