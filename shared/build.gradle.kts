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
import com.android.build.api.dsl.ManagedVirtualDevice
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.cocoapods)
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.sqldelight)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.kotlin.serialization)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  jvmToolchain(20)

  androidTarget { instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test) }

  // spotless:off
  val iOSBinaryFlags =
    listOf(
      "-linker-option", "-framework", "-linker-option", "Metal",
      "-linker-option", "-framework", "-linker-option", "CoreText",
      "-linker-option", "-framework", "-linker-option", "CoreGraphics",
      )
  // spotless:on

  iosArm64 { binaries.forEach { it.freeCompilerArgs += iOSBinaryFlags } }
  iosSimulatorArm64 { binaries.forEach { it.freeCompilerArgs += iOSBinaryFlags } }

  applyDefaultHierarchyTemplate()

  cocoapods {
    version = "1.0.0"
    summary = "Multiplatform RSS app built with Kotlin and Compose"
    homepage = "https://github.com/msasikanth/rss_reader"
    ios.deploymentTarget = "15.0"
    podfile = project.file("../iosApp/Podfile")
    pod("Bugsnag")

    framework {
      baseName = "shared"
      isStatic = true

      export(libs.decompose)
      export(libs.essenty.lifecycle)
      export(libs.essenty.backhandler)
      export(libs.crashkios.bugsnag)
    }
  }

  compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

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

      implementation(projects.resources.strings)
      implementation(projects.resources.icons)
      implementation(projects.thirdparty.readability)

      implementation(libs.bundles.compose)
      implementation(libs.bundles.kotlinx)
      implementation(libs.ktor.core)
      implementation(libs.ktor.client.logging)
      implementation(libs.sqldelight.extensions.coroutines)
      implementation(libs.sqldelight.extensions.paging)
      api(libs.decompose)
      implementation(libs.decompose.extensions.compose)
      api(libs.essenty.lifecycle)
      implementation(libs.essenty.lifecycle.coroutines)
      api(libs.essenty.backhandler)
      implementation(libs.kotlininject.runtime)
      implementation(libs.androidx.collection)
      implementation(libs.material.color.utilities)
      implementation(libs.ksoup)
      implementation(libs.windowSizeClass)
      api(libs.androidx.datastore.okio)
      api(libs.androidx.datastore.preferences)
      api(libs.okio)
      implementation(libs.paging.common)
      implementation(libs.paging.compose)
      implementation(libs.stately.isolate)
      implementation(libs.stately.iso.collections)
      implementation(libs.bundles.xmlutil)
      api(libs.webview)
      implementation(libs.uuid)
      api(libs.coil.compose)
      api(libs.coil.network)
      api(libs.coil.svg)
      api(libs.crashkios.bugsnag)
      implementation(libs.kermit)
      implementation(libs.kermit.bugsnag)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
    }

    androidMain.dependencies {
      api(libs.androidx.activity.compose)
      api(libs.androidx.appcompat)
      api(libs.androidx.core)
      api(libs.androidx.browser)
      implementation(libs.ktor.client.okhttp)
      implementation(libs.sqldelight.driver.android)
      api(libs.sqliteAndroid)
    }
    val androidInstrumentedTest by getting {
      dependencies {
        implementation(libs.androidx.test.runner)
        implementation(libs.androidx.test.rules)
      }
    }

    iosMain.dependencies {
      implementation(libs.ktor.client.darwin)
      implementation(libs.sqldelight.driver.native)
    }
  }
}

sqldelight {
  databases {
    create("ReaderDatabase") {
      packageName.set("dev.sasikanth.rss.reader.database")
      dialect(libs.sqldelight.sqlite.dialect)
      schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
      verifyMigrations.set(true)
    }
  }
}

android {
  compileSdk = libs.versions.android.sdk.compile.get().toInt()
  namespace = "dev.sasikanth.rss.reader.common"

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

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

dependencies {
  // https://github.com/google/ksp/pull/1021
  add("kspAndroid", libs.kotlininject.compiler)
  add("kspIosArm64", libs.kotlininject.compiler)
  add("kspIosSimulatorArm64", libs.kotlininject.compiler)
}
