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

package dev.sasikanth.rss.reader.about.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.about.AboutEvent
import dev.sasikanth.rss.reader.about.AboutPresenter
import dev.sasikanth.rss.reader.about.Person
import dev.sasikanth.rss.reader.about.Social
import dev.sasikanth.rss.reader.components.bottomsheet.fastForEach
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.GitHub
import dev.sasikanth.rss.reader.resources.icons.Threads
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.Twitter
import dev.sasikanth.rss.reader.resources.icons.Website
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import kotlinx.coroutines.launch

@Composable
internal fun AboutScreen(aboutPresenter: AboutPresenter, modifier: Modifier = Modifier) {
  val layoutDirection = LocalLayoutDirection.current
  val strings = LocalStrings.current
  val persons: List<Person> = remember {
    listOf(
      Person(
        name = Constants.ABOUT_SASI_NAME,
        role = strings.aboutRoleDeveloper,
        profilePicture = Constants.ABOUT_SASI_PIC,
        socials =
          listOf(
            Social(
              service = strings.aboutSocialThreads,
              link = Constants.ABOUT_SASI_THREADS,
              icon = TwineIcons.Threads
            ),
            Social(
              service = strings.aboutSocialTwitter,
              link = Constants.ABOUT_SASI_TWITTER,
              icon = TwineIcons.Twitter
            ),
            Social(
              service = strings.aboutSocialGitHub,
              link = Constants.ABOUT_SASI_GITHUB,
              icon = TwineIcons.GitHub
            ),
            Social(
              service = strings.aboutSocialWebsite,
              link = Constants.ABOUT_SASI_WEBSITE,
              icon = TwineIcons.Website
            ),
          )
      ),
      Person(
        name = Constants.ABOUT_ED_NAME,
        role = strings.aboutRoleDesigner,
        profilePicture = Constants.ABOUT_ED_PIC,
        socials =
          listOf(
            Social(
              service = strings.aboutSocialThreads,
              link = Constants.ABOUT_ED_THREADS,
              icon = TwineIcons.Threads
            ),
            Social(
              service = strings.aboutSocialTwitter,
              link = Constants.ABOUT_ED_TWITTER,
              icon = TwineIcons.Twitter
            ),
          )
      ),
    )
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = { Text(strings.about) },
          navigationIcon = {
            IconButton(onClick = { aboutPresenter.dispatch(AboutEvent.BackClicked) }) {
              Icon(TwineIcons.ArrowBack, contentDescription = null)
            }
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = AppTheme.colorScheme.surface,
              navigationIconContentColor = AppTheme.colorScheme.onSurface,
              titleContentColor = AppTheme.colorScheme.onSurface,
              actionIconContentColor = AppTheme.colorScheme.onSurface
            ),
        )

        HorizontalDivider(
          modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
          color = AppTheme.colorScheme.surfaceContainer
        )
      }
    },
    content = { padding ->
      Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
          contentPadding =
            PaddingValues(
              start = padding.calculateStartPadding(layoutDirection),
              top = padding.calculateTopPadding() + 8.dp,
              end = padding.calculateEndPadding(layoutDirection),
              bottom = padding.calculateBottomPadding() + 80.dp
            ),
        ) {
          items(persons) { person -> AboutListItem(person) }
        }
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified,
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutListItem(person: Person, modifier: Modifier = Modifier) {
  val coroutineScope = rememberCoroutineScope()
  Box(modifier = modifier) {
    Box(modifier = Modifier.padding(start = 8.dp, top = 32.dp, end = 8.dp, bottom = 24.dp)) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              person.role,
              style = MaterialTheme.typography.bodyMedium,
              color = AppTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
              person.name,
              style = MaterialTheme.typography.titleLarge,
              color = AppTheme.colorScheme.onSurface
            )
          }
          Spacer(Modifier.width(16.dp))
          AsyncImage(
            url = person.profilePicture,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.requiredSize(56.dp).clip(CircleShape),
          )
        }

        Spacer(Modifier.height(24.dp))

        FlowRow {
          val linkHandler = LocalLinkHandler.current
          person.socials.fastForEach { social ->
            SocialButton(
              social = social,
              onClick = { coroutineScope.launch { linkHandler.openLink(social.link) } }
            )
          }
        }
      }
    }

    HorizontalDivider(
      modifier = Modifier.align(Alignment.BottomStart),
      color = AppTheme.colorScheme.surfaceContainer
    )
  }
}

@Composable
private fun SocialButton(
  social: Social,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Column(
    modifier =
      modifier
        .clip(MaterialTheme.shapes.large)
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      imageVector = social.icon,
      contentDescription = null,
      tint = AppTheme.colorScheme.tintedForeground
    )
    Spacer(Modifier.height(4.dp))
    Text(
      social.service,
      style = MaterialTheme.typography.labelMedium,
      color = AppTheme.colorScheme.tintedForeground
    )
  }
}
