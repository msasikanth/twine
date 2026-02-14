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

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

@OptIn(UnstableApi::class)
class MediaService : MediaSessionService() {

  private var mediaSession: MediaSession? = null

  override fun onCreate() {
    super.onCreate()
    val cacheDataSourceFactory =
      CacheDataSource.Factory()
        .setCache(AudioCacheProvider.cache)
        .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())

    val player =
      ExoPlayer.Builder(this)
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build(),
          true,
        )
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
        .build()

    mediaSession =
      MediaSession.Builder(this, player)
        .setSessionActivity(
          PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName)!!.apply {
              action = Intent.ACTION_VIEW
              data = Uri.parse("twine://reader/currently-playing")
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
          )
        )
        .build()
  }

  override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
    return mediaSession
  }

  override fun onDestroy() {
    mediaSession?.run {
      player.release()
      release()
      mediaSession = null
    }
    super.onDestroy()
  }

  override fun onTaskRemoved(rootIntent: Intent?) {
    stopSelf()
  }
}
