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

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose)
  alias(libs.plugins.ksp)
}

kotlin {
  androidTarget()
  jvm()
  listOf(iosArm64(), iosSimulatorArm64())

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.compose.foundation)
        api(libs.compose.material.icons.extended)
      }
    }
    val commonTest by getting { dependencies { implementation(kotlin("test")) } }
  }
}

android {
  compileSdk = libs.versions.android.sdk.compile.get().toInt()
  namespace = "dev.sasikanth.rss.reader.resources.icons"

  defaultConfig { minSdk = libs.versions.android.sdk.min.get().toInt() }
}
