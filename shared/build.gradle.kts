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
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.compose)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  jvmToolchain(21)

  androidTarget { instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test) }

  // spotless:off
  val iOSBinaryFlags =
    listOf(
      "-linker-option", "-framework", "-linker-option", "Metal",
      "-linker-option", "-framework", "-linker-option", "CoreText",
      "-linker-option", "-framework", "-linker-option", "CoreGraphics",
      "-linker-option", "-F/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/Library/Frameworks/",
      "-linker-option", "-framework", "-linker-option", "PurchasesHybridCommon"
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
      implementation(libs.bundles.kotlinx)
      implementation(libs.ktor.core)
      implementation(libs.ktor.client.logging)
      implementation(libs.kotlininject.runtime)
      implementation(libs.androidx.collection)
      implementation(libs.material.color.utilities)
      implementation(libs.ksoup)
      implementation(libs.ksoup.kotlinx.io)
      implementation(libs.windowSizeClass)
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
      api(libs.crashkios.bugsnag)
      implementation(libs.kermit)
      implementation(libs.kermit.bugsnag)
      implementation(libs.reorderable)
      api(libs.filekit)
      implementation(libs.markdown.renderer)
      implementation(libs.markdown.material3)
      implementation(libs.markdown.coil)
      implementation(libs.markdown.code)
      implementation(libs.purchases.core)
      implementation(libs.purchases.ui)
      implementation(libs.viewmodel)
      implementation(libs.navigation)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.client.mock)
    }

    androidMain.dependencies {
      api(libs.androidx.activity.compose)
      api(libs.androidx.appcompat)
      api(libs.androidx.core)
      api(libs.androidx.browser)
      implementation(libs.ktor.client.okhttp)
    }
    val androidInstrumentedTest by getting {
      dependencies {
        implementation(libs.androidx.test.runner)
        implementation(libs.androidx.test.rules)
      }
    }

    iosMain.dependencies { implementation(libs.ktor.client.darwin) }

    compilerOptions { optIn.add("kotlin.time.ExperimentalTime") }
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
  debugImplementation(compose.uiTooling)

  // https://github.com/google/ksp/pull/1021
  add("kspAndroid", libs.kotlininject.compiler)
  add("kspIosArm64", libs.kotlininject.compiler)
  add("kspIosSimulatorArm64", libs.kotlininject.compiler)
}
