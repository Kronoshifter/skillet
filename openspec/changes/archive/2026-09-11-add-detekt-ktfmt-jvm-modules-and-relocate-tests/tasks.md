## 1. Add detekt plugin + config to `utils/build.gradle.kts`

- [ ] 1.1 Add `alias(libs.plugins.detekt)` to the `plugins {}` block in `utils/build.gradle.kts`
- [ ] 1.2 Add the required imports:
  ```kotlin
  import dev.detekt.gradle.Detekt
  import dev.detekt.gradle.extensions.FailOnSeverity
  ```
- [ ] 1.3 Add a `detekt { ... }` configuration block:
  ```kotlin
  detekt {
      config.setFrom("$rootDir/detekt.yml")
      buildUponDefaultConfig = true
      allRules = false
      failOnSeverity = FailOnSeverity.Error
      baseline = file("$rootDir/utils/detekt-baseline.xml")
  }
  ```
- [ ] 1.4 Add a `tasks.withType<Detekt>().configureEach { ... }` block:
  ```kotlin
  tasks.withType<Detekt>().configureEach {
      exclude("**/generated/**")
      exclude("**/test/**")
  }
  ```
- **Acceptance:** `./gradlew :utils:detekt --dry-run` succeeds (config is parseable)

## 2. Add ktfmt plugin + config to `utils/build.gradle.kts`

- [ ] 2.1 Add `alias(libs.plugins.ktfmt)` to the `plugins {}` block in `utils/build.gradle.kts`
- [ ] 2.2 Add the import:
  ```kotlin
  import com.ncorti.ktfmt.gradle.TrailingCommaManagementStrategy
  ```
- [ ] 2.3 Add a `ktfmt { ... }` configuration block:
  ```kotlin
  ktfmt {
      googleStyle()
      removeUnusedImports = true
      maxWidth = 100
      trailingCommaManagementStrategy = TrailingCommaManagementStrategy.COMPLETE
  }
  ```
- **Acceptance:** `./gradlew :utils:ktfmtCheckMain --dry-run` succeeds (config is parseable)

## 3. Add detekt plugin + config to `measurement/build.gradle.kts`

- [ ] 3.1 Add `alias(libs.plugins.detekt)` to the `plugins {}` block in `measurement/build.gradle.kts`
- [ ] 3.2 Add the required imports:
  ```kotlin
  import dev.detekt.gradle.Detekt
  import dev.detekt.gradle.extensions.FailOnSeverity
  ```
- [ ] 3.3 Add a `detekt { ... }` configuration block:
  ```kotlin
  detekt {
      config.setFrom("$rootDir/detekt.yml")
      buildUponDefaultConfig = true
      allRules = false
      failOnSeverity = FailOnSeverity.Error
      baseline = file("$rootDir/measurement/detekt-baseline.xml")
  }
  ```
- [ ] 3.4 Add a `tasks.withType<Detekt>().configureEach { ... }` block:
  ```kotlin
  tasks.withType<Detekt>().configureEach {
      exclude("**/generated/**")
      exclude("**/test/**")
  }
  ```
- **Acceptance:** `./gradlew :measurement:detekt --dry-run` succeeds (config is parseable)

## 4. Add ktfmt plugin + config to `measurement/build.gradle.kts`

- [ ] 4.1 Add `alias(libs.plugins.ktfmt)` to the `plugins {}` block in `measurement/build.gradle.kts`
- [ ] 4.2 Add the import:
  ```kotlin
  import com.ncorti.ktfmt.gradle.TrailingCommaManagementStrategy
  ```
- [ ] 4.3 Add a `ktfmt { ... }` configuration block:
  ```kotlin
  ktfmt {
      googleStyle()
      removeUnusedImports = true
      maxWidth = 100
      trailingCommaManagementStrategy = TrailingCommaManagementStrategy.COMPLETE
  }
  ```
- **Acceptance:** `./gradlew :measurement:ktfmtCheckMain --dry-run` succeeds (config is parseable)

