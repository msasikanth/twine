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
import kotlinx.cinterop.readValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.playImmediatelyAtRate
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.CoreMedia.kCMTimeZero
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionDownloadTask
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataTaskWithURL
import platform.Foundation.downloadTaskWithURL
import platform.MediaPlayer.MPChangePlaybackPositionCommandEvent
import platform.MediaPlayer.MPMediaItemArtwork
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyArtwork
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import platform.UIKit.UIImage
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_attr_make_with_qos_class
import platform.darwin.dispatch_queue_create
import platform.posix.QOS_CLASS_USER_INITIATED

@OptIn(ExperimentalForeignApi::class)
@AppScope
class IOSAudioPlayer
internal constructor(
  private val dispatchersProvider: DispatchersProvider,
  private val platformPlayer: PlatformAudioPlayer,
) : AudioPlayer {

  @Inject
  constructor(
    dispatchersProvider: DispatchersProvider
  ) : this(dispatchersProvider, AvPlatformAudioPlayer())

  private val _playbackState = MutableStateFlow(PlaybackState.Idle)
  override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

  private val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
  private var progressJob: Job? = null
  private var playingUrl: String? = null
  private var playingPostId: String? = null
  private var playingTitle: String? = null
  private var playingArtist: String? = null
  private var playingCoverUrl: String? = null
  private var playJob: Job? = null
  private var downloadTask: NSURLSessionDownloadTask? = null
  private var pendingLocalCacheUrl: NSURL? = null
  private var currentSeekId: Long = 0L
  private var activePositionSeekId: Long = 0L
  private var pendingSeekPositionMillis: Long? = null
  private var pendingAutoPlayAfterSeek: Boolean = false
  private var sleepTimerJob: Job? = null
  private var sleepTimerRemainingMillis: Long? = null
  private var selectedSleepTimerOption: SleepTimerOption = SleepTimerOption.None

  private val fileManager = NSFileManager.defaultManager
  private val cacheDirectory =
    fileManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask).first() as NSURL

  private var currentSpeed: Float = 1.0f

  init {
    setupRemoteCommands()

    platformPlayer.observePlaybackEnd { finishedItemMatchesCurrent ->
      scope.launch {
        if (!finishedItemMatchesCurrent()) return@launch

        if (_playbackState.value.sleepTimerRemaining == -1L) {
          setSleepTimer(SleepTimerOption.None)
        }
        stop()
      }
    }
  }

  override fun play(
    url: String,
    title: String,
    artist: String,
    coverUrl: String?,
    postId: String?,
    initialPosition: Long,
  ) {
    playJob?.cancel()
    downloadTask?.cancel()
    downloadTask = null
    pendingLocalCacheUrl = null
    pendingSeekPositionMillis = null
    pendingAutoPlayAfterSeek = false
    activePositionSeekId = ++currentSeekId

    playJob =
      scope.launch {
        playingUrl = url
        playingPostId = postId
        playingTitle = title
        playingArtist = artist
        playingCoverUrl = coverUrl

        val fileName = "${nameBasedUuidOf(url)}.mp3"
        val localUrl = cacheDirectory.URLByAppendingPathComponent(fileName)!!

        val isDownloaded =
          withContext(dispatchersProvider.io) { fileManager.fileExistsAtPath(localUrl.path!!) }

        if (!isActive) return@launch

        if (isDownloaded) {
          playInternal(localUrl, title, artist, coverUrl, initialPosition)
        } else {
          downloadAndPlay(url, localUrl, title, artist, coverUrl, initialPosition)
        }
      }
  }

  private fun downloadAndPlay(
    remoteUrl: String,
    localUrl: NSURL,
    title: String,
    artist: String,
    coverUrl: String?,
    initialPosition: Long,
  ) {
    val nsUrl = NSURL.URLWithString(remoteUrl) ?: return
    val task =
      NSURLSession.sharedSession.downloadTaskWithURL(nsUrl) { location, response, error ->
        val statusValid =
          (response as? NSHTTPURLResponse)?.statusCode?.let { it in 200..299 } ?: true
        if (error != null || !statusValid || location == null) return@downloadTaskWithURL

        val localPath = localUrl.path
        if (localPath != null && fileManager.fileExistsAtPath(localPath)) {
          fileManager.removeItemAtURL(localUrl, null)
        }
        val moved = fileManager.moveItemAtURL(location, localUrl, null)
        if (!moved) return@downloadTaskWithURL

        scope.launch { onLocalCacheReady(remoteUrl, localUrl) }
      }
    task.priority = 0.0f
    downloadTask = task

    scope.launch {
      delay(3000)
      if (playingUrl == remoteUrl) {
        task.resume()
      }
    }

    playInternal(nsUrl, title, artist, coverUrl, initialPosition)
  }

  internal fun onLocalCacheReady(remoteUrl: String, localUrl: NSURL) {
    if (playingUrl != remoteUrl) return
    downloadTask = null

    when {
      platformPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying -> {
        pendingLocalCacheUrl = localUrl
      }
      !_playbackState.value.isPlaying -> {
        swapToLocalCacheItem(
          localUrl = localUrl,
          positionMillis = currentPositionMillisOrZero(),
          shouldPlay = false,
        )
      }
      else -> {
        swapToLocalCacheItem(
          localUrl = localUrl,
          positionMillis = currentPositionMillisOrZero(),
          shouldPlay = true,
        )
      }
    }
  }

  private fun playInternal(
    url: NSURL,
    title: String,
    artist: String,
    coverUrl: String?,
    initialPosition: Long,
  ) {
    platformPlayer.activatePlaybackSession()
    platformPlayer.replaceCurrentItemWithUrl(url)

    if (initialPosition > 0) {
      val targetPosition = initialPosition.coerceAtLeast(0L)
      val seekId = ++currentSeekId
      activePositionSeekId = seekId
      pendingSeekPositionMillis = targetPosition
      platformPlayer.seekTo(positionMillis = targetPosition, zeroTolerance = false) {
        scope.launch {
          if (seekId == activePositionSeekId) {
            pendingSeekPositionMillis = null
            if (playingUrl != null) {
              updatePlaybackState(syncNowPlaying = true)
            }
          }
        }
      }
    }

    platformPlayer.playImmediatelyAtRate(currentSpeed)

    updateNowPlayingInfo(title, artist, coverUrl, initialPosition)
    startProgressUpdate()
  }

  private fun swapToLocalCacheItem(localUrl: NSURL, positionMillis: Long, shouldPlay: Boolean) {
    pendingLocalCacheUrl = null
    val targetPosition = positionMillis.coerceAtLeast(0L)
    val seekId = ++currentSeekId
    activePositionSeekId = seekId

    if (targetPosition > 0L || !shouldPlay) {
      platformPlayer.pause()
    }
    if (!shouldPlay) {
      stopProgressUpdate()
    }

    platformPlayer.replaceCurrentItemWithUrl(localUrl)

    if (shouldPlay) {
      platformPlayer.activatePlaybackSession()
    }

    if (targetPosition > 0L) {
      pendingSeekPositionMillis = targetPosition
      pendingAutoPlayAfterSeek = shouldPlay
      platformPlayer.seekTo(positionMillis = targetPosition, zeroTolerance = true) {
        scope.launch {
          if (seekId == activePositionSeekId) {
            val autoPlay = pendingAutoPlayAfterSeek && seekId == currentSeekId
            pendingSeekPositionMillis = null
            pendingAutoPlayAfterSeek = false
            if (playingUrl != null) {
              if (autoPlay) {
                platformPlayer.playImmediatelyAtRate(currentSpeed)
                startProgressUpdate()
              }
              updatePlaybackState(syncNowPlaying = true)
            }
          }
        }
      }
    } else {
      pendingSeekPositionMillis = null
      pendingAutoPlayAfterSeek = false
      if (shouldPlay) {
        platformPlayer.playImmediatelyAtRate(currentSpeed)
        startProgressUpdate()
      }
    }

    updatePlaybackState(syncNowPlaying = true)
  }

  override fun pause() {
    playJob?.cancel()
    val positionBeforePause = currentPositionMillisOrZero()
    ++currentSeekId
    pendingAutoPlayAfterSeek = false
    platformPlayer.pause()
    stopProgressUpdate()

    val cachedUrl = pendingLocalCacheUrl
    if (cachedUrl != null) {
      swapToLocalCacheItem(
        localUrl = cachedUrl,
        positionMillis = positionBeforePause,
        shouldPlay = false,
      )
    } else {
      updatePlaybackState(syncNowPlaying = true)
    }
  }

  override fun stop() {
    playJob?.cancel()
    downloadTask?.cancel()
    downloadTask = null
    pendingLocalCacheUrl = null
    pendingSeekPositionMillis = null
    pendingAutoPlayAfterSeek = false
    activePositionSeekId = ++currentSeekId
    platformPlayer.pause()
    platformPlayer.replaceCurrentItemWithUrl(null)
    stopProgressUpdate()
    sleepTimerJob?.cancel()
    sleepTimerRemainingMillis = null
    selectedSleepTimerOption = SleepTimerOption.None
    playingUrl = null
    playingPostId = null
    playingTitle = null
    playingArtist = null
    playingCoverUrl = null
    platformPlayer.setNowPlayingInfo(null)
    _playbackState.value = PlaybackState.Idle
  }

  override fun resume() {
    val cachedUrl = pendingLocalCacheUrl
    if (cachedUrl != null) {
      swapToLocalCacheItem(
        localUrl = cachedUrl,
        positionMillis = currentPositionMillisOrZero(),
        shouldPlay = true,
      )
      return
    }

    platformPlayer.activatePlaybackSession()
    platformPlayer.playImmediatelyAtRate(currentSpeed)
    startProgressUpdate()
    updatePlaybackState(syncNowPlaying = true)
  }

  override fun seekTo(position: Long) {
    val targetPosition = position.coerceAtLeast(0L)
    val cachedUrl = pendingLocalCacheUrl
    if (cachedUrl != null) {
      val wasPlaying = _playbackState.value.isPlaying
      swapToLocalCacheItem(
        localUrl = cachedUrl,
        positionMillis = targetPosition,
        shouldPlay = wasPlaying,
      )
      return
    }

    if (!platformPlayer.hasCurrentItem) {
      _playbackState.update { it.copy(currentPosition = targetPosition) }
      return
    }

    val seekId = ++currentSeekId
    activePositionSeekId = seekId
    val zeroTolerance = pendingAutoPlayAfterSeek
    pendingSeekPositionMillis = targetPosition
    _playbackState.update { it.copy(currentPosition = targetPosition) }

    platformPlayer.seekTo(positionMillis = targetPosition, zeroTolerance = zeroTolerance) {
      scope.launch {
        if (seekId == activePositionSeekId) {
          val autoPlay = pendingAutoPlayAfterSeek && seekId == currentSeekId
          pendingSeekPositionMillis = null
          pendingAutoPlayAfterSeek = false
          if (playingUrl != null) {
            if (autoPlay) {
              platformPlayer.playImmediatelyAtRate(currentSpeed)
              startProgressUpdate()
            }
            updatePlaybackState(syncNowPlaying = true)
          }
        }
      }
    }
  }

  override fun setPlaybackSpeed(speed: Float) {
    currentSpeed = speed
    if (platformPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying) {
      platformPlayer.playImmediatelyAtRate(speed)
    }
    updatePlaybackState(syncNowPlaying = true)
  }

  override fun setSleepTimer(option: SleepTimerOption) {
    sleepTimerJob?.cancel()
    sleepTimerRemainingMillis = null
    selectedSleepTimerOption = option

    when (option) {
      SleepTimerOption.None -> Unit
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
              updatePlaybackState(syncNowPlaying = false)
            }
            pause()
            setSleepTimer(SleepTimerOption.None)
          }
      }
    }
    updatePlaybackState(syncNowPlaying = false)
  }

  private fun setupRemoteCommands() {
    platformPlayer.setupRemoteCommands(
      onPlay = { resume() },
      onPause = { pause() },
      onSeekToMillis = { positionMillis -> seekTo(positionMillis) },
    )
  }

  private fun updateNowPlayingInfo(
    title: String,
    artist: String,
    coverUrl: String?,
    initialPositionMillis: Long = 0L,
  ) {
    val nowPlayingInfo = mutableMapOf<Any?, Any?>()
    nowPlayingInfo[MPMediaItemPropertyTitle] = title
    nowPlayingInfo[MPMediaItemPropertyArtist] = artist

    val durationSeconds = platformPlayer.currentItemDurationSeconds
    if (!durationSeconds.isNaN() && durationSeconds > 0) {
      nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = durationSeconds
    }

    val currentPositionSeconds =
      if (initialPositionMillis > 0L) {
        initialPositionMillis.toDouble() / 1000.0
      } else {
        val raw = platformPlayer.currentTimeSeconds
        if (raw.isNaN() || raw < 0.0) 0.0 else raw
      }
    nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentPositionSeconds
    nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] =
      if (platformPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying) {
        currentSpeed.toDouble()
      } else {
        0.0
      }

    platformPlayer.setNowPlayingInfo(nowPlayingInfo)

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
              if (playingCoverUrl != url) return@dispatch_async
              val nowPlayingInfo =
                (platformPlayer.getNowPlayingInfo() ?: emptyMap<Any?, Any?>()).toMutableMap()
              nowPlayingInfo[MPMediaItemPropertyArtwork] = artwork
              platformPlayer.setNowPlayingInfo(nowPlayingInfo)
            }
          }
        }
      }
    task.resume()
  }

  private fun currentPositionMillisOrZero(): Long {
    pendingSeekPositionMillis?.let {
      return it
    }
    val rawSeconds = platformPlayer.currentTimeSeconds
    return if (rawSeconds.isNaN() || rawSeconds < 0.0) {
      _playbackState.value.currentPosition.coerceAtLeast(0L)
    } else {
      (rawSeconds * 1000).toLong()
    }
  }

  private fun updatePlaybackState(syncNowPlaying: Boolean = false) {
    val cachedUrl = pendingLocalCacheUrl
    if (
      cachedUrl != null &&
        pendingSeekPositionMillis == null &&
        (platformPlayer.timeControlStatus ==
          AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate ||
          platformPlayer.isCurrentItemFailed)
    ) {
      swapToLocalCacheItem(
        localUrl = cachedUrl,
        positionMillis = _playbackState.value.currentPosition,
        shouldPlay = true,
      )
      return
    }

    val previousState = _playbackState.value
    val durationSeconds =
      if (platformPlayer.hasCurrentItem) platformPlayer.currentItemDurationSeconds else 0.0
    val duration =
      if (durationSeconds.isNaN() || durationSeconds < 0) 0L else (durationSeconds * 1000).toLong()

    val pendingPosition = pendingSeekPositionMillis
    val currentPosition: Long
    val currentPositionSeconds: Double
    if (pendingPosition != null) {
      currentPosition = pendingPosition
      currentPositionSeconds = pendingPosition.toDouble() / 1000.0
    } else {
      val rawPositionSeconds = platformPlayer.currentTimeSeconds
      currentPositionSeconds =
        if (rawPositionSeconds.isNaN() || rawPositionSeconds < 0) 0.0 else rawPositionSeconds
      currentPosition = (currentPositionSeconds * 1000).toLong()
    }

    val isActivelyPlaying = platformPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying
    val buffering =
      platformPlayer.timeControlStatus == AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
    val isPlaying = isActivelyPlaying || buffering || pendingAutoPlayAfterSeek

    _playbackState.update {
      it.copy(
        isPlaying = isPlaying,
        currentPosition = currentPosition,
        duration = duration,
        playingUrl = playingUrl,
        playingPostId = playingPostId,
        title = playingTitle,
        artist = playingArtist,
        coverUrl = playingCoverUrl,
        buffering = buffering,
        playbackSpeed = currentSpeed,
        sleepTimerRemaining = sleepTimerRemainingMillis,
        selectedSleepTimerOption = selectedSleepTimerOption,
      )
    }

    if (
      syncNowPlaying ||
        previousState.duration != duration ||
        previousState.isPlaying != isPlaying ||
        previousState.buffering != buffering
    ) {
      syncNowPlayingPlaybackState(
        currentPositionSeconds = currentPositionSeconds,
        durationSeconds = durationSeconds,
        isActivelyPlaying = isActivelyPlaying,
      )
    }
  }

  private fun syncNowPlayingPlaybackState(
    currentPositionSeconds: Double,
    durationSeconds: Double,
    isActivelyPlaying: Boolean,
  ) {
    val nowPlayingInfo =
      (platformPlayer.getNowPlayingInfo() ?: emptyMap<Any?, Any?>()).toMutableMap()
    nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentPositionSeconds
    if (!durationSeconds.isNaN() && durationSeconds > 0) {
      nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = durationSeconds
    }
    nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] =
      if (isActivelyPlaying) currentSpeed.toDouble() else 0.0
    platformPlayer.setNowPlayingInfo(nowPlayingInfo)
  }

  private fun startProgressUpdate() {
    progressJob?.cancel()
    progressJob =
      scope.launch {
        while (true) {
          updatePlaybackState(syncNowPlaying = false)
          delay(1000)
        }
      }
  }

  private fun stopProgressUpdate() {
    progressJob?.cancel()
    progressJob = null
  }
}

