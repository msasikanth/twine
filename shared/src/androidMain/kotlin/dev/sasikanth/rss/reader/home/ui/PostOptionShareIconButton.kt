package dev.sasikanth.rss.reader.home.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.sasikanth.rss.reader.resources.IconResources
import dev.sasikanth.rss.reader.resources.strings.LocalStrings

@Composable
internal actual fun PostOptionShareIconButton(postLink: String) {
  val context = LocalContext.current
  PostOptionIconButton(
    iconRes = IconResources.share,
    contentDescription = LocalStrings.current.share,
    onClick = {
      val sendIntent =
        Intent().apply {
          action = Intent.ACTION_SEND
          putExtra(Intent.EXTRA_TEXT, postLink)
          type = "text/plain"
        }
      val shareIntent = Intent.createChooser(sendIntent, null)
      context.startActivity(shareIntent)
    }
  )
}