## 5. Create empty `utils/detekt-baseline.xml`

- [ ] 5.1 Create `utils/detekt-baseline.xml` with an empty `<Detekt>` root element:
  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <Detekt />
  ```
- **Acceptance:** File exists at `utils/detekt-baseline.xml` and is valid XML

## 6. Create empty `measurement/detekt-baseline.xml`

- [ ] 6.1 Create `measurement/detekt-baseline.xml` with an empty `<Detekt>` root element:
  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <Detekt />
  ```
- **Acceptance:** File exists at `measurement/detekt-baseline.xml` and is valid XML

## 7. Move `ExtensionTests.kt` to `utils/` and rename package

- [ ] 7.1 Move `measurement/src/test/kotlin/com/kronos/measurement/ExtensionTests.kt` to `utils/src/test/kotlin/com/kronos/utils/ExtensionTests.kt`
- [ ] 7.2 Change the package declaration from `package com.kronos.measurement` to `package com.kronos.utils`
- [ ] 7.3 Verify the test file compiles: `./gradlew :utils:compileTestKotlin`
- **Acceptance:** File exists at `utils/src/test/kotlin/com/kronos/utils/ExtensionTests.kt`, compiles, and original is deleted

## 8. Move `FractionTests.kt` to `utils/` and rename package

- [ ] 8.1 Move `measurement/src/test/kotlin/com/kronos/measurement/FractionTests.kt` to `utils/src/test/kotlin/com/kronos/utils/FractionTests.kt`
- [ ] 8.2 Change the package declaration from `package com.kronos.measurement` to `package com.kronos.utils`
- [ ] 8.3 Verify the test file compiles: `./gradlew :utils:compileTestKotlin`
- **Acceptance:** File exists at `utils/src/test/kotlin/com/kronos/utils/FractionTests.kt`, compiles, and original is deleted

## 9. Delete original test files from `measurement/`

- [ ] 9.1 Confirm `measurement/src/test/kotlin/com/kronos/measurement/ExtensionTests.kt` no longer exists
- [ ] 9.2 Confirm `measurement/src/test/kotlin/com/kronos/measurement/FractionTests.kt` no longer exists
- [ ] 9.3 Confirm `MeasurementTests.kt` still exists in `measurement/` (not deleted)
- **Acceptance:** Only `MeasurementTests.kt` remains in `measurement/src/test/kotlin/com/kronos/measurement/`

## 10. Run detekt on both modules and populate baselines

- [ ] 10.1 Run `./gradlew :utils:detekt` and capture the violation output
- [ ] 10.2 Run `./gradlew :measurement:detekt` and capture the violation output
- [ ] 10.3 Review violations — move acceptable/known issues into the respective module baselines
- [ ] 10.4 Re-run `./gradlew :utils:detekt` and `./gradlew :measurement:detekt` — both should pass (zero unhandled violations)
- **Acceptance:** Both `:utils:detekt` and `:measurement:detekt` complete with exit code 0 and zero unhandled violations

## 11. Run ktfmt on both modules

- [ ] 11.1 Run `./gradlew :utils:ktfmtFormatMain :utils:ktfmtFormatTest`
- [ ] 11.2 Run `./gradlew :measurement:ktfmtFormatMain :measurement:ktfmtFormatTest`
- [ ] 11.3 Verify zero violations: `./gradlew :utils:ktfmtCheckMain :utils:ktfmtCheckTest :measurement:ktfmtCheckMain :measurement:ktfmtCheckTest`
- **Acceptance:** All four ktfmt check tasks report zero formatting violations

## 12. Final verification

- [ ] 12.1 Run `./gradlew :utils:test` — all tests pass (including relocated ExtensionTests and FractionTests)
- [ ] 12.2 Run `./gradlew :measurement:test` — all tests pass (MeasurementTests.kt still runs)
- [ ] 12.3 Run `./gradlew :app:assembleDebug` — full build still works (no regressions)
- **Acceptance:** All three verification commands succeed with zero test failures
