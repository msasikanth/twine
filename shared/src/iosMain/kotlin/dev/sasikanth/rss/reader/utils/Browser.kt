package dev.sasikanth.rss.reader.utils

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openBrowser(url: String) {
  UIApplication.sharedApplication.openURL(url = NSURL(string = url))
}
