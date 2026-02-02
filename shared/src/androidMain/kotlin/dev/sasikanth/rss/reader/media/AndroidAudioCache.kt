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

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dev.sasikanth.rss.reader.di.scopes.AppScope
import java.io.File
import me.tatarka.inject.annotations.Inject

@OptIn(UnstableApi::class)
@Inject
@AppScope
class AndroidAudioCache(private val context: Context) {

  private val cacheDirectory = File(context.cacheDir, "audio_cache")
  private val databaseProvider = StandaloneDatabaseProvider(context)
  private val evictor = LeastRecentlyUsedCacheEvictor(250 * 1024 * 1024) // 250 MB

  val cache: SimpleCache by lazy { SimpleCache(cacheDirectory, evictor, databaseProvider) }
}
