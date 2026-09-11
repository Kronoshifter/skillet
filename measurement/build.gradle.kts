plugins {
  alias(libs.plugins.java.library)
  alias(libs.plugins.jetbrains.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
  compilerOptions {
    languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4
    jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
  }
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

dependencies {
  // Serialization support for data classes
  api(libs.kotlinx.serialization.json)
  // Result type for error handling
  api(libs.kotlin.result)
  // Kotlin reflection for serialization
  implementation(libs.kotlin.reflect)
  // Shared utilities
  implementation(project(":utils"))

  // Testing
  testImplementation(libs.kotest.runner)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.property)
}
