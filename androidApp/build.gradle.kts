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
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.sentry.android)
}

sentry { tracingInstrumentation { enabled = false } }

kotlin {
  jvmToolchain(20)

  androidTarget()
}

android {
  compileSdk = libs.versions.android.sdk.compile.get().toInt()
  namespace = "dev.sasikanth.rss.reader"

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

  defaultConfig {
    applicationId = "dev.sasikanth.rss.reader"
    minSdk = libs.versions.android.sdk.min.get().toInt()
    targetSdk = libs.versions.android.sdk.target.get().toInt()

    versionCode =
      if (project.properties["VERSION_CODE"] != null) {
        (project.properties["VERSION_CODE"] as String).toInt()
      } else {
        1
      }

    versionName =
      if (project.properties["VERSION_NAME"] != null) {
        project.properties["VERSION_NAME"] as String
      } else {
        "1.0.0"
      }
  }

  compileOptions { isCoreLibraryDesugaringEnabled = true }

  signingConfigs {
    create("release") {
      storeFile = file("$rootDir/release/reader.jks")
      storePassword = "${project.properties["READER_KEYSTORE_PASSWORD"]}"
      keyAlias = "reader_alias"
      keyPassword = "${project.properties["READER_KEY_PASSWORD"]}"
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      signingConfig = signingConfigs.getByName("release")

      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug { applicationIdSuffix = ".debug" }
  }

  packaging {
    resources { excludes.add("/META-INF/{AL2.0,LGPL2.1}") }

    // Deprecated ABIs. See https://developer.android.com/ndk/guides/abis
    jniLibs.excludes.add("lib/mips/libsqlite3x.so")
    jniLibs.excludes.add("lib/mips64/libsqlite3x.so")
    jniLibs.excludes.add("lib/armeabi/libsqlite3x.so")
  }

  buildFeatures { buildConfig = true }
}

dependencies {
  implementation(project(":shared"))
  implementation(libs.kotlininject.runtime)
  ksp(libs.kotlininject.compiler)
  implementation(libs.androidx.work)
  implementation(libs.sentry)
  coreLibraryDesugaring(libs.desugarJdk)
  implementation(libs.kotlinx.datetime)
  implementation(libs.bugsnag)
}
