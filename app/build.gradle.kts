plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  kotlin("plugin.serialization")
  id("kotlin-parcelize")
  id("com.google.devtools.ksp")
  id("org.jetbrains.kotlin.plugin.compose")
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
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

dependencies {
  val nav_version = "2.8.0-beta05"
  val koin_version = "4.0.0"
  val room_version = "2.6.1"
  val coil_version = "3.1.0"

  implementation("androidx.core:core-ktx:1.15.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
  implementation("androidx.activity:activity-compose:1.9.0")
  implementation(platform("androidx.compose:compose-bom:2024.06.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3:1.3.1")
  implementation("androidx.compose.material:material-icons-extended:1.7.8")
  implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
  implementation("androidx.navigation:navigation-compose:$nav_version")

  // Koin
  implementation(platform("io.insert-koin:koin-bom:$koin_version"))
  implementation("io.insert-koin:koin-androidx-compose")
  implementation("io.insert-koin:koin-androidx-compose-navigation")

  // Utils
  implementation("com.leinardi.android:speed-dial.compose:2.0.0-alpha01") //Speed Dial Composable
  implementation("org.antlr:antlr4:4.13.0")
  implementation("sh.calvin.reorderable:reorderable:2.3.1")
  implementation("it.skrape:skrapeit:1.2.2")

  // Android Room
  implementation("androidx.room:room-runtime:$room_version")
  ksp("androidx.room:room-compiler:$room_version")
  implementation("androidx.room:room-ktx:$room_version")

  // Coil
  implementation("io.coil-kt.coil3:coil-compose-core:$coil_version")
  implementation("io.coil-kt.coil3:coil-network-okhttp:$coil_version")

  testImplementation("junit:junit:4.13.2")
  testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
  testImplementation("io.kotest:kotest-assertions-core:5.9.1")
  testImplementation("io.kotest:kotest-property:5.9.1")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  implementation(kotlin("reflect"))
  implementation("com.michael-bull.kotlin-result:kotlin-result:2.0.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}