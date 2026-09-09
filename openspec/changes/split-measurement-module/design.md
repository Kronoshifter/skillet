# Design: Split Measurement System into Separate Module

## Context

The Skillet app is a single-module Android project (`app/`) using Gradle 9.3.1, Kotlin 2.4 (language version 2.4), and AGP 9.3.1. The measurement system lives in `app/src/main/java/com/kronos/skilletapp/model/measurement/` and consists of 3 files (777 lines total) that model cooking measurements, unit hierarchies, and conversion DSLs.

The measurement code depends on:
- `kotlinx.serialization.Serializable` (for Room JSON serialization)
- `kotlin-result` (`Result<T, E>`) for safe error propagation
- `kotlin-reflect` (`KClass.nestedClasses`) for type checking
- Utility functions from `com.kronos.skilletapp.utils`: `Fraction`, `NumberUtils`, `MiscUtils`

The `IngredientType` dependency in `MeasurementUnit.kt` (lines 341-362, `allowedIngredientTypes` map) has **zero callers** in the entire codebase. It is dead code from a previous implementation plan that never landed. It must be removed.

## Goals / Non-Goals

**Goals:**
- Extract measurement code into a pure Kotlin JVM library module (`measurement/`)
- Extract utility code (`Fraction`, `NumberUtils`, `MiscUtils`) into a shared Kotlin JVM library module (`utils/`)
- Both `:app` and `:measurement` depend on `:utils`
- Update measurement-related imports to use new package names (`com.kronos.measurement.model.*`)
- Update utils-related imports to use new package names (`com.kronos.utils.*`)
- Remove the dead `IngredientType` dependency from `MeasurementUnit.kt`
- Ensure the app module builds successfully after the split
- Ensure measurement module tests pass in pure JVM context

**Non-Goals:**
- Refactoring the measurement system itself (conversions, base units, serialization)
- Adding Android framework dependencies to the measurement or utils modules
- Changing `@Serializable` annotations or class hierarchy
- Modifying the Room schema or database layer beyond import updates
- Adding new measurement units or converters
- CI/CD pipeline changes

## Decisions

### D1: Pure Kotlin JVM module (not Android library)

**Decision:** Both the `measurement/` and `utils/` modules use `kotlin("jvm")` plugin, not `com.android.library`.

**Rationale:** Both modules have zero Android framework dependencies. They only use:
- `kotlinx.serialization` (JVM)
- `kotlin-result` (JVM)
- `kotlin-reflect` (JVM)
- Standard library types (`Float`, `Int`, `List`, `String`, etc.)

Pure JVM modules are simpler, faster to compile, and allow Kotest tests to run without Android test infrastructure. This is the correct architectural choice.

**Alternatives considered:**
- Android library module (`com.android.library`) — unnecessary overhead; no Android APIs are used
- Shared Android module — overkill for pure-utility libraries

### D2: Package naming — `com.kronos.measurement.model` and `com.kronos.utils`

**Decision:** Two new root packages:
- `com.kronos.measurement.model` — `Measurement`, `MeasurementUnit`, `MeasurementConverter`
- `com.kronos.utils` — `Fraction`, `NumberUtils`, `MiscUtils`

