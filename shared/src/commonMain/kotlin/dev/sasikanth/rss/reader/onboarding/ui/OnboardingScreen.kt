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

package dev.sasikanth.rss.reader.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.Button
import dev.sasikanth.rss.reader.onboarding.OnboardingEffect
import dev.sasikanth.rss.reader.onboarding.OnboardingEvent
import dev.sasikanth.rss.reader.onboarding.OnboardingViewModel
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.appName
import twine.shared.generated.resources.ic_launcher_foreground
import twine.shared.generated.resources.onboardingGetStarted
import twine.shared.generated.resources.onboardingSubtitle
import twine.shared.generated.resources.onboardingSubtitle2

@Composable
internal fun OnboardingScreen(
  viewModel: OnboardingViewModel,
  onOnboardingDone: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val backgroundColor = AppTheme.colorScheme.primary

  LaunchedEffect(Unit) {
    viewModel.effects.collect { effect ->
      when (effect) {
        OnboardingEffect.NavigateToHome -> onOnboardingDone()
      }
    }
  }

  Scaffold(
    modifier =
      modifier
        .background(AppTheme.colorScheme.surface)
        .background(
          Brush.verticalGradient(
            0f to backgroundColor.copy(alpha = 0f),
            0.60f to backgroundColor.copy(alpha = 0.15f),
            1f to backgroundColor.copy(alpha = 0.65f),
          )
        ),
    containerColor = Color.Transparent,
    bottomBar = {
      Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = stringResource(Res.string.onboardingSubtitle2),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed,
          textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        Button(
          onClick = { viewModel.dispatch(OnboardingEvent.GetStartedClicked) },
          modifier = Modifier.fillMaxWidth().height(56.dp),
          enabled = !state.isPrePopulating,
          shape = MaterialTheme.shapes.extraLarge,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = AppTheme.colorScheme.onPrimary,
              contentColor = AppTheme.colorScheme.primary,
            ),
          elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
        ) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state.isPrePopulating) {
              CircularProgressIndicator(
                modifier = Modifier.requiredSize(24.dp),
                color = AppTheme.colorScheme.primary,
                strokeWidth = 2.dp,
              )
            } else {
              Text(
                text = stringResource(Res.string.onboardingGetStarted).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }

        if (platform !is Platform.Apple) {
          Spacer(Modifier.height(16.dp))
        }
      }
    },
  ) { paddingValues ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = 24.dp)
          .padding(top = 88.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      val backgroundColor = AppTheme.colorScheme.primary
      val backgroundBrush =
        Brush.radialGradient(
          0.17f to backgroundColor.copy(alpha = 0.55f).compositeOver(Color.White),
          1f to backgroundColor,
          center = Offset(20f, 24f),
        )

      Box(
        modifier =
          Modifier.requiredSize(128.dp).background(backgroundBrush, RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          painter = painterResource(Res.drawable.ic_launcher_foreground),
          contentDescription = null,
          tint = AppTheme.colorScheme.inverseOnSurface,
          modifier = Modifier.requiredSize(200.dp),
        )
      }

      Spacer(Modifier.height(40.dp))

      Text(
        text = stringResource(Res.string.appName),
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colorScheme.textEmphasisHigh,
      )

      Text(
        text = stringResource(Res.string.onboardingSubtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = AppTheme.colorScheme.textEmphasisMed,
      )
    }
  }
}
