/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.sasikanth.rss.reader.discovery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.Button
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.TextField
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.DiscoveryFeed
import dev.sasikanth.rss.reader.core.model.DiscoveryGroup
import dev.sasikanth.rss.reader.discovery.DiscoveryEvent
import dev.sasikanth.rss.reader.discovery.DiscoveryViewModel
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.Check
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonChange
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.discoveryAddFeed
import twine.shared.generated.resources.discoveryAdded
import twine.shared.generated.resources.discoverySearchHint
import twine.shared.generated.resources.discoveryTitle

@Composable
fun DiscoveryScreen(
  viewModel: DiscoveryViewModel,
  goBack: () -> Unit,
  showDoneButton: Boolean = false,
  onDone: () -> Unit = goBack,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsState()
  val focusRequester = remember { FocusRequester() }
  val appBarBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

  Scaffold(
    modifier = modifier,
    topBar = {
      Column(modifier = Modifier.background(AppTheme.colorScheme.surface)) {
        CenterAlignedTopAppBar(
          navigationIcon = {
            if (!showDoneButton) {
              CircularIconButton(
                modifier = Modifier.padding(start = 12.dp),
                icon = TwineIcons.ArrowBack,
                label = stringResource(Res.string.buttonGoBack),
                onClick = goBack,
              )
            }
          },
          title = {
            Text(
              text = stringResource(Res.string.discoveryTitle),
              style = MaterialTheme.typography.titleMedium,
              color = AppTheme.colorScheme.onSurface,
            )
          },
          actions = {
            if (showDoneButton) {
              CircularIconButton(
                modifier = Modifier.padding(end = 12.dp),
                icon = TwineIcons.Check,
                label = stringResource(Res.string.buttonChange),
                onClick = onDone,
              )
            }
          },
          contentPadding = PaddingValues(top = 8.dp, bottom = 4.dp),
          scrollBehavior = appBarBehavior,
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = AppTheme.colorScheme.surface,
              navigationIconContentColor = AppTheme.colorScheme.onSurface,
              titleContentColor = AppTheme.colorScheme.onSurface,
              actionIconContentColor = AppTheme.colorScheme.onSurface,
              scrolledContainerColor = AppTheme.colorScheme.surface,
            ),
        )

        TextField(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp)
              .padding(top = 4.dp, bottom = 12.dp)
              .focusRequester(focusRequester),
          value = state.searchQuery,
          onValueChange = { viewModel.dispatch(DiscoveryEvent.SearchQueryChanged(it)) },
          hint = stringResource(Res.string.discoverySearchHint),
          trailingIcon = {
            if (state.searchQuery.text.isNotBlank()) {
              androidx.compose.material3.IconButton(
                onClick = {
                  viewModel.dispatch(DiscoveryEvent.SearchQueryChanged(TextFieldValue()))
                }
              ) {
                Icon(
                  imageVector = TwineIcons.Close,
                  contentDescription = null,
                  tint = AppTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          },
        )
      }
    },
    content = { paddingValues ->
      if (state.isLoading) {
        Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator(color = AppTheme.colorScheme.primary)
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize().nestedScroll(appBarBehavior.nestedScrollConnection),
          contentPadding = paddingValues,
        ) {
          items(state.filteredGroups) { group ->
            DiscoveryGroupItem(
              group = group,
              addedFeedLinks = state.addedFeedLinks,
              inProgressFeedLinks = state.inProgressFeedLinks,
              onAddFeed = { link -> viewModel.dispatch(DiscoveryEvent.AddFeedClicked(link)) },
            )
          }
        }
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
  )
}

@Composable
private fun DiscoveryGroupItem(
  group: DiscoveryGroup,
  addedFeedLinks: Set<String>,
  inProgressFeedLinks: Set<String>,
  onAddFeed: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth().padding(vertical = 16.dp)) {
    Row(
      modifier = Modifier.padding(horizontal = 24.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = group.name,
        style = MaterialTheme.typography.titleMedium,
        color = AppTheme.colorScheme.onSurface,
      )

      Text(
        text = group.feeds.size.toString(),
        style = MaterialTheme.typography.titleMedium,
        color = AppTheme.colorScheme.primary,
      )
    }

    Spacer(Modifier.requiredHeight(12.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 24.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      items(group.feeds) { feed ->
        DiscoveryFeedItem(
          feed = feed,
          isAdded =
            addedFeedLinks.contains(feed.link) ||
              addedFeedLinks.contains(feed.link.removeSuffix("/")),
          isLoading = inProgressFeedLinks.contains(feed.link),
          onAddFeed = { onAddFeed(feed.link) },
        )
      }
    }
  }
}

@Composable
private fun DiscoveryFeedItem(
  feed: DiscoveryFeed,
  isAdded: Boolean,
  isLoading: Boolean,
  onAddFeed: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .requiredWidth(160.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(AppTheme.colorScheme.surfaceContainerLowest)
        .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    FeedIcon(
      modifier = Modifier.requiredSize(48.dp),
      icon = "",
      homepageLink = feed.homepageLink,
      showFeedFavIcon = true,
      contentDescription = null,
      shape = MaterialTheme.shapes.small,
    )

    Spacer(Modifier.requiredHeight(8.dp))

    Text(
      modifier = Modifier.basicMarquee(),
      text = feed.name,
      style = MaterialTheme.typography.titleSmall,
      color = AppTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )

    Spacer(Modifier.requiredHeight(4.dp))

    Text(
      text = feed.description,
      style = MaterialTheme.typography.bodySmall,
      color = AppTheme.colorScheme.onSurfaceVariant,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Justify,
      modifier = Modifier.requiredHeight(32.dp),
    )

    Spacer(Modifier.requiredHeight(12.dp))

    Button(
      modifier = Modifier.fillMaxWidth().requiredHeight(32.dp),
      onClick = onAddFeed,
      enabled = !isAdded && !isLoading,
      colors =
        ButtonDefaults.buttonColors(
          containerColor = AppTheme.colorScheme.primary,
          contentColor = AppTheme.colorScheme.onPrimary,
          disabledContainerColor = AppTheme.colorScheme.surfaceContainerHigh,
          disabledContentColor = AppTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
      if (isAdded) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = TwineIcons.Check,
            contentDescription = null,
            modifier = Modifier.requiredSize(16.dp),
            tint = AppTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(Modifier.requiredWidth(4.dp))
          Text(
            text = stringResource(Res.string.discoveryAdded),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colorScheme.onSurfaceVariant,
          )
        }
      } else if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.requiredSize(16.dp),
          color = AppTheme.colorScheme.onPrimary,
          strokeWidth = 2.dp,
        )
      } else {
        Text(
          text = stringResource(Res.string.discoveryAddFeed),
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Medium,
        )
      }
    }
  }
}
