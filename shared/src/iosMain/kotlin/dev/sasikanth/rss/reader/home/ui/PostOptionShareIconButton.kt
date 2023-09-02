package dev.sasikanth.rss.reader.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.interop.LocalUIViewController
import dev.sasikanth.rss.reader.resources.IconResources
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import platform.UIKit.UIActivityViewController

@Composable
internal actual fun PostOptionShareIconButton(postLink: String) {
  val viewController = LocalUIViewController.current
  PostOptionIconButton(
    iconRes = IconResources.share,
    contentDescription = LocalStrings.current.share,
    onClick = {
      val items = listOf(postLink)
      val activityController = UIActivityViewController(items, null)
      viewController.presentViewController(activityController, true, null)
    }
  )
}
