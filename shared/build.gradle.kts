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

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.compose)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  val isFoss =
    project.findProperty("twine.isFoss")?.toString()?.toBoolean()
      ?: gradle.startParameter.taskNames.any { it.contains("Foss", ignoreCase = true) }

  jvmToolchain(21)

  // spotless:off
  val iOSBinaryFlags =
    listOf(
      "-linker-option", "-framework", "-linker-option", "Metal",
      "-linker-option", "-framework", "-linker-option", "CoreText",
      "-linker-option", "-framework", "-linker-option", "CoreGraphics",
    )
  // spotless:on

  listOf(iosArm64(), iosSimulatorArm64()).forEach { iOSTarget ->
    iOSTarget.binaries.framework {
      baseName = "shared"
      isStatic = true

      freeCompilerArgs += iOSBinaryFlags

      export(libs.crashkios.bugsnag)
    }
  }

  applyDefaultHierarchyTemplate()

  compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

  jvm()

  android {
    namespace = "dev.sasikanth.rss.reader.common"

    minSdk = libs.versions.android.sdk.min.get().toInt()
    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    androidResources { enable = true }
    withHostTestBuilder {}.configure {}
    withDeviceTestBuilder {}.configure {}
  }

  sourceSets {
    all {
      languageSettings.optIn("androidx.compose.material.ExperimentalMaterialApi")
      languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
      languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
      languageSettings.optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
    }

    commonMain.dependencies {
      api(projects.core.base)
      api(projects.core.model)
      api(projects.core.network)
      api(projects.core.data)

      implementation(projects.resources.icons)

      implementation(libs.bundles.compose)
      implementation(libs.compose.material3.windowsizeclass)
      implementation(libs.bundles.kotlinx)
      implementation(libs.ktor.core)
      implementation(libs.ktor.client.logging)
      implementation(libs.kotlininject.runtime)
      implementation(libs.androidx.collection)
      implementation(libs.ksoup)
      implementation(libs.ksoup.kotlinx.io)
      api(libs.androidx.datastore.okio)
      api(libs.androidx.datastore.preferences)
      implementation(libs.paging.common)
      implementation(libs.paging.compose)
      implementation(libs.stately.isolate)
      implementation(libs.stately.iso.collections)
      implementation(libs.bundles.xmlutil)
      implementation(libs.uuid)
      api(libs.coil.compose)
      api(libs.coil.network)
      api(libs.coil.svg)
      implementation(libs.kermit)
      implementation(libs.reorderable)
      api(libs.filekit)
      implementation(libs.markdown.renderer)
      implementation(libs.markdown.material3)
      implementation(libs.markdown.coil)
      implementation(libs.markdown.code)
      implementation(libs.viewmodel)
      implementation(libs.lifecycle.runtime.compose)
      implementation(libs.navigation)
      implementation(libs.material.kolor)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.client.mock)
    }

    androidMain {
      if (isFoss) {
        kotlin.srcDir("src/androidFoss/kotlin")
      } else {
        kotlin.srcDir("src/androidFull/kotlin")
      }

      dependencies {
        api(libs.androidx.activity.compose)
        api(libs.androidx.appcompat)
        api(libs.androidx.core)
        api(libs.androidx.browser)
        implementation(libs.ktor.client.okhttp)
        api(libs.crashkios.bugsnag)
        implementation(libs.kermit.bugsnag)
        implementation(libs.purchases.core)
        implementation(libs.purchases.ui)

        if (!isFoss) {
          implementation(libs.googlePlayReview)
        }
      }
    }

    iosMain.dependencies {
      implementation(libs.ktor.client.darwin)
      implementation(libs.crashkios.bugsnag)
      implementation(libs.kermit.bugsnag)
      implementation(libs.purchases.core)
      implementation(libs.purchases.ui)
    }

    jvmMain.dependencies {
      implementation(libs.ktor.client.okhttp)
      implementation(libs.htmlunit)
    }

    compilerOptions { optIn.add("kotlin.time.ExperimentalTime") }
  }
}

composeCompiler {
  reportsDestination = layout.buildDirectory.dir("compose_compiler")
  metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
  "androidRuntimeClasspath"(libs.compose.ui.tooling)

  // https://github.com/google/ksp/pull/1021
  add("kspAndroid", libs.kotlininject.compiler)
  add("kspIosArm64", libs.kotlininject.compiler)
  add("kspIosSimulatorArm64", libs.kotlininject.compiler)
  add("kspJvm", libs.kotlininject.compiler)
}
