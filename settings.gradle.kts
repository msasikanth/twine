rootProject.name = "RSS Reader"

include(":androidApp")
include(":shared")

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}
