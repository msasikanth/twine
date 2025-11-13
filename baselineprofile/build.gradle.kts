plugins {
  alias(libs.plugins.android.test)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.baselineprofile)
}

kotlin { androidTarget() }

android {
  namespace = "dev.sasikanth.twine.baselineprofile"
  compileSdk { version = release(36) }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  defaultConfig {
    minSdk = 28
    targetSdk = 36

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  targetProjectPath = ":androidApp"
}

// This is the configuration block for the Baseline Profile plugin.
// You can specify to run the generators on a managed devices or connected devices.
baselineProfile { useConnectedDevices = true }

dependencies {
  implementation(libs.junit)
  implementation(libs.espresso.core)
  implementation(libs.uiautomator)
  implementation(libs.benchmark.macro.junit4)
}

androidComponents {
  onVariants { v ->
    val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
    v.instrumentationRunnerArguments.put(
      "targetAppId",
      v.testedApks.map { artifactsLoader.load(it)?.applicationId }
    )
  }
}
