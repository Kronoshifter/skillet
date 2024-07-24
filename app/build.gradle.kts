plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  kotlin("plugin.serialization") version "2.0.0"
}

android {
  namespace = "com.kronos.skilletapp"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.kronos.skilletapp"
    minSdk = 27
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
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.1"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  testOptions {
    unitTests.all {
      it.useJUnitPlatform()
    }
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

dependencies {
  val nav_version = "2.8.0-beta05"

  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
  implementation("androidx.activity:activity-compose:1.9.0")
  implementation(platform("androidx.compose:compose-bom:2024.06.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3-android:1.2.1")
  implementation("androidx.compose.material:material-icons-extended:1.6.8")
  implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
  implementation("androidx.navigation:navigation-compose:$nav_version")
  implementation("io.insert-koin:koin-androidx-compose:3.4.0")
  implementation("com.leinardi.android:speed-dial.compose:2.0.0-alpha01") //Speed Dial Composable
  implementation("org.antlr:antlr4:4.13.0")

  testImplementation("junit:junit:4.13.2")
  testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
  testImplementation("io.kotest:kotest-assertions-core:5.9.1")
  testImplementation("io.kotest:kotest-property:5.9.1")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  implementation(kotlin("reflect"))
  implementation("com.michael-bull.kotlin-result:kotlin-result:2.0.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}