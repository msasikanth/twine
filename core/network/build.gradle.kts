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
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvmToolchain(20)

  jvm()
  @Suppress("OPT_IN_USAGE")
  androidTarget { instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test) }
  listOf(iosArm64(), iosSimulatorArm64())

  applyDefaultHierarchyTemplate()

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(projects.core.model)
        implementation(projects.core.base)

        implementation(libs.kotlinx.datetime)
        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.io)
        implementation(libs.kotlininject.runtime)
        implementation(libs.ktor.core)
        implementation(libs.ktor.client.logging)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ksoup)
        implementation(libs.ksoup.kotlinx.io)
        implementation(libs.ktxml)
        implementation(libs.kermit)
        api(libs.korlibs.string)
      }
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }

    val mobileMain by creating {
      dependsOn(commonMain)

      dependencies { implementation(libs.crashkios.bugsnag) }
    }
    val androidMain by getting {
      dependsOn(mobileMain)
      dependencies {
        implementation(libs.androidx.annotation)
        implementation(libs.ktor.client.okhttp)
      }
    }

    val iosMain by getting {
      dependsOn(mobileMain)
      dependencies { implementation(libs.ktor.client.darwin) }
    }

    jvmMain.dependencies { implementation(libs.ktor.client.okhttp) }
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
