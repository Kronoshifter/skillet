import com.ncorti.ktfmt.gradle.TrailingCommaManagementStrategy
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.FailOnSeverity

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.androidx.room)
  alias(libs.plugins.detekt)
  alias(libs.plugins.ktfmt)
}

android {
  namespace = "com.kronos.skilletapp"
  compileSdk = 37

  defaultConfig {
    applicationId = "com.kronos.skilletapp"
    minSdk = 30
    targetSdk = 37
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures { compose = true }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
      excludes += "META-INF/DEPENDENCIES"

      pickFirsts += "mozilla/public-suffix-list.txt"
    }
  }

  testOptions { unitTests.all { it.useJUnitPlatform() } }
}

kotlin {
  compilerOptions {
    languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4
    jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
  }
}

room { schemaDirectory("$projectDir/schemas") }

detekt {
  config.setFrom("$rootDir/detekt.yml")
  buildUponDefaultConfig = true
  allRules = false
  source.setFrom("src/main/kotlin", "src/main/java")
  failOnSeverity = FailOnSeverity.Error
  baseline = file("$rootDir/detekt-baseline.xml")
}

ktfmt {
  googleStyle()
  removeUnusedImports = true
  maxWidth = 100
  trailingCommaManagementStrategy = TrailingCommaManagementStrategy.COMPLETE
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

tasks.withType<Detekt>().configureEach {
  exclude("**/generated/**")
  exclude("**/test/**")
  exclude("**/androidTest/**")
}

dependencies {
  // AndroidX
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.activity.compose)

  // Compose
  implementation(platform(libs.compose.bom))
  implementation(libs.bundles.compose)

  // Navigation
  implementation(libs.navigation.ui.ktx)
  implementation(libs.navigation.compose)

  // Koin
  implementation(platform(libs.koin.bom))
  implementation(libs.bundles.koin)

  // Utils
  implementation(libs.speed.dial.compose) // Speed Dial Composable
  implementation(libs.antlr)
  implementation(libs.reorderable)
  implementation(libs.skrapeit)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlin.result)
  implementation(libs.kotlinx.serialization.json)

  // Internal modules
  implementation(project(":measurement"))
  implementation(project(":utils"))

  // Android Room
  implementation(libs.bundles.room)
  ksp(libs.room.compiler)

  // Coil
  implementation(libs.bundles.coil)

  // Testing
  testImplementation(libs.junit)
  testImplementation(libs.bundles.kotest)
  androidTestImplementation(libs.androidx.test.ext)
  androidTestImplementation(libs.espresso.core)
  androidTestImplementation(platform(libs.compose.bom))
  androidTestImplementation(libs.compose.ui.test)
  debugImplementation(libs.compose.ui.tooling)
  debugImplementation(libs.compose.ui.test.manifest)
}
