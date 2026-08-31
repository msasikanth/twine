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

import org.gradle.api.artifacts.component.ModuleComponentIdentifier
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

// vlcj pulls the modular jna-jpms artifacts, which carry the same com.sun.jna packages that
// filekit brings in through plain jna. Shipping both leaves the winner down to classpath order,
// and the bundled native library below has to match whichever one loads.
configurations.named("jvmRuntimeClasspath") {
  exclude(group = "net.java.dev.jna", module = "jna-jpms")
  exclude(group = "net.java.dev.jna", module = "jna-platform-jpms")
}

// sqlite-jdbc and JNA both unpack their native libraries into a temp directory on first use,
// and an App Store build may only load code that is signed inside its own bundle. Unpacking
// them here puts them in app resources, where Compose signs them like the other native
// libraries. The jars are taken from the resolved runtime classpath so the extracted copies
// cannot drift from the versions actually on it.
fun nativesFrom(module: String): Provider<List<FileTree>> =
  configurations.named("jvmRuntimeClasspath").map { configuration ->
    configuration.incoming
      .artifactView {
        componentFilter { id -> id is ModuleComponentIdentifier && id.module == module }
      }
      .files
      .map { zipTree(it) }
  }

val unpackNativeLibraries by
  tasks.registering(Sync::class) {
    val natives =
      mapOf(
        "macos-arm64" to
          listOf(
            "sqlite-jdbc" to "org/sqlite/native/Mac/aarch64/**",
            "jna" to "com/sun/jna/darwin-aarch64/**",
          ),
        "macos-x64" to
          listOf(
            "sqlite-jdbc" to "org/sqlite/native/Mac/x86_64/**",
            "jna" to "com/sun/jna/darwin-x86-64/**",
          ),
      )

    natives.forEach { (target, sources) ->
      sources.forEach { (module, path) ->
        from(nativesFrom(module)) {
          include(path)
          // jpackage only code-signs .dylib, and JNA falls back to that extension itself.
          eachFile { relativePath = RelativePath(true, target, name.replace(".jnilib", ".dylib")) }
        }
      }
    }

    includeEmptyDirs = false
    into(layout.buildDirectory.dir("appResources"))
  }

compose.desktop {
  application {
    mainClass = "dev.sasikanth.rss.reader.MainKt"

    if (System.getProperty("os.name").contains("Mac")) {
      jvmArgs("-Xdock:name=Twine")
    }

    nativeDistributions {
      targetFormats(*hostTargetFormats)
      packageName = "Twine"
      packageVersion = appPackageVersion
      modules("java.sql")
      modules("jdk.unsupported")

      buildTypes.release.proguard { configurationFiles.from(project.file("proguard-rules.pro")) }

      appResourcesRootDir.fileProvider(unpackNativeLibraries.map { it.destinationDir })

      macOS {
        bundleID = "dev.sasikanth.rss.reader"
        iconFile.set(project.file("icon.icns"))

        infoPlist { extraKeysRawXml = macExtraPlistKeys }

        // Covers every macOS target format (app image, dmg, pkg) - dmg/pkg-specific
        // version properties don't exist for the app-image format, so the app's own
        // Info.plist (baked in during createReleaseDistributable) would otherwise fall
        // back to the shared top-level packageVersion.
        packageVersion = appPackageVersion
        // CFBundleVersion: must increase on every App Store upload of a given
        // CFBundleShortVersionString, so it tracks the build counter, not the release.
        packageBuildVersion = appVersionCode

        minimumSystemVersion = "12.0"

        if (isMacAppStoreBuild) {
          appStore = true
          entitlementsFile.set(project.file("appstore.entitlements"))
          runtimeEntitlementsFile.set(project.file("appstore-runtime.entitlements"))
          provisioningProfile.set(project.file("embedded.provisionprofile"))
          runtimeProvisioningProfile.set(project.file("embedded.provisionprofile"))
        }
      }

      windows {
        iconFile.set(project.file("icon.ico"))
        menu = true
        menuGroup = "Twine"
        // Keeps repeat installs upgrading in place instead of stacking up as separate
        // entries; jpackage has no stable default, so it has to be pinned here.
        upgradeUuid = "d4e54722-7924-4736-9d37-fb158d0f8f52"
      }

      linux {
        debMaintainer = "contact@sasikanth.dev"
        // Lives in resources so the app can also load it as the AWT window icon.
        iconFile.set(project.file("src/jvmMain/resources/icon.png"))
      }
    }
  }
}

// Compose validates packageVersion against *every* declared format, so a globally declared
// Msi made macOS-only builds fail the Windows rule (major <= 255) on versions macOS allows.
val hostTargetFormats: Array<TargetFormat>
  get() {
    val os = System.getProperty("os.name")
    return when {
      os.contains("Mac") -> arrayOf(TargetFormat.Dmg, TargetFormat.Pkg)
      os.startsWith("Win") -> arrayOf(TargetFormat.Msi)
      else -> arrayOf(TargetFormat.Deb)
    }
  }

val isMacAppStoreBuild: Boolean
  get() = providers.gradleProperty("twine.macAppStore").getOrElse("false").toBoolean()

val appVersionName: String
  get() = providers.gradleProperty("VERSION_NAME").getOrElse("1.0.0").removePrefix("v")

val appVersionCode: String
  get() = providers.gradleProperty("VERSION_CODE").getOrElse("1")

// jpackage only accepts dotted numbers, so a pre-release tag like `3.9.0-beta1` has to
// drop its suffix before it reaches any of the installer bundlers.
val appPackageVersion: String
  get() = appVersionName.substringBefore('-')

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
