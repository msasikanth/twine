@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

/*
 * Copyright 2025 Sasikanth Miriyampalli
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

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvmToolchain(20)

  androidTarget { instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test) }
  listOf(iosArm64(), iosSimulatorArm64())

  sourceSets {
    named { it.lowercase().startsWith("ios") }.configureEach {
      languageSettings {
        optIn("kotlinx.cinterop.ExperimentalForeignApi")
      }
    }

    commonMain.dependencies {
      implementation(projects.core.model)
      implementation(projects.core.base)

      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.coroutines)
      implementation(libs.kotlininject.runtime)
      implementation(libs.purchases.core)
      implementation(libs.purchases.datetime)
    }
  }
}
