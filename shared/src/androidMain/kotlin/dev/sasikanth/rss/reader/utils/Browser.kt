package dev.sasikanth.rss.reader.utils

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import dev.sasikanth.rss.reader.ReaderApplication

actual fun openBrowser(url: String) {
  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
    flags = FLAG_ACTIVITY_NEW_TASK
  }
  ReaderApplication.context.startActivity(intent)
}
