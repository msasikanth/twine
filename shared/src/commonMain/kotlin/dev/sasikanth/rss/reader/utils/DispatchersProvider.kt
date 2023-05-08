package dev.sasikanth.rss.reader.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

interface DispatchersProvider {
  val main: CoroutineDispatcher
  val io: CoroutineDispatcher
  val default: CoroutineDispatcher
}

class DefaultDispatchersProvider : DispatchersProvider {
  override val main = Dispatchers.Main
  override val io = Dispatchers.IO
  override val default = Dispatchers.Default
}
