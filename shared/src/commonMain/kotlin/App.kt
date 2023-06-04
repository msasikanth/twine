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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.sasikanth.rss.reader.components.DynamicContentTheme
import dev.sasikanth.rss.reader.components.rememberDynamicColorState
import dev.sasikanth.rss.reader.home.HomeViewModelFactory
import dev.sasikanth.rss.reader.home.ui.HomeScreen
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun App(homeViewModelFactory: HomeViewModelFactory, openLink: (String) -> Unit) {
  val dynamicColorState = rememberDynamicColorState()
  var imageUrl by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(imageUrl) {
    if (imageUrl != null) {
      dynamicColorState.updateColorsFromImageUrl(imageUrl!!)
    } else {
      dynamicColorState.reset()
    }
  }

  DynamicContentTheme(dynamicColorState) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = AppTheme.colorScheme.surfaceContainerLowest
    ) {
      HomeScreen(
        homeViewModelFactory = homeViewModelFactory,
        onFeaturedItemChange = { imageUrl = it },
        openLink = openLink
      )
    }
  }
}
