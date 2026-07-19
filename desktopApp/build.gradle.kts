/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

import java.time.LocalDate
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose)
  alias(libs.plugins.kotlin.compose)
}

kotlin {
  jvmToolchain(21)

  jvm()

  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(projects.shared)
        implementation(compose.desktop.currentOs)
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.kotlininject.runtime)
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "dev.sasikanth.rss.reader.MainKt"

    if (System.getProperty("os.name").contains("Mac")) {
      jvmArgs("-Xdock:name=Twine")
    }

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Pkg)
      packageName = "Twine"
      packageVersion = "1.0.0"
      modules("java.sql")
      modules("jdk.unsupported")

      buildTypes.release.proguard { configurationFiles.from(project.file("proguard-rules.pro")) }

      macOS {
        bundleID = "dev.sasikanth.rss.reader"
        iconFile.set(project.file("icon.icns"))

        infoPlist { extraKeysRawXml = macExtraPlistKeys }

        // Covers every macOS target format (app image, dmg, pkg) - dmg/pkg-specific
        // version properties don't exist for the app-image format, so the app's own
        // Info.plist (baked in during createReleaseDistributable) would otherwise fall
        // back to the shared top-level packageVersion instead of picking up CalVer.
        packageVersion = macPackageVersion
        packageBuildVersion = macPackageBuildVersion

        minimumSystemVersion = "12.0"

        if (isMacAppStoreBuild) {
          appStore = true
          entitlementsFile.set(project.file("appstore.entitlements"))
          runtimeEntitlementsFile.set(project.file("appstore-runtime.entitlements"))
          provisioningProfile.set(project.file("embedded.provisionprofile"))
          runtimeProvisioningProfile.set(project.file("embedded.provisionprofile"))
        }
      }

      windows { iconFile.set(project.file("icon.ico")) }

      linux {
        debMaintainer = "contact@sasikanth.dev"
        iconFile.set(project.file("icon.png"))
      }
    }
  }
}

val isMacAppStoreBuild: Boolean
  get() = providers.gradleProperty("twine.macAppStore").getOrElse("false").toBoolean()

val macPackageVersion: String
  get() {
    val today = LocalDate.now()
    val defaultVersion = "%d.%02d.%02d".format(today.year, today.monthValue, today.dayOfMonth)
    return providers.gradleProperty("twine.macVersion").getOrElse(defaultVersion)
  }

val macPackageBuildVersion: String
  get() = providers.gradleProperty("twine.macBuildVersion").getOrElse("1")

val macExtraPlistKeys: String
  get() =
    """
    <key>CFBundleURLTypes</key>
    <array>
	    <dict>
		    <key>CFBundleURLName</key>
		    <string>dev.sasikanth.rss.reader</string>
		    <key>CFBundleURLSchemes</key>
		    <array>
			    <string>twine</string>
		    </array>
	    </dict>
    </array>
    """
