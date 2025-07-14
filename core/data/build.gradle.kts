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
  alias(libs.plugins.sqldelight)
}

kotlin {
  jvmToolchain(20)

  androidTarget()
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
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }

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

android {
  namespace = "dev.sasikanth.rss.reader.data"
  compileSdk = libs.versions.android.sdk.compile.get().toInt()

  defaultConfig {
    minSdk = libs.versions.android.sdk.min.get().toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
}
