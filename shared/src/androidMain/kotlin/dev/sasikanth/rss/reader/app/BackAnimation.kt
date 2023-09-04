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
package dev.sasikanth.rss.reader.app

import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandler

@OptIn(ExperimentalDecomposeApi::class)
actual fun <C : Any, T : Any> backAnimation(
  backHandler: BackHandler,
  onBack: () -> Unit,
): StackAnimation<C, T> =
  predictiveBackAnimation(
    backHandler = backHandler,
    animation = stackAnimation(fade() + scale()),
    enterModifier = { progress, _ -> Modifier.enterModifier(progress) },
    exitModifier = { progress, edge -> Modifier.exitModifier(progress, edge) },
    onBack = onBack,
  )

private fun Modifier.exitModifier(progress: Float, edge: BackEvent.SwipeEdge): Modifier =
  scale(1F - progress * 0.5F)
    .absoluteOffset(
      x =
        when (edge) {
          BackEvent.SwipeEdge.LEFT -> 32.dp * progress
          BackEvent.SwipeEdge.RIGHT -> (-32).dp * progress
          BackEvent.SwipeEdge.UNKNOWN -> 0.dp
        },
    )
    .alpha(((1F - progress) * 2F).coerceAtMost(1F))
    .clip(RoundedCornerShape(size = 64.dp * progress))

private fun Modifier.enterModifier(progress: Float): Modifier = drawWithContent {
  drawContent()
  drawRect(color = Color(red = 0F, green = 0F, blue = 0F, alpha = (1F - progress) / 4F))
}
