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

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.isFront
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimator
import com.arkivanov.essenty.backhandler.BackHandler

@OptIn(ExperimentalDecomposeApi::class)
internal actual fun <C : Any, T : Any> backAnimation(
  backHandler: BackHandler,
  onBack: () -> Unit,
): StackAnimation<C, T> =
  predictiveBackAnimation(
    backHandler = backHandler,
    animation = stackAnimation(iosLikeSlide()),
    exitModifier = { progress, _ -> Modifier.slideExitModifier(progress = progress) },
    enterModifier = { progress, _ -> Modifier.slideEnterModifier(progress = progress) },
    onBack = onBack,
  )

private fun iosLikeSlide(animationSpec: FiniteAnimationSpec<Float> = tween()): StackAnimator =
  stackAnimator(animationSpec = animationSpec) { factor, direction, content ->
    content(
      Modifier.then(if (direction.isFront) Modifier else Modifier.fade(factor + 1F))
        .offsetXFactor(factor = if (direction.isFront) factor else factor * 0.5F)
    )
  }

private fun Modifier.slideExitModifier(progress: Float): Modifier = offsetXFactor(progress)

private fun Modifier.slideEnterModifier(progress: Float): Modifier =
  fade(progress).offsetXFactor((progress - 1f) * 0.5f)

private fun Modifier.fade(factor: Float) = drawWithContent {
  drawContent()
  drawRect(color = Color(red = 0F, green = 0F, blue = 0F, alpha = (1F - factor) / 4F))
}

private fun Modifier.offsetXFactor(factor: Float): Modifier = layout { measurable, constraints ->
  val placeable = measurable.measure(constraints)

  layout(placeable.width, placeable.height) {
    placeable.placeRelative(x = (placeable.width.toFloat() * factor).toInt(), y = 0)
  }
}
