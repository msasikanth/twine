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

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "Twine"
      packageVersion = "1.0.0"
      modules("java.sql")
      modules("jdk.unsupported")

      macOS {
        bundleID = "dev.sasikanth.rss.reader"
        iconFile.set(project.file("icon.icns"))

        infoPlist { extraKeysRawXml = macExtraPlistKeys }
      }

      windows { iconFile.set(project.file("icon.ico")) }

      linux {
        debMaintainer = "contact@sasikanth.dev"
        iconFile.set(project.file("icon.png"))
      }
    }
  }
}

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
