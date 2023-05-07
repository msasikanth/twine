package dev.sasikanth.rss.reader.utils

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun initialiseLogging() {
  Napier.base(DebugAntilog())
}
