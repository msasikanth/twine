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

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.sqldelight)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvmToolchain(21)

  compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

  android {
    namespace = "dev.sasikanth.rss.reader.data"

    minSdk = libs.versions.android.sdk.min.get().toInt()
    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    withHostTestBuilder {}.configure {}
    withDeviceTest { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
  }
  listOf(iosArm64(), iosSimulatorArm64())

  sourceSets {
    commonMain.dependencies {
      api(projects.core.model)
      api(projects.core.base)
      api(projects.core.network)

      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.coroutines)
      implementation(libs.kotlininject.runtime)
      implementation(libs.kermit)
      implementation(libs.crashkios.bugsnag)
      api(libs.sqldelight.extensions.coroutines)
      api(libs.sqldelight.extensions.paging)
      api(libs.androidx.datastore.preferences)
      api(libs.androidx.datastore.okio)
      api(libs.uuid)
      api(libs.ktor.core)
      implementation(libs.ktor.content.negotiation)
      implementation(libs.ktor.json)
      api(libs.kotlinx.serialization.json)
      implementation(libs.filekit)
      implementation(libs.bundles.xmlutil)
      implementation(libs.stately.isolate)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.client.mock)
    }

    androidMain.dependencies {
      implementation(libs.androidx.annotation)
      implementation(libs.sqldelight.driver.android)
      api(libs.sqliteAndroid)
    }

    iosMain.dependencies { implementation(libs.sqldelight.driver.native) }
  }

  compilerOptions { optIn.add("kotlin.time.ExperimentalTime") }
}

sqldelight {
  databases {
    create("ReaderDatabase") {
      packageName.set("dev.sasikanth.rss.reader.data.database")
      dialect(libs.sqldelight.sqlite.dialect)
      schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
      verifyMigrations.set(true)
    }
  }
}
