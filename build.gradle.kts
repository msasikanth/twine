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
@file:Suppress("DSL_SCOPE_VIOLATION")

import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
  // this is necessary to avoid the plugins to be loaded multiple times
  // in each subproject's classloader
  alias(libs.plugins.kotlin.multiplatform).apply(false)
  alias(libs.plugins.kotlin.cocoapods).apply(false)
  alias(libs.plugins.android.application).apply(false)
  alias(libs.plugins.android.library).apply(false)
  alias(libs.plugins.compose).apply(false)
  alias(libs.plugins.spotless).apply(false)
  alias(libs.plugins.buildKonfig).apply(false)
}

allprojects {
  apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
  configure<SpotlessExtension> {
    kotlin {
      ktfmt(libs.versions.ktfmt.get()).googleStyle()
      target("src/**/*.kt")
      targetExclude("$buildDir/**/*.kt")
      licenseHeaderFile(rootProject.file("spotless/copyright.txt"))
        .onlyIfContentMatches("missingString")
    }
    kotlinGradle {
      ktfmt(libs.versions.ktfmt.get()).googleStyle()
      target("**/*.kts")
      targetExclude("$buildDir/**/*.kts")
      licenseHeaderFile(rootProject.file("spotless/copyright.txt"), "(^(?![\\/ ]\\*).*$)")
        .onlyIfContentMatches("missingString")
    }
    format("xml") {
      target("src/**/*.xml")
      targetExclude("**/build/", ".idea/")
      trimTrailingWhitespace()
      endWithNewline()
    }
  }
}
