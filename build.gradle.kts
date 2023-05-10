@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
  // this is necessary to avoid the plugins to be loaded multiple times
  // in each subproject's classloader
  alias(libs.plugins.kotlin.multiplatform).apply(false)
  alias(libs.plugins.kotlin.cocoapods).apply(false)
  alias(libs.plugins.android.application).apply(false)
  alias(libs.plugins.android.library).apply(false)
  alias(libs.plugins.compose).apply(false)
}
