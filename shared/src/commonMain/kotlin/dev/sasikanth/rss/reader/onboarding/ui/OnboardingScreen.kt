/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.onboarding.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.dropShadow
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
  modifier: Modifier = Modifier
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.effects.collect { effect ->
      when (effect) {
        OnboardingEffect.NavigateToHome -> onOnboardingDone()
      }
    }
  }

  Scaffold(
    modifier = modifier,
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    bottomBar = {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
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
          elevation = ButtonDefaults.elevatedButtonElevation()
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            if (state.isPrePopulating) {
              CircularProgressIndicator(
                modifier = Modifier.requiredSize(24.dp),
                color = AppTheme.colorScheme.primary,
                strokeWidth = 2.dp
              )
            } else {
              Text(
                text = stringResource(Res.string.onboardingGetStarted).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colorScheme.inverseOnSurface
              )
            }
          }
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      val backgroundColor = AppTheme.colorScheme.primary
      val backgroundBrush =
        Brush.radialGradient(
          0.17f to backgroundColor.copy(alpha = 0.55f).compositeOver(Color.White),
          1f to backgroundColor,
          center = Offset(20f, 24f)
        )

      val infiniteTransition = rememberInfiniteTransition()
      val glowRadius by
        infiniteTransition.animateFloat(
          initialValue = 20f,
          targetValue = 70f,
          animationSpec =
            infiniteRepeatable(
              animation = tween(2000, easing = LinearEasing),
              repeatMode = RepeatMode.Reverse
            )
        )

      val glowOffsetX by
        infiniteTransition.animateFloat(
          initialValue = -10f,
          targetValue = 10f,
          animationSpec =
            infiniteRepeatable(
              animation = tween(3000, easing = LinearEasing),
              repeatMode = RepeatMode.Reverse
            )
        )

      val glowOffsetY by
        infiniteTransition.animateFloat(
          initialValue = -10f,
          targetValue = 10f,
          animationSpec =
            infiniteRepeatable(
              animation = tween(4000, easing = LinearEasing),
              repeatMode = RepeatMode.Reverse
            )
        )

      Box(
        modifier =
          Modifier.requiredSize(128.dp)
            .dropShadow(shape = RoundedCornerShape(32.dp)) {
              color = backgroundColor
              radius = glowRadius.dp.toPx()
              offset = Offset(glowOffsetX.dp.toPx(), glowOffsetY.dp.toPx())
            }
            .background(backgroundBrush, RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
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
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colorScheme.textEmphasisHigh
      )

      Text(
        text = stringResource(Res.string.onboardingSubtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = AppTheme.colorScheme.textEmphasisMed
      )
    }
  }
}
