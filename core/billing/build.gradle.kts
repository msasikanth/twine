/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvmToolchain(20)

  androidTarget { instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test) }
  listOf(iosArm64(), iosSimulatorArm64()).forEach { iOSTarget ->
    iOSTarget.binaries.framework {
      isStatic = true
    }
  }

  sourceSets {
    named { it.lowercase().startsWith("ios") }
      .configureEach { languageSettings { optIn("kotlinx.cinterop.ExperimentalForeignApi") } }

    commonMain.dependencies {
      implementation(projects.core.model)
      implementation(projects.core.base)

      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.coroutines)
      implementation(libs.kotlininject.runtime)
      implementation(libs.purchases.core)
      implementation(libs.purchases.datetime)
    }
  }
}

android {
  namespace = "dev.sasikanth.rss.reader.core.billing"
  compileSdk = libs.versions.android.sdk.compile.get().toInt()

  defaultConfig { minSdk = libs.versions.android.sdk.min.get().toInt() }
}
