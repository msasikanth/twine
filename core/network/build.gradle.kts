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
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvmToolchain(21)

  compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

  jvm()

  android {
    namespace = "dev.sasikanth.rss.reader.core.network"

    minSdk = libs.versions.android.sdk.min.get().toInt()
    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    withHostTestBuilder {}.configure {}
    withDeviceTest { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
  }
  listOf(iosArm64(), iosSimulatorArm64())

  sourceSets {
    all { languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi") }

    commonMain.dependencies {
      implementation(projects.core.model)
      implementation(projects.core.base)

      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.coroutines)
      implementation(libs.kotlinx.io)
      implementation(libs.kotlininject.runtime)
      implementation(libs.ktor.core)
      implementation(libs.ktor.client.logging)
      implementation(libs.ktor.content.negotiation)
      implementation(libs.ktor.json)
      implementation(libs.ktor.resources)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.ksoup)
      implementation(libs.ksoup.kotlinx.io)
      implementation(libs.ktxml)
      implementation(libs.kermit)
      api(libs.korlibs.string)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.client.mock)
    }

    androidMain.dependencies {
      implementation(libs.androidx.annotation)
      implementation(libs.ktor.client.okhttp)
      implementation(libs.crashkios.bugsnag)
    }

    iosMain.dependencies {
      implementation(libs.ktor.client.darwin)
      implementation(libs.crashkios.bugsnag)
    }

    jvmMain.dependencies { implementation(libs.ktor.client.okhttp) }

    compilerOptions { optIn.add("kotlin.time.ExperimentalTime") }
  }
}