@OptIn(ExperimentalForeignApi::class)
internal interface PlatformAudioPlayer {
  val hasCurrentItem: Boolean
  val isCurrentItemFailed: Boolean
  val timeControlStatus: Long
  val currentItemDurationSeconds: Double
  val currentTimeSeconds: Double

  fun activatePlaybackSession()

  fun replaceCurrentItemWithUrl(url: NSURL?)

  fun pause()

  fun playImmediatelyAtRate(rate: Float)

  fun seekTo(positionMillis: Long, zeroTolerance: Boolean, onComplete: () -> Unit)

  fun observePlaybackEnd(onNotification: (finishedItemMatchesCurrent: () -> Boolean) -> Unit)

  fun setupRemoteCommands(onPlay: () -> Unit, onPause: () -> Unit, onSeekToMillis: (Long) -> Unit)

  fun getNowPlayingInfo(): Map<Any?, Any?>?

  fun setNowPlayingInfo(info: Map<Any?, Any?>?)
}

@OptIn(ExperimentalForeignApi::class)
private class AvPlatformAudioPlayer : PlatformAudioPlayer {
  private val player = AVPlayer()
  private val audioSessionQueue =
    dispatch_queue_create(
      label = "dev.sasikanth.rss.reader.audio_session",
      attr = dispatch_queue_attr_make_with_qos_class(null, QOS_CLASS_USER_INITIATED, 0),
    )

