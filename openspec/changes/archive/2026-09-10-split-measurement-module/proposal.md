# Proposal: Split Measurement System into Separate Module

## Why

The measurement system (`Measurement.kt`, `MeasurementUnit.kt`, `MeasurementConverter.kt` — 777 lines) is tightly coupled within the single `app/` module alongside Android UI code, Room persistence, and parsing logic. The user plans further refactoring of the measurement system (conversions, base units, serialization) and would benefit from a standalone module that:

1. **Isolates measurement logic** — cleaner boundaries, easier to test in pure JVM context
2. **Enables independent iteration** — measurement refactoring won't require rebuilding the Android app
3. **Follows the single-responsibility principle** — measurement types, conversions, and utilities are a cohesive domain that deserves its own module

This is exploratory groundwork for broader measurement system refactoring the user plans to do afterward. The split itself does NOT change any measurement behavior, serialization, or conversion logic.

## What Changes

**Three new modules are created:**

1. **`:utils`** — A pure Kotlin JVM shared library containing `Fraction.kt`, `NumberUtils.kt`, and `MiscUtils.kt`. Package: `com.kronos.utils`. Both `:app` and `:measurement` depend on this module.
2. **`:measurement`** — A pure Kotlin JVM library containing `Measurement.kt`, `MeasurementUnit.kt`, `MeasurementConverter.kt`, and their tests. Package: `com.kronos.measurement.model`. Depends on `:utils` for `Fraction`, `NumberUtils`, and `MiscUtils`.
3. **`:app`** — The existing Android module, updated to depend on both `:measurement` and `:utils`.

- Move `com.kronos.skilletapp.model.measurement` → `com.kronos.measurement.model`
- Move `com.kronos.skilletapp.utils` → `com.kronos.utils` (in the new `:utils` module)
- Delete `IngredientType` dependency from `MeasurementUnit.kt` (zero callers — dead code)
- Update all ~35 consumer files to use new package names for both measurement and utils
- Update `Ingredient.kt` import from `Measurement` to new package
- Delete old measurement and utils directories from app module

## Capabilities

### New Capabilities

- **utils-library**: A pure Kotlin JVM shared library providing `Fraction` (fraction arithmetic and display), `NumberUtils` (rounding, GCD/LCM, fraction conversion), and `MiscUtils` (mutate/apply helpers, JSON serialization, type checking). Used by both the `:measurement` library and the `:app` module. Depends on `kotlinx-serialization` (for JSON helpers) and `kotlin-reflect` (for type checking). Testable in JVM context with Kotest.
- **measurement-library**: A pure Kotlin library module providing `Measurement`, `MeasurementUnit` (sealed hierarchy of 15 units), `MeasurementConverter` (DSL-based conversion) — testable in JVM context without Android dependencies. Depends on `:utils` for `Fraction`, `NumberUtils`, and `MiscUtils`.

### Modified Capabilities

_None._ The measurement system's public API, serialization format, and behavioral contracts remain unchanged. Only the module boundary and package names differ.

## What's Out of Scope

- Refactoring the measurement system itself (conversions, base units, serialization)
- Adding Android framework dependencies to the measurement or utils modules
- Changing `@Serializable` annotations or class hierarchy
- Modifying the Room schema or database layer beyond import updates
- Adding new measurement units or converters
- CI/CD pipeline changes

## Impact

**Files created (6 new files):**
- `utils/build.gradle.kts` — Kotlin JVM module config with kotlinx-serialization, kotlin-reflect, kotest dependencies
- `utils/src/main/kotlin/com/kronos/utils/Fraction.kt`
- `utils/src/main/kotlin/com/kronos/utils/NumberUtils.kt`
- `utils/src/main/kotlin/com/kronos/utils/MiscUtils.kt`
- `measurement/build.gradle.kts` — Kotlin JVM module config with kotlinx-serialization, kotlin-result, kotlin-reflect, kotest deps, depends on `:utils`
- `measurement/src/main/kotlin/com/kronos/measurement/model/Measurement.kt`
- `measurement/src/main/kotlin/com/kronos/measurement/model/MeasurementUnit.kt`
- `measurement/src/main/kotlin/com/kronos/measurement/model/MeasurementConverter.kt`
- `measurement/src/test/kotlin/com/kronos/measurement/MeasurementTests.kt`
- `measurement/src/test/kotlin/com/kronos/measurement/FractionTests.kt`
- `measurement/src/test/kotlin/com/kronos/measurement/ExtensionTests.kt`

**Files modified (4):**
- `settings.gradle.kts` — add `include(":utils", ":measurement")`
- `build.gradle.kts` (root) — register Kotlin JVM plugin with `apply false` (both modules)
- `app/build.gradle.kts` — add `implementation(project(":measurement"))` and `implementation(project(":utils"))`
- `app/src/main/java/com/kronos/skilletapp/model/Ingredient.kt` — update `Measurement` import

**Files modified (consumer imports, ~35 files):**
All files that import from `com.kronos.skilletapp.model.measurement` or `com.kronos.skilletapp.utils` need their imports updated:
- Measurement imports: `com.kronos.skilletapp.model.measurement.*` → `com.kronos.measurement.model.*`
- Utils imports: `com.kronos.skilletapp.utils.*` → `com.kronos.utils.*`

**Files deleted from app module (6):**
- `app/src/main/java/com/kronos/skilletapp/model/measurement/Measurement.kt`
- `app/src/main/java/com/kronos/skilletapp/model/measurement/MeasurementUnit.kt`
- `app/src/main/java/com/kronos/skilletapp/model/measurement/MeasurementConverter.kt`
- `app/src/test/java/com/kronos/skilletapp/MeasurementTests.kt`
- `app/src/test/java/com/kronos/skilletapp/FractionTests.kt`
- `app/src/test/java/com/kronos/skilletapp/ExtensionTests.kt`
- `app/src/main/java/com/kronos/skilletapp/utils/Fraction.kt`
- `app/src/main/java/com/kronos/skilletapp/utils/NumberUtils.kt`
- `app/src/main/java/com/kronos/skilletapp/utils/MiscUtils.kt`

**Dependencies added:**
- `:utils` project dependency in `app/build.gradle.kts` and `measurement/build.gradle.kts`
- `:measurement` project dependency in `app/build.gradle.kts`

**Dependency graph:**
```
      :utils (shared JVM library)
     /       │       \
    ▼        ▼        ▼
 :measurement  :app  (parser, UI, navigation, viewmodels)
```

**Dependencies removed from app module (transitively):**
_None._ The app module keeps `kotlin-reflect`, `kotlin-result`, and `kotlinx-serialization-json` as they are used by non-measurement app code.
