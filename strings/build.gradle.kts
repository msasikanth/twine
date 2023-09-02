import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose)
  alias(libs.plugins.ksp)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  targetHierarchy.default()

  jvm()
  listOf(iosX64(), iosArm64(), iosSimulatorArm64())

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.lyricist)
        api(libs.compose.foundation)
      }
    }
    val commonTest by getting { dependencies { implementation(kotlin("test")) } }
  }
}

ksp { arg("lyricist.packageName", "dev.sasikanth.rss.reader.resources.strings") }

dependencies { add("kspCommonMainMetadata", libs.lyricist.processor) }

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
  if (name != "kspCommonMainKotlinMetadata") {
    dependsOn("kspCommonMainKotlinMetadata")
  }
}

kotlin.sourceSets.commonMain { kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin") }
