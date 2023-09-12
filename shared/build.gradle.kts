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
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.cocoapods)
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose)
  alias(libs.plugins.sqldelight)
  alias(libs.plugins.ksp)
  alias(libs.plugins.buildKonfig)
  alias(libs.plugins.kotlin.parcelize)
}

buildkonfig {
  packageName = "dev.sasikanth.reader"
  defaultConfigs {
    val sentryDsn = System.getenv("SENTRY_DSN")
    buildConfigField(STRING, "SENTRY_DSN", sentryDsn.orEmpty())
  }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  targetHierarchy.default()

  android()

  // spotless:off
  val iOSBinaryFlags =
    listOf(
      "-linker-option", "-framework", "-linker-option", "Metal",
      "-linker-option", "-framework", "-linker-option", "CoreText",
      "-linker-option", "-framework", "-linker-option", "CoreGraphics",
    )
  // spotless:on

  ios()
  iosX64 { binaries.forEach { it.freeCompilerArgs += iOSBinaryFlags } }

  iosSimulatorArm64() { binaries.forEach { it.freeCompilerArgs += iOSBinaryFlags } }

  cocoapods {
    version = "1.0.0"
    summary = "Multiplatform RSS app built with Kotlin and Compose"
    homepage = "https://github.com/msasikanth/rss_reader"
    ios.deploymentTarget = "15.0"
    podfile = project.file("../iosApp/Podfile")
    pod("Sentry", "~> 8.4.0")

    framework {
      baseName = "shared"
      isStatic = true

      export(libs.decompose)
      export(libs.essenty.lifecycle)
      export(libs.essenty.backhandler)
    }
  }

  sourceSets {
    all {
      languageSettings.optIn("androidx.compose.material.ExperimentalMaterialApi")
      languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
      languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
      languageSettings.optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
    }

    val commonMain by getting {
      dependencies {
        implementation(projects.resources.strings)
        implementation(projects.resources.icons)

        implementation(libs.bundles.compose)
        implementation(libs.bundles.kotlinx)
        implementation(libs.ktor.core)
        implementation(libs.napier)
        implementation(libs.sqldelight.extensions.coroutines)
        implementation(libs.sqldelight.extensions.paging)
        api(libs.decompose)
        implementation(libs.decompose.extensions.compose)
        api(libs.essenty.lifecycle)
        api(libs.essenty.backhandler)
        implementation(libs.kotlininject.runtime)
        implementation(libs.androidx.collection)
        implementation(libs.material.color.utilities)
        implementation(libs.ksoup)
        implementation(libs.sentry)
        implementation(libs.windowSizeClass)
        api(libs.androidx.datastore.okio)
        api(libs.androidx.datastore.preferences)
        api(libs.okio)
        implementation(libs.paging.common)
        implementation(libs.paging.compose)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlinx.coroutines.test)
      }
    }

    val androidMain by getting {
      dependencies {
        api(libs.androidx.activity.compose)
        api(libs.androidx.appcompat)
        api(libs.androidx.core)
        implementation(libs.ktor.client.okhttp)
        implementation(libs.sqldelight.driver.android)
        implementation(libs.coil.compose)
        api(libs.sqliteAndroid)
      }
    }
    val androidInstrumentedTest by getting {
      dependsOn(commonTest)
      dependencies {
        implementation(libs.androidx.test.runner)
        implementation(libs.androidx.test.rules)
      }
    }

    val iosSimulatorArm64Main by getting
    val iosMain by getting {
      iosSimulatorArm64Main.dependsOn(this)
      dependencies {
        implementation(libs.ktor.client.darwin)
        implementation(libs.sqldelight.driver.native)
      }
    }
    val iosX64Test by getting
    val iosArm64Test by getting
    val iosSimulatorArm64Test by getting
    val iosTest by getting
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
  sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/resources")
  sourceSets["main"].resources.srcDirs("src/commonMain/resources")

  defaultConfig {
    minSdk = libs.versions.android.sdk.min.get().toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlin { jvmToolchain(11) }
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
  add("kspIosX64", libs.kotlininject.compiler)
  add("kspIosArm64", libs.kotlininject.compiler)
  add("kspIosSimulatorArm64", libs.kotlininject.compiler)
}