**Rationale:**
- Both modules are now standalone libraries that could theoretically be used by other projects
- `com.kronos.measurement` is a clean, self-describing package name
- `com.kronos.utils` is a clean, self-describing package name for shared utilities
- The `.model` subpackage in measurement mirrors the existing convention (the app module's `model/` package)
- The old paths `com.kronos.skilletapp.model.measurement` and `com.kronos.skilletapp.utils` implied they were part of Skillet's app module, which is no longer accurate

**Trade-off:** This is a breaking change for all ~35 consumer files — every measurement import AND every utils import must be updated. However, the user has accepted breaking changes as they plan further measurement refactoring anyway.

### D3: Three-module architecture with shared utils

**Decision:** Create **three** modules, not two. The `:utils` module is a shared dependency that both `:app` and `:measurement` depend on.

**Rationale:**
- The measurement module needs `Fraction`, `NumberUtils`, and `MiscUtils` (from the old `com.kronos.skilletapp.utils`)
- The app module also needs these utilities (~35 consumer files)
- If `:app` depends on `:measurement`, and `:measurement` depends on `:app` for utils, Gradle rejects this as a circular dependency
- The solution: extract utils into a third module `:utils` that both `:app` and `:measurement` depend on independently
- This creates a clean diamond dependency graph with no cycles

**Dependency graph:**
```
      :utils (shared JVM library)
     /       │       \
    ▼        ▼        ▼
 :measurement  :app  (parser, UI, navigation, viewmodels)
```

**What each module depends on:**
- `:utils` — no project dependencies. Depends on `kotlinx-serialization` (JSON helpers), `kotlin-reflect` (type checking)
- `:measurement` — depends on `:utils` for `Fraction`, `NumberUtils`, `MiscUtils`. Also depends on `kotlinx-serialization`, `kotlin-result`, `kotlin-reflect`
- `:app` — depends on `:measurement` for measurement types, `:utils` for utilities. Also depends on `kotlinx-serialization`, `kotlin-result`, `kotlin-reflect` (app module uses these independently too)

**Package changes:**
- `com.kronos.skilletapp.utils` → `com.kronos.utils` (moved to `:utils` module)
- `com.kronos.skilletapp.model.measurement` → `com.kronos.measurement.model` (moved to `:measurement` module)

**Consumer impact:**
- ~35 consumer files need BOTH measurement imports AND utils imports updated
- This is a one-time mechanical change — find/replace all old package references

### D4: Remove `IngredientType` dependency (dead code)

**Decision:** Delete the `allowedIngredientTypes` map (lines 341-362) in `MeasurementUnit.kt` and remove the `import com.kronos.skilletapp.model.IngredientType.Dry` / `Wet` imports.

**Rationale:**
- The `allowedIngredientTypes` map has **zero callers** in the entire codebase (verified by grep across all source files)
- It is leftover from a previous implementation plan that never landed
- Removing it eliminates the circular dependency: `MeasurementUnit.kt` → `IngredientType` → `Measurement`
- The `Wet` and `Dry` enum values from `IngredientType` are no longer needed by the measurement module

**Verification:** The speculator confirmed zero callers exist. The Architect should verify this with `rg "allowedIngredientTypes" --type kotlin` before implementation.

### D5: Kotest tests run in pure JVM context

**Decision:** All tests (`MeasurementTests.kt`, `FractionTests.kt`, `ExtensionTests.kt`) run as pure JVM tests using Kotest's `FunSpec` with JUnit platform. No Android test infrastructure is needed.

**Rationale:**
- `MeasurementTests.kt` (907 lines) tests scaling, conversions, normalization, fraction rounding — all pure Kotlin operations
- `FractionTests.kt` tests fraction arithmetic — pure Kotlin
- `ExtensionTests.kt` tests `gcd`, `roundToEighth` — pure Kotlin
- Kotest's `FunSpec` with JUnit platform runs fine in pure JVM contexts
- No `AndroidTestCase`, `InstrumentationTest`, or `androidx.test` dependencies needed

**Gradle test config:**
```kotlin
dependencies {
  testImplementation(libs.kotest.runner)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.property)
}
```

### D6: Gradle plugin setup

**Decision:** The root `build.gradle.kts` registers the Kotlin JVM plugin with `apply false`. Both `utils/` and `measurement/` modules apply `kotlin("jvm")`.

**Root `build.gradle.kts` addition:**
```kotlin
plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  // ... existing plugins
}
```

**`utils/build.gradle.kts`:**
```kotlin
plugins {
  alias(libs.plugins.kotlin.serialization)
  kotlin("jvm")
}

dependencies {
  api(libs.kotlinx.serialization.json)
  implementation(libs.kotlin.reflect)
  testImplementation(libs.kotest.runner)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.property)
}
```

**`measurement/build.gradle.kts`:**
```kotlin
plugins {
  alias(libs.plugins.kotlin.serialization)
  kotlin("jvm")
}

dependencies {
  api(libs.kotlinx.serialization.json)
  api(libs.kotlin.result)
  implementation(libs.kotlin.reflect)
  implementation(project(":utils"))
  testImplementation(libs.kotest.runner)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.property)
}
```

**`app/build.gradle.kts` additions:**
```kotlin
dependencies {
  implementation(project(":measurement"))
  implementation(project(":utils"))
  // ... existing dependencies (kotlin-reflect, kotlin-result, kotlinx-serialization-json all stay)
}
```

### D7: Room serialization unaffected

**Decision:** No changes to `@Serializable` annotations, `@JsonClassDiscriminator`, or Room type converters.

**Rationale:**
- Room stores `Ingredient` as JSON via `kotlinx.serialization`
- `Measurement` and `MeasurementUnit` are `@Serializable` — serialization works identically regardless of module boundary
- The `@JsonClassDiscriminator("measurement_type")` annotation on `MeasurementUnit` works identically in the new module
- Existing database data will deserialize correctly — no migration needed

### D8: `kotlin-reflect` dependency scope

**Decision:** `kotlin-reflect` remains a dependency of **all three** modules.

**Rationale:**
- `kotlin-reflect` is used by `haveSameTypes()` in `MiscUtils.kt` (now in `:utils` module)
- `kotlin-reflect` is used by `:measurement` module for type checking in measurement code
- **Additionally**, the app module uses `kotlin-reflect` directly in:
  - `navigation/SkilletNavigation.kt` — `KClass`, `full.memberProperties`
  - `navigation/SkilletNavGraph.kt` — `typeOf`
  - `ui/viewmodel/RecipeListViewModel.kt` — `typeOf`
- Therefore, `implementation(libs.kotlin.reflect)` stays in `app/build.gradle.kts`

### D9: `kotlin-result` and `kotlinx-serialization` stay in app module

**Decision:** The app module keeps `implementation(libs.kotlin.result)` and `implementation(libs.kotlinx.serialization.json)`.

**Rationale:**
- `kotlin-result` is used extensively in app module code: `ResultUtils.kt`, `Converters.kt`, `RecipeScraper.kt`, `AddEditRecipeViewModel.kt`, `AddEditRecipeScreen.kt`
- `kotlinx-serialization-json` is used extensively in app module code: `NavigationUtils.kt`, `SkilletNavigation.kt`, `Recipe.kt`, `Ingredient.kt`, `Serializers.kt`, `RecipeScraper.kt`, `Converters.kt`
- These are NOT removed from the app module — they are independent dependencies that happen to also be needed by the utils and measurement modules

### D10: Utils module depends only on JVM libraries

**Decision:** The `:utils` module has no project dependencies. It depends only on `kotlinx-serialization` (for JSON helpers in `MiscUtils.kt`) and `kotlin-reflect` (for type checking in `MiscUtils.kt`).

**Rationale:**
- `Fraction.kt` — pure Kotlin, no external deps
- `NumberUtils.kt` — pure Kotlin, no external deps
- `MiscUtils.kt` — uses `kotlinx.serialization` (toJson/fromJson) and `kotlin-reflect` (haveSameTypes)
- The `:utils` module is a true shared library — any other module can depend on it without pulling in app-specific code

### D11: Measurement module imports from utils via `:utils` project dependency

**Decision:** The `:measurement` module has a compile-time dependency on the `:utils` module to access `com.kronos.utils`.

**Rationale:**
- `Measurement.kt` needs `Fraction`, `NumberUtils` extensions (`fraction`, `nearestEighth`), and `MiscUtils` functions (`haveSameTypes`)
- `MeasurementConverter.kt` needs `MiscUtils` functions (`mutateUnless`)
- These utilities are moved to the `:utils` module and imported as `com.kronos.utils.*`
- The dependency is clean and acyclic: `app` depends on both `:measurement` and `:utils`; `:measurement` depends on `:utils`

**Measurement module imports from utils:**
- `import com.kronos.utils.Fraction` — used by `Measurement.kt`, `FractionTests.kt`
- `import com.kronos.utils.fraction`, `nearestEighth`, `nearestThird`, `roundToNth` — NumberUtils extensions
- `import com.kronos.utils.gcd`, `lcm` — NumberUtils functions
- `import com.kronos.utils.mutateUnless`, `haveSameTypes`, `haveSameType` — MiscUtils functions
- `import com.kronos.utils.toJson`, `fromJson` — MiscUtils JSON helpers (used by tests)

**Consumer imports (app module):**
- Measurement imports: `com.kronos.skilletapp.model.measurement.*` → `com.kronos.measurement.model.*`
- Utils imports: `com.kronos.skilletapp.utils.*` → `com.kronos.utils.*`

**Gradle config:**
```kotlin
// measurement/build.gradle.kts
dependencies {
  implementation(project(":utils"))
  api(libs.kotlinx.serialization.json)
  api(libs.kotlin.result)
  implementation(libs.kotlin.reflect)
  testImplementation(libs.kotest.runner)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.property)
}
```

## Risks / Trade-offs

| Risk | Likelihood | Mitigation |
|------|-----------|------------|
| Import updates break build | Medium | Update all imports atomically. Verify with `./gradlew :app:compileDebugKotlin` |
| `kotlin-reflect` removal breaks app module | Low | Verify no other app code uses `kotlin-reflect` before removing from app module |
| Kotest tests fail in pure JVM context | Low | Kotest's `FunSpec` + JUnit platform works in pure JVM; verify with `./gradlew :measurement:test` |
| `IngredientType` removal has hidden callers | Low | Speculator already verified zero callers; Architect should re-verify with `rg "allowedIngredientTypes"` |
| Serialization deserialization mismatch | Very Low | No changes to serialization annotations; Room will deserialize identically |
| Three-module setup adds complexity | Low | Diamond dependency is clean and well-understood; no circular deps |

## Migration Plan

1. **Create `utils/` module structure** — `build.gradle.kts`, source directories
2. **Create `measurement/` module structure** — `build.gradle.kts`, source directories
3. **Update root `build.gradle.kts`** — register Kotlin JVM plugin with `apply false`
4. **Update `settings.gradle.kts`** — add `include(":utils", ":measurement")`
5. **Move utils files** — `Fraction.kt`, `NumberUtils.kt`, `MiscUtils.kt` to `utils/src/main/kotlin/com/kronos/utils/`
6. **Update utils package declarations** — `com.kronos.skilletapp.utils` → `com.kronos.utils`
7. **Move measurement files** — `Measurement.kt`, `MeasurementUnit.kt`, `MeasurementConverter.kt` to `measurement/src/main/kotlin/com/kronos/measurement/model/`
8. **Update measurement package declarations** — `com.kronos.skilletapp.model.measurement` → `com.kronos.measurement.model`
9. **Update measurement internal imports** — `com.kronos.skilletapp.utils.*` → `com.kronos.utils.*`
10. **Remove `IngredientType` dependency** — delete `allowedIngredientTypes` map and unused imports from `MeasurementUnit.kt`
11. **Move test files** — `MeasurementTests.kt`, `FractionTests.kt`, `ExtensionTests.kt` to `measurement/src/test/kotlin/com/kronos/measurement/`
12. **Update test package declarations and imports** — measurement imports from `com.kronos.measurement.model.*`, utils from `com.kronos.utils.*`
13. **Update `app/build.gradle.kts`** — add `implementation(project(":measurement"))` and `implementation(project(":utils"))`
14. **Update consumer imports across app module** — update BOTH measurement imports (`com.kronos.skilletapp.model.measurement.*` → `com.kronos.measurement.model.*`) AND utils imports (`com.kronos.skilletapp.utils.*` → `com.kronos.utils.*`)
15. **Delete moved files from app module** — remove old measurement and utils directories
16. **Verify build** — `./gradlew :utils:compileKotlin`, `./gradlew :measurement:compileKotlin`, `./gradlew :app:assembleDebug`, `./gradlew :measurement:test`

## Open Questions

_None._ All technical decisions are resolved. The speculator has completed the analysis; this plan focuses on task decomposition.
