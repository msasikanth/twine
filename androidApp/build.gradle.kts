@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose)
}

kotlin {
  android()
  sourceSets {
    val androidMain by getting {
      dependencies {
        implementation(project(":shared"))
      }
    }
  }
}

android {
  compileSdk = libs.versions.android.sdk.compile.get().toInt()
  namespace = "dev.sasikanth.rss.reader"

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

  defaultConfig {
    applicationId = "dev.sasikanth.rss.reader"
    minSdk = libs.versions.android.sdk.min.get().toInt()
    targetSdk = libs.versions.android.sdk.target.get().toInt()
    versionCode = 1
    versionName = "1.0.0"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlin {
    jvmToolchain(11)
  }
}
