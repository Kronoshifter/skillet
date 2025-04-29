plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.androidx.room)

}

android {
  namespace = "com.kronos.skilletapp"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.kronos.skilletapp"
    minSdk = 30
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  buildFeatures {
    compose = true
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
      excludes += "META-INF/DEPENDENCIES"

      pickFirsts += "mozilla/public-suffix-list.txt"
    }

  }

  testOptions {
    unitTests.all {
      it.useJUnitPlatform()
    }
  }

  room {
    schemaDirectory("$projectDir/schemas")
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
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
  implementation(libs.speed.dial.compose) //Speed Dial Composable
  implementation(libs.antlr)
  implementation(libs.reorderable)
  implementation(libs.skrapeit)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlin.result)
  implementation(libs.kotlinx.serialization.json)

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

