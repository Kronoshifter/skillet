## 1. Add plugin versions to Version Catalog ✅ DONE

- [x] 1.1 Add `detekt = "2.0.0-alpha.6"` and `ktfmt = "0.27.0"` to the `[versions]` section of `gradle/libs.versions.toml`
- [x] 1.2 Add plugin entries to the `[plugins]` section of `gradle/libs.versions.toml`:
  - `detekt = { id = "dev.detekt", version.ref = "detekt" }`
  - `ktfmt = { id = "com.ncorti.ktfmt.gradle", version.ref = "ktfmt" }`
- [x] 1.3 Verify the TOML file is valid by running `./gradlew help` and confirming no parsing errors

## 2. Register detekt plugin in root build file ✅ DONE

- [x] 2.1 Add `alias(libs.plugins.detekt) apply false` to the `plugins {}` block in root `build.gradle.kts`
- [x] 2.2 Verify the root build file still compiles by running `./gradlew tasks --all | grep detekt` and confirming `detekt` task appears

## 3. Apply plugins and configure in app module ✅ DONE

- [x] 3.1 Add `alias(libs.plugins.detekt)` and `alias(libs.plugins.ktfmt)` to the `plugins {}` block in `app/build.gradle.kts`
- [x] 3.2 Add a `detekt` configuration block in `app/build.gradle.kts`:
  ```kotlin
  detekt {
      config.setFrom("$rootDir/detekt.yml")
      buildUponDefaultConfig = false
      allRules = false
      source.setFrom("src/main/kotlin", "src/main/java")
      failOnSeverity = FailOnSeverity.Error
  }
  ```
- [x] 3.3 Add a `ktfmt` configuration block in `app/build.gradle.kts`:
  ```kotlin
  ktfmt {
      googleStyle()
      removeUnusedImports = true
      maxWidth = 100
      trailingCommaManagementStrategy = TrailingCommaManagementStrategy.COMPLETE
  }
  ```
- [x] 3.4 Move exclusions into `tasks.withType<Detekt>()` block (detekt v2 location):
  ```kotlin
  tasks.withType<Detekt>().configureEach {
      exclude("**/generated/**")
      exclude("**/test/**")
      exclude("**/androidTest/**")
  }
  ```
- [x] 3.5 Verify Gradle sync succeeds by running `./gradlew help` with no errors

## 3.6. Fix incorrect import in app/build.gradle.kts

- [ ] 3.6 Remove the incorrect import `import jdk.jfr.internal.JVM.exclude` from `app/build.gradle.kts`
  - **Why this is wrong:** `exclude()` on `Detekt` tasks is a standard Gradle method (`TaskInputs.exclude()` / `TaskOutputFileFilterCompat`). No JDK internal import is needed or appropriate.
  - **Correct imports to keep:** `dev.detekt.gradle.Detekt`, `dev.detekt.gradle.extensions.FailOnSeverity`, `com.ncorti.ktfmt.gradle.TrailingCommaManagementStrategy`

## 4. Create detekt.yml configuration (v2 YAML format)

- [ ] 4.1 Create `detekt.yml` at the project root following the **detekt v2 YAML format**:
  - **No** `android: true` — Android profile is handled by the Gradle plugin
  - **No** `autoCorrect` or `failFast` — removed in v2
  - **No** `excludes` at top level — exclusions go in Gradle `tasks.withType<Detekt>()` block
  - `config.validation: true`
  - `config.warningsAsErrors: false`
  - `config.checkExhaustiveness: false`
  - `config.excludes: []` (empty — Gradle handles source exclusions)
  - **`active` values are booleans** (`true`/`false`), NOT strings (`"true"`/`"false"`)
- [ ] 4.2 Enable these specific rules (set `active: true`):
  - **Naming:**
    - `FunctionNaming` with `functionPattern: '[a-z][a-zA-Z0-9]*'`
    - `ClassNaming`
    - `ObjectPropertyName`
    - `VariableNaming`
  - **Potential bugs:**
    - `UnusedPrivateFunction` (v2 rename — was `UnusedPrivateMember`)
    - `UnusedPrivateProperty` (v2 rename — was `UnusedPrivateMember`)
    - `DoubleMutabilityForCollection`
    - `EqualsAlwaysReturnsTrueOrFalse`
  - **Comments:**
    - `ForbiddenComment` with `comments: [{ reason: "Forbidden FIXME", value: "FIXME:" }, { reason: "Forbidden STOPSHIP", value: "STOPSHIP:" }, { reason: "Forbidden TODO", value: "TODO:" }]`
  - **Exceptions:**
    - `ExceptionRaisedInUnexpectedLocation` with `methodNames: [equals, finalize, hashCode, toString]`
  - **Style:**
    - `UnusedImport` (v2 rename — was `UnusedImports`)
- [ ] 4.3 Explicitly disable these rules (set `active: false`):
  - `FunctionNameMaxLength` (v2 rename — was `FunctionMaxLength`; Compose composables are long)
  - `TooManyFunctions` (ViewModels have multiple states)
  - `DataClassNaming` (not applicable to all data classes)
- [ ] 4.4 Verify the YAML is valid by running `./gradlew detekt` and confirming it runs without YAML parse errors

## 5. Create .editorconfig

- [ ] 5.1 Create `.editorconfig` at the project root with:
  ```
  root = true

  [*]
  charset = utf-8
  end_of_line = lf
  insert_final_newline = true
  trim_trailing_whitespace = true

  [*.kt]
  indent_size = 4
  continuation_indent_size = 4

  [*.gradle.kts]
  indent_size = 4
  continuation_indent_size = 4

  [*.xml]
  indent_size = 4

  [*.{yml,yaml}]
  indent_size = 2

  [*.properties]
  indent_size = 2
  ```
- [ ] 5.2 Verify `.editorconfig` is recognized by running `./gradlew tasks` and confirming no editorconfig-related errors

## 6. Verify Gradle tasks are available

- [ ] 6.1 Run `./gradlew tasks --all | grep detekt` and verify these tasks appear:
  - `detekt` — runs static analysis
- [ ] 6.2 Run `./gradlew tasks --all | grep ktfmt` and verify these tasks appear:
  - `ktfmtFormatMain` — formats main source code
  - `ktfmtCheckMain` — checks main source code formatting
- [ ] 6.3 Run `./gradlew detekt` and capture the output (expected: some violations, build succeeds with `--warning-mode all` or similar)
- [ ] 6.4 Run `./gradlew ktfmtCheckMain` and capture the output (expected: formatting violations on existing code — this is expected before the one-time format pass)

## 7. Run one-time ktfmt formatting pass

- [ ] 7.1 Run `./gradlew ktfmtFormatMain` to format all main source Kotlin files
- [ ] 7.2 Run `git status` to review the list of files modified by ktfmt
- [ ] 7.3 Run `git diff --stat` to see the scope of formatting changes
- [ ] 7.4 Stage and commit the formatting changes with message: `style: format Kotlin code with ktfmt (Google style, 100-char width)`
- [ ] 7.5 Verify the commit includes only formatting changes (no logic changes) by reviewing the diff

## 8. Final verification

- [ ] 8.1 Run `./gradlew detekt` and capture the final violation count (this is the project baseline)
- [ ] 8.2 Run `./gradlew ktfmtCheckMain` and confirm zero formatting violations (ktfmt pass succeeded)
- [ ] 8.3 Run `./gradlew :app:assembleDebug` and confirm the build still compiles successfully
- [ ] 8.4 Run `./gradlew :app:testDebugUnitTest` and confirm existing unit tests still pass
