/*
 * Copyright 2024 Sasikanth Miriyampalli
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
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  jvmToolchain(20)

  jvm()

  androidTarget()

  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "readability"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.ksoup)
      implementation(libs.uri)
    }
  }
}

android {
  namespace = "dev.sasikanth.readability"
  compileSdk = libs.versions.android.sdk.compile.get().toInt()
  defaultConfig { minSdk = libs.versions.android.sdk.min.get().toInt() }
}