  override val hasCurrentItem: Boolean
    get() = player.currentItem != null

  override val isCurrentItemFailed: Boolean
    get() = player.currentItem?.status == AVPlayerItemStatusFailed

  override val timeControlStatus: Long
    get() = player.timeControlStatus

  override val currentItemDurationSeconds: Double
    get() = CMTimeGetSeconds(player.currentItem?.duration ?: CMTimeMakeWithSeconds(0.0, 1))

  override val currentTimeSeconds: Double
    get() = CMTimeGetSeconds(player.currentTime())

  override fun activatePlaybackSession() {
    dispatch_async(audioSessionQueue) {
      val audioSession = AVAudioSession.sharedInstance()
      if (audioSession.category != AVAudioSessionCategoryPlayback) {
        audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
      }
      audioSession.setActive(true, null)
    }
  }

  override fun replaceCurrentItemWithUrl(url: NSURL?) {
    player.replaceCurrentItemWithPlayerItem(url?.let { AVPlayerItem(uRL = it) })
  }

  override fun pause() {
    player.pause()
  }

  override fun playImmediatelyAtRate(rate: Float) {
    player.playImmediatelyAtRate(rate)
  }

  override fun seekTo(positionMillis: Long, zeroTolerance: Boolean, onComplete: () -> Unit) {
    val cmTime = CMTimeMakeWithSeconds(positionMillis.toDouble() / 1000.0, 1000)
    if (zeroTolerance) {
      player.seekToTime(
        time = cmTime,
        toleranceBefore = kCMTimeZero.readValue(),
        toleranceAfter = kCMTimeZero.readValue(),
      ) { _ ->
        onComplete()
      }
    } else {
      player.seekToTime(cmTime) { _ -> onComplete() }
    }
  }

