/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.opml

import co.touchlab.stately.concurrency.AtomicInt
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.filemanager.FileManager
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.utils.Constants.BACKUP_FILE_NAME
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class OpmlManager(
  dispatchersProvider: DispatchersProvider,
  private val fileManager: FileManager,
  private val feedsOpml: FeedsOpml,
  private val rssRepository: RssRepository,
) {

  private val job = SupervisorJob() + dispatchersProvider.io

  private val _result = MutableSharedFlow<OpmlResult>(replay = 1)
  val result: SharedFlow<OpmlResult> = _result

  companion object {
    private const val IMPORT_CHUNKS = 20
  }

  init {
    _result.tryEmit(OpmlResult.Idle)
  }

  suspend fun import() {
    try {
      withContext(job) {
        val opmlXmlContent = fileManager.read()

        if (!opmlXmlContent.isNullOrBlank()) {
          _result.emit(OpmlResult.InProgress.Importing(0))
          val opmlFeeds = feedsOpml.decode(opmlXmlContent)

          addOpmlFeeds(opmlFeeds)
            .onEach { progress -> _result.emit(OpmlResult.InProgress.Importing(progress)) }
            .onCompletion { _result.emit(OpmlResult.Idle) }
            .collect()
        } else {
          _result.emit(OpmlResult.Error.NoContentInOpmlFile)
        }
      }
    } catch (e: Exception) {
      if (e is CancellationException) {
        return
      }

      _result.emit(OpmlResult.Error.UnknownFailure(e))
    }
  }

  suspend fun export() {
    try {
      withContext(job) {
        _result.emit(OpmlResult.InProgress.Exporting(0))

        // TODO: Use pagination for fetching feeds?
        //  will be much more memory efficient if there are lot of feeds.
        //  Need to modify encode as well to support paginated input

        // TODO: Should we track real time progress as we loop through the feeds?
        //  It's a quick action, so not sure. Maybe once pagination support is added here
        //  I can do that

        val opmlString =
          rssRepository.allFeedsBlocking().run {
            _result.emit(OpmlResult.InProgress.Exporting(50))
            feedsOpml.encode(this)
          }
        fileManager.save(BACKUP_FILE_NAME, opmlString)

        _result.emit(OpmlResult.InProgress.Exporting(100))
        _result.emit(OpmlResult.Idle)
      }
    } catch (e: Exception) {
      if (e is CancellationException) {
        return
      }

      _result.emit(OpmlResult.Error.UnknownFailure(e))
    }
  }

  fun cancel() {
    job.cancelChildren()
    _result.tryEmit(OpmlResult.Idle)
  }

  private fun addOpmlFeeds(feedLinks: List<OpmlFeed>): Flow<Int> = channelFlow {
    val totalFeedCount = feedLinks.size
    val processedFeedsCount = AtomicInt(0)

    feedLinks.chunked(IMPORT_CHUNKS).forEach { feedsGroup ->
      feedsGroup
        .map { feed -> launch { rssRepository.addFeed(feedLink = feed.link, title = feed.title) } }
        .joinAll()

      val size = processedFeedsCount.addAndGet(feedsGroup.size)
      // We are converting the total feed count to float
      // so that we can get the precise progress like 0.1, 0.2..etc.,
      send(((size / totalFeedCount.toFloat()) * 100).roundToInt())
    }
  }
}

sealed interface OpmlResult {
  object Idle : OpmlResult

  sealed interface InProgress : OpmlResult {
    data class Importing(val progress: Int) : OpmlResult

    data class Exporting(val progress: Int) : OpmlResult
  }

  sealed interface Error : OpmlResult {
    object NoContentInOpmlFile : OpmlResult

    data class UnknownFailure(val error: Exception) : OpmlResult
  }
}
