package dev.sasikanth.rss.reader

import android.app.Application
import dev.sasikanth.rss.reader.utils.initialiseLogging

class ReaderApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    initialiseLogging()
  }
}
