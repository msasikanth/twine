/*
 * Copyright 2026 Sasikanth Miriyampalli
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
import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  // this is necessary to avoid the plugins to be loaded multiple times
  // in each subproject's classloader
  alias(libs.plugins.kotlin.multiplatform).apply(false)
  alias(libs.plugins.android.application).apply(false)
  alias(libs.plugins.android.library).apply(false)
  alias(libs.plugins.compose).apply(false)
  alias(libs.plugins.spotless).apply(false)
  alias(libs.plugins.kotlin.parcelize).apply(false)
  alias(libs.plugins.kotlin.serialization).apply(false)
  alias(libs.plugins.kotlin.compose).apply(false)
  alias(libs.plugins.bugsnag).apply(false)
}

subprojects {
  tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions { freeCompilerArgs.add("-Xskip-prerelease-check") }
  }
}

allprojects {
  configurations.all {
    resolutionStrategy.eachDependency {
      if (requested.group.startsWith("org.jetbrains.compose") && requested.name.contains("uikit")) {
        // Force old uikit artifacts to use the new ios naming convention
        // filekit-compose 0.8.8 depends on Compose 1.7.1 which uses old uikit naming
        // Old: ui-uikitsimarm64 -> New: ui-iossimulatorarm64
        // Old: ui-uikitarm64 -> New: ui-iosarm64
        // Old: ui-uikitx64 -> New: ui-iosx64
        val newName =
          requested.name
            .replace("uikitsimarm64", "iossimulatorarm64")
            .replace("uikitarm64", "iosarm64")
            .replace("uikitx64", "iosx64")
        useTarget("${requested.group}:$newName:1.11.0-beta01")
      }
    }
  }
  apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
  configure<SpotlessExtension> {
    kotlin {
      ktfmt(libs.versions.ktfmt.get()).googleStyle()
      target("src/**/*.kt")
      targetExclude("${layout.buildDirectory}/**/*.kt")
      licenseHeaderFile(rootProject.file("spotless/copyright.txt"))
        .onlyIfContentMatches("missingString")
    }
    kotlinGradle {
      ktfmt(libs.versions.ktfmt.get()).googleStyle()
      target("*.kts")
      targetExclude("${layout.buildDirectory}/**/*.kts")
      licenseHeaderFile(rootProject.file("spotless/copyright.txt"), "(^(?![\\/ ]\\*).*$)")
        .onlyIfContentMatches("missingString")
      toggleOffOn()
    }
    format("xml") {
      target("src/**/*.xml")
      targetExclude("**/build/", ".idea/")
      trimTrailingWhitespace()
      endWithNewline()
    }
  }
}
