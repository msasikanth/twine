import com.android.build.api.dsl.ManagedVirtualDevice
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

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
}

kotlin {
  jvmToolchain(20)

  @Suppress("OPT_IN_USAGE")
  androidTarget { instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test) }
  listOf(iosArm64(), iosSimulatorArm64())

  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.model)
      implementation(projects.core.base)

      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.coroutines)
      implementation(libs.kotlininject.runtime)
      implementation(libs.ktor.core)
      implementation(libs.ktor.client.logging)
      implementation(libs.ksoup)
      // TODO: Extract logging abstraction into separate module
      implementation(libs.napier)
      implementation(libs.sentry)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }

    androidMain.dependencies {
      implementation(libs.androidx.annotation)
      implementation(libs.ktor.client.okhttp)
    }

    iosMain.dependencies { implementation(libs.ktor.client.darwin) }
  }
}

android {
  namespace = "dev.sasikanth.rss.reader.core.network"
  compileSdk = libs.versions.android.sdk.compile.get().toInt()

  defaultConfig {
    minSdk = libs.versions.android.sdk.min.get().toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  @Suppress("UnstableApiUsage")
  testOptions {
    managedDevices {
      devices {
        maybeCreate<ManagedVirtualDevice>("pixel2Api31").apply {
          device = "Pixel 2"
          apiLevel = 31
          systemImageSource = "aosp"
        }
      }
    }
  }
}
