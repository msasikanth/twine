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

import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import co.touchlab.kermit.Logger
import co.touchlab.stately.concurrency.AtomicInt
import dev.sasikanth.rss.reader.data.repository.FeedAddResult
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.Constants.BACKUP_FILE_NAME
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlin.math.roundToInt
import kotlin.time.measureTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ProducerScope
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
class OpmlManager(
  private val dispatchersProvider: DispatchersProvider,
  private val sourcesOpml: SourcesOpml,
  private val rssRepository: RssRepository,
) {

  private val job = SupervisorJob() + dispatchersProvider.main
  private val coroutineScope = CoroutineScope(job)

  private val _result = MutableSharedFlow<OpmlResult>(replay = 1)
  val result: SharedFlow<OpmlResult> = _result

  companion object {
    private const val IMPORT_CHUNKS = 20
  }

  init {
    _result.tryEmit(OpmlResult.Idle)
  }

  suspend fun import() {
    val duration = measureTime {
      try {
        coroutineScope.launch {
          val file =
            FileKit.pickFile(
              title = "Import OPML",
              type = PickerType.File(extensions = listOf("xml", "opml", "bin")),
              mode = PickerMode.Single,
              initialDirectory = "downloads"
            )

          withContext(dispatchersProvider.io) {
            val opmlXmlContent = file?.readBytes()?.decodeToString()

            Logger.i { opmlXmlContent.orEmpty() }

            if (!opmlXmlContent.isNullOrBlank()) {
              _result.emit(OpmlResult.InProgress.Importing(0))
              val opmlSources = sourcesOpml.decode(opmlXmlContent)

              addOpmlSources(opmlSources)
                .onEach { progress -> _result.emit(OpmlResult.InProgress.Importing(progress)) }
                .onCompletion { _result.emit(OpmlResult.Idle) }
                .collect()
            } else {
              _result.emit(OpmlResult.Error.NoContentInOpmlFile)
            }
          }
        }
      } catch (e: Exception) {
        if (e is CancellationException) {
          return
        }

        BugsnagKotlin.sendHandledException(e)
        _result.emit(OpmlResult.Error.UnknownFailure(e))
      }
    }

    Logger.i("OPMLImport") { "Took: ${duration.inWholeMinutes} minutes" }
  }

  suspend fun export() {
    try {
      coroutineScope.launch {
        _result.emit(OpmlResult.InProgress.Exporting(0))

        val opmlString =
          withContext(dispatchersProvider.io) {
            val feeds = rssRepository.allFeedsBlocking()
            val feedGroups = rssRepository.allFeedGroupsBlocking()
            val opmlSources = mutableListOf<OpmlSource>()

            val feedsById = feeds.associateBy { it.id }

            feedGroups.forEach { feedGroup ->
              val feedsInGroup =
                feedGroup.feedIds.mapNotNull { feedId ->
                  feedsById[feedId]?.let { feed ->
                    OpmlFeed(
                      title = feed.name,
                      link = feed.link,
                    )
                  }
                }

              val opmlFeedGroup = OpmlFeedGroup(title = feedGroup.name, feeds = feedsInGroup)
              opmlSources.add(opmlFeedGroup)
            }

            feeds.forEach { feed ->
              if (feedGroups.none { it.feedIds.contains(feed.id) }) {
                opmlSources.add(OpmlFeed(title = feed.name, link = feed.link))
              }
            }

            sourcesOpml.encode(opmlSources)
          }

        _result.emit(OpmlResult.InProgress.Importing(50))

        FileKit.saveFile(
          bytes = opmlString.encodeToByteArray(),
          baseName = BACKUP_FILE_NAME,
          extension = "xml",
        )

        _result.emit(OpmlResult.Idle)
      }
    } catch (e: Exception) {
      if (e is CancellationException) {
        return
      }

      BugsnagKotlin.sendHandledException(e)
      _result.emit(OpmlResult.Error.UnknownFailure(e))
    }
  }

  fun cancel() {
    job.cancelChildren()
    _result.tryEmit(OpmlResult.Idle)
  }

  private fun addOpmlSources(sources: List<OpmlSource>): Flow<Int> = channelFlow {
    val totalSourcesCount = sources.size
    val processedFeedsCount = AtomicInt(0)

    if (sources.size > IMPORT_CHUNKS) {
      sources.reversed().chunked(IMPORT_CHUNKS).forEach { sourcesInChunk ->
        sourcesInChunk.map { source -> launch { createSourceInDB(source) } }.joinAll()

        val size = processedFeedsCount.addAndGet(sourcesInChunk.size)
        sendProgress(size, totalSourcesCount)
      }
    } else {
      sources.reversed().forEachIndexed { index, source ->
        launch { createSourceInDB(source) }.join()

        sendProgress(index, totalSourcesCount)
      }
    }
  }

  private suspend fun createSourceInDB(source: OpmlSource) {
    when (source) {
      is OpmlFeed -> {
        addFeed(source)
      }
      is OpmlFeedGroup -> {
        val groupId = rssRepository.createGroup(source.title)
        val feedIds = source.feeds.mapNotNull { feed -> addFeed(feed) }

        rssRepository.addFeedIdsToGroups(groupIds = setOf(groupId), feedIds = feedIds)
      }
    }
  }

  private suspend fun addFeed(feed: OpmlFeed): String? {
    val result = rssRepository.addFeed(feedLink = feed.link, title = feed.title)
    if (result !is FeedAddResult.Success) {
      Logger.e("OPMLImport") { "Failed to import: ${feed.link}" }
      return null
    }

    return result.feedId
  }

  private suspend fun ProducerScope<Int>.sendProgress(progressIndex: Int, totalFeedCount: Int) {
    // We are converting the total feed count to float
    // so that we can get the precise progress like 0.1, 0.2..etc.,
    send(((progressIndex / totalFeedCount.toFloat()) * 100).roundToInt())
  }
}

sealed interface OpmlResult {
  data object Idle : OpmlResult

  sealed interface InProgress : OpmlResult {
    data class Importing(val progress: Int) : OpmlResult

    data class Exporting(val progress: Int) : OpmlResult
  }

  sealed interface Error : OpmlResult {
    data object NoContentInOpmlFile : OpmlResult

    data class UnknownFailure(val error: Exception) : OpmlResult
  }
}
