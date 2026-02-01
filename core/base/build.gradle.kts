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
}

kotlin {
  jvmToolchain(21)

  jvm()

  android {
    namespace = "dev.sasikanth.rss.reader.core.base"

    minSdk = libs.versions.android.sdk.min.get().toInt()
    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    androidResources { enable = true }

    withHostTestBuilder {}.configure {}
  }

  listOf(iosArm64(), iosSimulatorArm64())

  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.coroutines)
      implementation(libs.kotlininject.runtime)
      api(libs.uuid)
    }

    commonTest.dependencies { implementation(libs.kotlin.test) }

    androidMain.dependencies {
      implementation(libs.androidx.annotation)
      api(libs.androidx.activity.compose)
      api(libs.androidx.core)
      implementation(libs.crashkios.bugsnag)
    }

    iosMain.dependencies { implementation(libs.crashkios.bugsnag) }
  }

  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
    optIn.add("kotlin.time.ExperimentalTime")
  }
}