  override fun observePlaybackEnd(
    onNotification: (finishedItemMatchesCurrent: () -> Boolean) -> Unit
  ) {
    NSNotificationCenter.defaultCenter.addObserverForName(
      AVPlayerItemDidPlayToEndTimeNotification,
      null,
      null,
    ) { notification ->
      onNotification {
        val currentItem = player.currentItem ?: return@onNotification false
        val finishedItem = notification?.`object` as? AVPlayerItem
        finishedItem == null || finishedItem == currentItem
      }
    }
  }

  override fun setupRemoteCommands(
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeekToMillis: (Long) -> Unit,
  ) {
    val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
    commandCenter.playCommand.setEnabled(true)
    commandCenter.playCommand.addTargetWithHandler {
      onPlay()
      MPRemoteCommandHandlerStatusSuccess
    }

    commandCenter.pauseCommand.setEnabled(true)
    commandCenter.pauseCommand.addTargetWithHandler {
      onPause()
      MPRemoteCommandHandlerStatusSuccess
    }

    commandCenter.changePlaybackPositionCommand.setEnabled(true)
    commandCenter.changePlaybackPositionCommand.addTargetWithHandler { event ->
      val position = (event as? MPChangePlaybackPositionCommandEvent)?.positionTime ?: 0.0
      onSeekToMillis((position * 1000).toLong())
      MPRemoteCommandHandlerStatusSuccess
    }
  }

  override fun getNowPlayingInfo(): Map<Any?, Any?>? =
    MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo

  override fun setNowPlayingInfo(info: Map<Any?, Any?>?) {
    MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = info
  }
}
