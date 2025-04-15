// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  val kotlin_version = "2.0.21"

  id("com.android.application") version "8.7.2" apply false
  id("org.jetbrains.kotlin.android") version kotlin_version apply false
  id("com.google.devtools.ksp") version "$kotlin_version-1.0.27" apply false
  id("org.jetbrains.kotlin.plugin.compose") version kotlin_version apply false
  id("androidx.room") version "2.6.1" apply false

  kotlin("plugin.serialization") version kotlin_version
}