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

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.compose)
}

kotlin {
  jvmToolchain(21)

  jvm()

  android {
    namespace = "dev.sasikanth.rss.reader.resources.icons"

    minSdk = libs.versions.android.sdk.min.get().toInt()
    compileSdk = libs.versions.android.sdk.compile.get().toInt()
  }

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
