package dev.sasikanth.rss.reader

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import dev.sasikanth.rss.reader.utils.initialiseLogging

class ReaderApplication : Application() {

  companion object {
    @SuppressLint("StaticFieldLeak")
    internal lateinit var context: Context
  }

  override fun onCreate() {
    super.onCreate()
    context = this
    initialiseLogging()
  }
}
