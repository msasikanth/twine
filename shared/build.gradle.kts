plugins {
  kotlin("multiplatform")
  kotlin("native.cocoapods")
  id("com.android.library")
  id("org.jetbrains.compose")
  id("app.cash.sqldelight")
}

kotlin {
  android()

  ios()
  iosSimulatorArm64()

  cocoapods {
    version = "1.0.0"
    summary = "RSS Reader"
    homepage = "https://github.com/msasikanth/rss_reader"
    ios.deploymentTarget = "14.1"
    podfile = project.file("../iosApp/Podfile")
    framework {
      baseName = "shared"
      isStatic = true
    }
    extraSpecAttributes["resources"] = "['src/commonMain/resources/**', 'src/iosMain/resources/**']"
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material)
        implementation(compose.material3)
        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        implementation(compose.components.resources)
        implementation("io.ktor:ktor-client-core:2.3.0")
        implementation("io.ktor:ktor-client-logging:2.2.4")
        implementation("io.github.aakira:napier:2.6.1")
        implementation("app.cash.sqldelight:coroutines-extensions:2.0.0-alpha05")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.0")
      }
    }
    val androidMain by getting {
      dependencies {
        api("androidx.activity:activity-compose:1.6.1")
        api("androidx.appcompat:appcompat:1.6.1")
        api("androidx.core:core-ktx:1.9.0")
        implementation("io.ktor:ktor-client-okhttp:2.2.4")
        implementation("app.cash.sqldelight:android-driver:2.0.0-alpha05")
        implementation("io.coil-kt:coil-compose:2.3.0")
      }
    }

    val iosSimulatorArm64Main by getting
    val iosMain by getting {
      dependsOn(commonMain)
      iosSimulatorArm64Main.dependsOn(this)
      dependencies {
        implementation("io.ktor:ktor-client-darwin:2.2.4")
        implementation("app.cash.sqldelight:native-driver:2.0.0-alpha05")
      }
    }
  }
}

sqldelight {
  databases {
    create("ReaderDatabase") {
      packageName.set("dev.sasikanth.rss.reader.database")
    }
  }
}

android {
  compileSdk = (findProperty("android.compileSdk") as String).toInt()
  namespace = "dev.sasikanth.rss.reader.common"

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  sourceSets["main"].res.srcDirs("src/androidMain/res")
  sourceSets["main"].resources.srcDirs("src/commonMain/resources")

  defaultConfig {
    minSdk = (findProperty("android.minSdk") as String).toInt()
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlin {
    jvmToolchain(11)
  }
}
