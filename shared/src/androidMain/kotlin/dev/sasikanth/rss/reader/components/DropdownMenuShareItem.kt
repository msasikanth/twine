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
package dev.sasikanth.rss.reader.components

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Share
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.compose.stringResource
import dev.sasikanth.rss.reader.CommonRes

@Composable
actual fun DropdownMenuShareItem(
  contentToShare: String,
  modifier: Modifier,
  onShareMenuOpened: () -> Unit
) {
  val context = LocalContext.current
  val sendIntent =
    Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, contentToShare)
      type = "text/plain"
    }
  val shareIntent = Intent.createChooser(sendIntent, null)

  DropdownMenuItem(
    modifier = modifier,
    text = { Text(text = stringResource(CommonRes.strings.share)) },
    leadingIcon = { Icon(Icons.TwoTone.Share, null) },
    onClick = {
      context.startActivity(shareIntent)
      onShareMenuOpened()
    }
  )
}
