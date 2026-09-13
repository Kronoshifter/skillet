# Design: Add Detekt & ktfmt to JVM Modules and Relocate Test Files

## Context

The Skillet app has three Gradle modules:

```
Root build.gradle.kts (plugins declared with apply false)
├── :app          — Android library, detekt + ktfmt configured
├── :utils        — Pure Kotlin JVM (3 source files), NO detekt, NO ktfmt
└── :measurement  — Pure Kotlin JVM (~10 source files), depends on :utils, NO detekt, NO ktfmt
```

The root `build.gradle.kts` registers both plugins with `apply false`:
```kotlin
alias(libs.plugins.detekt) apply false
alias(libs.plugins.ktfmt) apply false
```

This means the plugins are available to subprojects but **not applied** to any. Each module must explicitly apply them.

The `app` module already has both plugins fully configured with:
- `detekt { config.setFrom("$rootDir/detekt.yml"), buildUponDefaultConfig = true, allRules = false, failOnSeverity = Error, baseline = file("$rootDir/detekt-baseline.xml") }`
- `ktfmt { googleStyle(), removeUnusedImports = true, maxWidth = 100, COMPLETE trailing comma }`
- Task exclusions in `tasks.withType<Detekt>()` block

## Decisions

### D1: Module-level detekt/ktfmt config blocks (Option A)

**Decision:** Each JVM module gets its own `detekt { ... }` and `ktfmt { ... }` blocks referencing shared root files.

**Rationale:** The modules are tiny (3 files in `:utils`, ~10 in `:measurement`). A convention plugin or `buildSrc/` setup would be over-engineering. Module-level blocks provide independent task scopes (`:utils:detekt`, `:measurement:detekt`) and independent baselines without coupling.

**Configuration for `:utils` and `:measurement`:**
```kotlin
import com.ncorti.ktfmt.gradle.TrailingCommaManagementStrategy
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.FailOnSeverity

detekt {
    config.setFrom("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
    allRules = false
    failOnSeverity = FailOnSeverity.Error
    baseline = file("$rootDir/utils/detekt-baseline.xml")  // per-module path
}

ktfmt {
    googleStyle()
    removeUnusedImports = true
    maxWidth = 100
    trailingCommaManagementStrategy = TrailingCommaManagementStrategy.COMPLETE
}

tasks.withType<Detekt>().configureEach {
    exclude("**/generated/**")
    exclude("**/test/**")
}
```

**Note on baseline paths:** For `:utils`, the baseline is `$rootDir/utils/detekt-baseline.xml`. For `:measurement`, it's `$rootDir/measurement/detekt-baseline.xml`. These are per-module baselines at the module root, not at the project root.

### D2: Per-module detekt baselines

**Decision:** Create `utils/detekt-baseline.xml` and `measurement/detekt-baseline.xml` at the module root.

**Rationale:** The existing `detekt-baseline.xml` at project root has 129 entries scoped to `app/`. Mixing in JVM module entries would pollute the app baseline. Per-module baselines keep each module's violations isolated.

**Strategy:** Start with empty baselines. Run `./gradlew :utils:detekt :measurement:detekt` first. Review the violations. Add known-acceptable violations to the baseline. This ensures the baseline is accurate and doesn't suppress legitimate issues.

**Estimated violations:**
- `:utils` (~5-10): `MagicNumber`, `TooManyFunctions` on `Fraction.kt`
- `:measurement` (~10-20): `MagicNumber`, `TooManyFunctions`, `WildcardImport`

### D3: Replicate `failOnSeverity = Error`

**Decision:** Both modules use `failOnSeverity = FailOnSeverity.Error` to match the `:app` module.

**Rationale:** Consistency across modules. Prevents silent violations. The baseline will absorb known issues so the build doesn't break.

### D4: Run ktfmt as part of this change

**Decision:** Run `ktfmtFormatMain` and `ktfmtFormatTest` on both modules during implementation.

**Rationale:** ktfmt is idempotent and cosmetic. Running it now ensures all code conforms to the project standard from day one. No baseline needed — ktfmt enforces formatting on every run.

### D5: Test file relocation

**Decision:** Move `ExtensionTests.kt` and `FractionTests.kt` from `measurement/src/test/kotlin/com/kronos/measurement/` to `utils/src/test/kotlin/com/kronos/utils/`. Change package from `com.kronos.measurement` to `com.kronos.utils`.

**Rationale:** Both files test `:utils` production code (`gcd`, `roundToEighth`, `Fraction`, `fraction` extension). Tests should live alongside the code they test.

**No dependency issues:**
- `:utils` already has Kotest dependencies (`testImplementation(libs.kotest.runner)`, etc.)
- `:utils` already has `useJUnitPlatform()` configured
- The test imports (`com.kronos.utils.*`) are satisfied by `:utils`'s own source code
- `:measurement` no longer needs these tests (they test `:utils`, not `:measurement`)

**`MeasurementTests.kt` stays in `:measurement`** — it tests `:measurement`'s own code.

### D6: Plugin import statements

**Decision:** Add the same import statements that `:app` uses:
```kotlin
import com.ncorti.ktfmt.gradle.TrailingCommaManagementStrategy
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.FailOnSeverity
```

**Rationale:** Replicate the `:app` module's pattern exactly. These are the only plugin-specific types needed beyond the Gradle base API.

## Files Modified

| File | Change |
|------|--------|
| `utils/build.gradle.kts` | Add `alias(libs.plugins.detekt)` and `alias(libs.plugins.ktfmt)` to plugins block; add `detekt { ... }` and `ktfmt { ... }` config blocks; add `tasks.withType<Detekt>()` exclusions |
| `measurement/build.gradle.kts` | Same as `utils/build.gradle.kts` |
| `utils/detekt-baseline.xml` | New file (per-module baseline, populated after first detekt run) |
| `measurement/detekt-baseline.xml` | New file (per-module baseline, populated after first detekt run) |
| `utils/src/test/kotlin/com/kronos/utils/ExtensionTests.kt` | Moved from measurement/; package renamed to `com.kronos.utils` |
| `utils/src/test/kotlin/com/kronos/utils/FractionTests.kt` | Moved from measurement/; package renamed to `com.kronos.utils` |
| `measurement/src/test/kotlin/com/kronos/measurement/ExtensionTests.kt` | Deleted (moved) |
| `measurement/src/test/kotlin/com/kronos/measurement/FractionTests.kt` | Deleted (moved) |

## Verification Plan

After implementation:
1. `./gradlew :utils:detekt` — runs with no unhandled violations (baseline absorbs known ones)
2. `./gradlew :measurement:detekt` — same
3. `./gradlew :utils:ktfmtCheckMain :utils:ktfmtCheckTest` — zero formatting violations
4. `./gradlew :measurement:ktfmtCheckMain :measurement:ktfmtCheckTest` — zero formatting violations
5. `./gradlew :utils:test` — all tests pass (including relocated tests)
6. `./gradlew :measurement:test` — all tests pass (MeasurementTests.kt still runs)
7. `./gradlew :app:assembleDebug` — full build still works (no regressions)
