## 1. Create utils module structure

- [ ] 1.1 Create `utils/build.gradle.kts` with Kotlin JVM plugin, kotlinx-serialization, kotlin-reflect, and Kotest dependencies
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
- [ ] 1.2 Create directory structure:
  - `utils/src/main/kotlin/com/kronos/utils/`
  - `utils/src/test/kotlin/com/kronos/utils/`
- [ ] 1.3 Add `alias(libs.plugins.kotlin.jvm) apply false` to root `build.gradle.kts`
- [ ] 1.4 Add `include(":utils")` to `settings.gradle.kts`
- [ ] 1.5 Verify Gradle sync succeeds: `./gradlew tasks --all | grep utils`

## 2. Move utility files to utils module

- [ ] 2.1 Move `app/src/main/java/com/kronos/skilletapp/utils/Fraction.kt` → `utils/src/main/kotlin/com/kronos/utils/Fraction.kt`
  - Update package: `com.kronos.skilletapp.utils` → `com.kronos.utils`
- [ ] 2.2 Move `app/src/main/java/com/kronos/skilletapp/utils/NumberUtils.kt` → `utils/src/main/kotlin/com/kronos/utils/NumberUtils.kt`
  - Update package: `com.kronos.skilletapp.utils` → `com.kronos.utils`
- [ ] 2.3 Move `app/src/main/java/com/kronos/skilletapp/utils/MiscUtils.kt` → `utils/src/main/kotlin/com/kronos/utils/MiscUtils.kt`
  - Update package: `com.kronos.skilletapp.utils` → `com.kronos.utils`
- [ ] 2.4 Verify utils module compiles: `./gradlew :utils:compileKotlin`

## 3. Create measurement module structure

- [ ] 3.1 Create `measurement/build.gradle.kts` with Kotlin JVM plugin, dependencies on `:utils`, kotlinx-serialization, kotlin-result, kotlin-reflect, and Kotest
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
- [ ] 3.2 Create directory structure:
  - `measurement/src/main/kotlin/com/kronos/measurement/model/`
  - `measurement/src/test/kotlin/com/kronos/measurement/`
- [ ] 3.3 Add `include(":measurement")` to `settings.gradle.kts`
- [ ] 3.4 Verify Gradle sync succeeds: `./gradlew tasks --all | grep measurement`

## 4. Move measurement files to measurement module

- [ ] 4.1 Move `app/src/main/java/com/kronos/skilletapp/model/measurement/Measurement.kt` → `measurement/src/main/kotlin/com/kronos/measurement/model/Measurement.kt`
  - Update package: `com.kronos.skilletapp.model.measurement` → `com.kronos.measurement.model`
  - Update imports: `com.kronos.skilletapp.utils.*` → `com.kronos.utils.*`
- [ ] 4.2 Move `app/src/main/java/com/kronos/skilletapp/model/measurement/MeasurementConverter.kt` → `measurement/src/main/kotlin/com/kronos/measurement/model/MeasurementConverter.kt`
  - Update package: `com.kronos.skilletapp.model.measurement` → `com.kronos.measurement.model`
  - Update imports: `com.kronos.skilletapp.utils.*` → `com.kronos.utils.*`
- [ ] 4.3 Move `app/src/main/java/com/kronos/skilletapp/model/measurement/MeasurementUnit.kt` → `measurement/src/main/kotlin/com/kronos/measurement/model/MeasurementUnit.kt`
  - Update package: `com.kronos.skilletapp.model.measurement` → `com.kronos.measurement.model`
  - Update imports: remove `import com.kronos.skilletapp.model.IngredientType.Dry` and `import com.kronos.skilletapp.model.IngredientType.Wet`
  - **Delete** the `allowedIngredientTypes` map (lines 341-362) and the `private val wet/dry/either` declarations
- [ ] 4.4 Verify internal cross-references compile — `MeasurementConverter.kt` imports `MeasurementUnit.None.baseUnit` which should resolve correctly after package update. Verify that `com.kronos.utils.*` imports resolve.
- [ ] 4.5 Verify measurement module compiles: `./gradlew :measurement:compileKotlin`

## 5. Move test files

- [ ] 5.1 Move `app/src/test/java/com/kronos/skilletapp/MeasurementTests.kt` → `measurement/src/test/kotlin/com/kronos/measurement/MeasurementTests.kt`
  - Update package: `com.kronos.skilletapp` → `com.kronos.measurement`
  - Update imports: `com.kronos.skilletapp.model.measurement.*` → `com.kronos.measurement.model.*`
  - Update imports: `com.kronos.skilletapp.utils.*` → `com.kronos.utils.*`
- [ ] 5.2 Move `app/src/test/java/com/kronos/skilletapp/FractionTests.kt` → `measurement/src/test/kotlin/com/kronos/measurement/FractionTests.kt`
  - Update package: `com.kronos.skilletapp` → `com.kronos.measurement`
  - Update imports: `com.kronos.skilletapp.utils.*` → `com.kronos.utils.*`
- [ ] 5.3 Move `app/src/test/java/com/kronos/skilletapp/ExtensionTests.kt` → `measurement/src/test/kotlin/com/kronos/measurement/ExtensionTests.kt`
  - Update package: `com.kronos.skilletapp` → `com.kronos.measurement`
  - Update imports: `com.kronos.skilletapp.utils.*` → `com.kronos.utils.*`
- [ ] 5.4 Verify measurement tests pass: `./gradlew :measurement:test`

## 6. Update app module dependencies and remove old sources

- [ ] 6.1 Add `implementation(project(":measurement"))` to `app/build.gradle.kts` `dependencies {}` block
- [ ] 6.2 Add `implementation(project(":utils"))` to `app/build.gradle.kts` `dependencies {}` block
- [ ] 6.3 Do NOT remove `implementation(libs.kotlin.reflect)` from `app/build.gradle.kts` — the app module uses it in `SkilletNavigation.kt`, `SkilletNavGraph.kt`, and `RecipeListViewModel.kt`
- [ ] 6.4 Delete old measurement files from app module:
  - `app/src/main/java/com/kronos/skilletapp/model/measurement/Measurement.kt`
  - `app/src/main/java/com/kronos/skilletapp/model/measurement/MeasurementConverter.kt`
  - `app/src/main/java/com/kronos/skilletapp/model/measurement/MeasurementUnit.kt`
  - `app/src/test/java/com/kronos/skilletapp/MeasurementTests.kt`
  - `app/src/test/java/com/kronos/skilletapp/FractionTests.kt`
  - `app/src/test/java/com/kronos/skilletapp/ExtensionTests.kt`
- [ ] 6.5 Delete old utils files from app module:
  - `app/src/main/java/com/kronos/skilletapp/utils/Fraction.kt`
  - `app/src/main/java/com/kronos/skilletapp/utils/NumberUtils.kt`
  - `app/src/main/java/com/kronos/skilletapp/utils/MiscUtils.kt`
- [ ] 6.6 Delete `app/src/main/java/com/kronos/skilletapp/model/measurement/` directory if now empty
- [ ] 6.7 Delete `app/src/main/java/com/kronos/skilletapp/utils/` directory if now empty

## 7. Update consumer imports across app module

**Both measurement AND utils imports must be updated.**

**Measurement imports that change:**
- `com.kronos.skilletapp.model.measurement.Measurement` → `com.kronos.measurement.model.Measurement`
- `com.kronos.skilletapp.model.measurement.MeasurementUnit` → `com.kronos.measurement.model.MeasurementUnit`
- `com.kronos.skilletapp.model.measurement.displayQuantity` → `com.kronos.measurement.model.displayQuantity`
- `com.kronos.skilletapp.model.measurement.convertTo` → `com.kronos.measurement.model.convertTo`
- `com.kronos.skilletapp.model.measurement.hasSameDimensionAs` → `com.kronos.measurement.model.hasSameDimensionAs`

**Utils imports that change:**
- `com.kronos.skilletapp.utils.Fraction` → `com.kronos.utils.Fraction`
- `com.kronos.skilletapp.utils.fraction` → `com.kronos.utils.fraction`
- `com.kronos.skilletapp.utils.nearestEighth` → `com.kronos.utils.nearestEighth`
- `com.kronos.skilletapp.utils.nearestThird` → `com.kronos.utils.nearestThird`
- `com.kronos.skilletapp.utils.roundToNth` → `com.kronos.utils.roundToNth`
- `com.kronos.skilletapp.utils.gcd` → `com.kronos.utils.gcd`
- `com.kronos.skilletapp.utils.lcm` → `com.kronos.utils.lcm`
- `com.kronos.skilletapp.utils.toJson` → `com.kronos.utils.toJson`
- `com.kronos.skilletapp.utils.fromJson` → `com.kronos.utils.fromJson`
- `com.kronos.skilletapp.utils.applyIf` → `com.kronos.utils.applyIf`
- `com.kronos.skilletapp.utils.applyUnless` → `com.kronos.utils.applyUnless`
- `com.kronos.skilletapp.utils.mutateIf` → `com.kronos.utils.mutateIf`
- `com.kronos.skilletapp.utils.mutateUnless` → `com.kronos.utils.mutateUnless`
- `com.kronos.skilletapp.utils.haveSameTypes` → `com.kronos.utils.haveSameTypes`
- `com.kronos.skilletapp.utils.haveSameType` → `com.kronos.utils.haveSameType`
- `com.kronos.skilletapp.utils.move` → `com.kronos.utils.move`
- `com.kronos.skilletapp.utils.err` → `com.kronos.utils.err`
- `com.kronos.skilletapp.utils.ok` → `com.kronos.utils.ok`
- `com.kronos.skilletapp.utils.update` → `com.kronos.utils.update`
- `com.kronos.skilletapp.utils.upsert` → `com.kronos.utils.upsert`
- `com.kronos.skilletapp.utils.isNotNullOrBlank` → `com.kronos.utils.isNotNullOrBlank`
- `com.kronos.skilletapp.utils.pluralize` → `com.kronos.utils.pluralize`
- `com.kronos.skilletapp.utils.navDeepLinkRequest` → `com.kronos.utils.navDeepLinkRequest`
- `com.kronos.skilletapp.utils.navTypeOf` → `com.kronos.utils.navTypeOf`
- `com.kronos.skilletapp.utils.verticalFadingEdge` → `com.kronos.utils.verticalFadingEdge`
- `com.kronos.skilletapp.utils.modifier.applyIf` → `com.kronos.utils.modifier.applyIf`
- `com.kronos.skilletapp.utils.modifier.applyUnless` → `com.kronos.utils.modifier.applyUnless`
- `com.kronos.skilletapp.utils.removePunctuation` → `com.kronos.utils.removePunctuation`
- `com.kronos.skilletapp.utils.roundToEighth` → `com.kronos.utils.roundToEighth`
- `com.kronos.skilletapp.utils.roundToThird` → `com.kronos.utils.roundToThird`
- `com.kronos.skilletapp.utils.roundToSignificantFigures` → `com.kronos.utils.roundToSignificantFigures`
- `com.kronos.skilletapp.utils.ONE_EIGHTH` → `com.kronos.utils.ONE_EIGHTH`

**Consumer files to update (with both measurement and utils imports):**

- [ ] 7.1 `app/src/main/java/com/kronos/skilletapp/model/Ingredient.kt`
  - `import com.kronos.skilletapp.model.measurement.Measurement` → `import com.kronos.measurement.model.Measurement`
- [ ] 7.2 `app/src/main/java/com/kronos/skilletapp/data/RecipeRepository.kt`
  - `import com.kronos.skilletapp.model.measurement.Measurement` → `import com.kronos.measurement.model.Measurement`
  - `import com.kronos.skilletapp.model.measurement.MeasurementUnit` → `import com.kronos.measurement.model.MeasurementUnit`
- [ ] 7.3 `app/src/main/java/com/kronos/skilletapp/parser/IngredientVisitor.kt`
  - `import com.kronos.skilletapp.model.measurement.Measurement` → `import com.kronos.measurement.model.Measurement`
  - `import com.kronos.skilletapp.model.measurement.MeasurementUnit` → `import com.kronos.measurement.model.MeasurementUnit`
  - `import com.kronos.skilletapp.utils.Fraction` → `import com.kronos.utils.Fraction`
  - `import com.kronos.skilletapp.utils.removePunctuation` → `import com.kronos.utils.removePunctuation`
- [ ] 7.4 `app/src/main/java/com/kronos/skilletapp/ui/screen/recipe/RecipeScreen.kt`
  - `import com.kronos.skilletapp.model.measurement.Measurement` → `import com.kronos.measurement.model.Measurement`
  - `import com.kronos.skilletapp.model.measurement.MeasurementUnit` → `import com.kronos.measurement.model.MeasurementUnit`
  - `import com.kronos.skilletapp.model.measurement.displayQuantity` → `import com.kronos.measurement.model.displayQuantity`
  - `import com.kronos.skilletapp.utils.fraction` → `import com.kronos.utils.fraction`
  - `import com.kronos.skilletapp.utils.mutateUnless` → `import com.kronos.utils.mutateUnless`
- [ ] 7.5 `app/src/main/java/com/kronos/skilletapp/ui/screen/AddEditRecipeScreen.kt`
  - `import com.kronos.skilletapp.model.measurement.Measurement` → `import com.kronos.measurement.model.Measurement`
  - `import com.kronos.skilletapp.model.measurement.MeasurementUnit` → `import com.kronos.measurement.model.MeasurementUnit`
  - `import com.kronos.skilletapp.model.measurement.displayQuantity` → `import com.kronos.measurement.model.displayQuantity`
  - `import com.kronos.skilletapp.utils.modifier.applyIf` → `import com.kronos.utils.modifier.applyIf`
  - `import com.kronos.skilletapp.utils.move` → `import com.kronos.utils.move`
  - `import com.kronos.skilletapp.utils.pluralize` → `import com.kronos.utils.pluralize`
- [ ] 7.6 `app/src/main/java/com/kronos/skilletapp/ui/component/UnitSelectionBottomSheet.kt`
  - `import com.kronos.skilletapp.model.measurement.Measurement` → `import com.kronos.measurement.model.Measurement`
  - `import com.kronos.skilletapp.model.measurement.MeasurementUnit` → `import com.kronos.measurement.model.MeasurementUnit`
  - `import com.kronos.skilletapp.model.measurement.displayQuantity` → `import com.kronos.measurement.model.displayQuantity`
  - `import com.kronos.skilletapp.utils.modifier.applyIf` → `import com.kronos.utils.modifier.applyIf`
- [ ] 7.7 `app/src/main/java/com/kronos/skilletapp/ui/component/IngredientComponent.kt`
  - `import com.kronos.skilletapp.model.measurement.Measurement` → `import com.kronos.measurement.model.Measurement`
  - `import com.kronos.skilletapp.model.measurement.MeasurementUnit` → `import com.kronos.measurement.model.MeasurementUnit`
  - `import com.kronos.skilletapp.model.measurement.convertTo` → `import com.kronos.measurement.model.convertTo`
  - `import com.kronos.skilletapp.model.measurement.displayQuantity` → `import com.kronos.measurement.model.displayQuantity`
  - `import com.kronos.skilletapp.model.measurement.hasSameDimensionAs` → `import com.kronos.measurement.model.hasSameDimensionAs`
  - `import com.kronos.skilletapp.utils.Fraction` → `import com.kronos.utils.Fraction`
  - `import com.kronos.skilletapp.utils.fraction` → `import com.kronos.utils.fraction`
  - `import com.kronos.skilletapp.utils.modifier.applyIf` → `import com.kronos.utils.modifier.applyIf`
  - `import com.kronos.skilletapp.utils.modifier.applyUnless` → `import com.kronos.utils.modifier.applyUnless`
- [ ] 7.8 `app/src/main/java/com/kronos/skilletapp/ui/screen/cooking/CookingScreen.kt`
  - `import com.kronos.skilletapp.model.measurement.MeasurementUnit` → `import com.kronos.measurement.model.MeasurementUnit`
- [ ] 7.9 `app/src/main/java/com/kronos/skilletapp/ui/viewmodel/RecipeViewModel.kt`
  - `import com.kronos.skilletapp.model.measurement.MeasurementUnit` → `import com.kronos.measurement.model.MeasurementUnit`
- [ ] 7.10 `app/src/main/java/com/kronos/skilletapp/ui/viewmodel/CookingViewModel.kt`
  - `import com.kronos.skilletapp.model.measurement.MeasurementUnit` → `import com.kronos.measurement.model.MeasurementUnit`
- [ ] 7.11 `app/src/main/java/com/kronos/skilletapp/ui/ComposeUtils.kt`
  - `import com.kronos.skilletapp.model.measurement.Measurement` → `import com.kronos.measurement.model.Measurement`
  - `import com.kronos.skilletapp.model.measurement.MeasurementUnit` → `import com.kronos.measurement.model.MeasurementUnit`
  - `import com.kronos.skilletapp.utils.fromJson` → `import com.kronos.utils.fromJson`
  - `import com.kronos.skilletapp.utils.toJson` → `import com.kronos.utils.toJson`
- [ ] 7.12 `app/src/main/java/com/kronos/skilletapp/navigation/SkilletNavGraph.kt`
  - `import com.kronos.skilletapp.utils.toJson` → `import com.kronos.utils.toJson`
  - `import com.kronos.skilletapp.utils.navDeepLinkRequest` → `import com.kronos.utils.navDeepLinkRequest`
  - `import com.kronos.skilletapp.utils.navTypeOf` → `import com.kronos.utils.navTypeOf`
- [ ] 7.13 `app/src/main/java/com/kronos/skilletapp/ui/component/ItemComponent.kt`
  - `import com.kronos.skilletapp.utils.modifier.applyIf` → `import com.kronos.utils.modifier.applyIf`
- [ ] 7.14 `app/src/main/java/com/kronos/skilletapp/ui/component/Picker.kt`
  - `import com.kronos.skilletapp.utils.modifier.verticalFadingEdge` → `import com.kronos.utils.modifier.verticalFadingEdge`
- [ ] 7.15 `app/src/main/java/com/kronos/skilletapp/ui/component/TimeSelectBottomSheet.kt`
  - `import com.kronos.skilletapp.utils.pluralize` → `import com.kronos.utils.pluralize`
- [ ] 7.16 `app/src/main/java/com/kronos/skilletapp/ui/screen/recipelist/RecipeListScreen.kt`
  - `import com.kronos.skilletapp.utils.isNotNullOrBlank` → `import com.kronos.utils.isNotNullOrBlank`
- [ ] 7.17 `app/src/main/java/com/kronos/skilletapp/ui/viewmodel/AddEditRecipeViewModel.kt`
  - `import com.kronos.skilletapp.utils.err` → `import com.kronos.utils.err`
  - `import com.kronos.skilletapp.utils.move` → `import com.kronos.utils.move`
  - `import com.kronos.skilletapp.utils.ok` → `import com.kronos.utils.ok`
  - `import com.kronos.skilletapp.utils.update` → `import com.kronos.utils.update`
  - `import com.kronos.skilletapp.utils.upsert` → `import com.kronos.utils.upsert`
- [ ] 7.18 `app/src/main/java/com/kronos/skilletapp/ui/viewmodel/RecipeListViewModel.kt`
  - `import com.kronos.skilletapp.utils.navTypeOf` → `import com.kronos.utils.navTypeOf`
- [ ] 7.19 `app/src/test/java/com/kronos/skilletapp/ParserTests.kt`
  - `import com.kronos.skilletapp.model.measurement.MeasurementUnit` → `import com.kronos.measurement.model.MeasurementUnit`

## 8. Verify build and tests

- [ ] 8.1 Run `./gradlew :utils:compileKotlin` — verify utils module compiles
- [ ] 8.2 Run `./gradlew :measurement:compileKotlin` — verify measurement module compiles
- [ ] 8.3 Run `./gradlew :measurement:test` — verify all measurement tests pass in pure JVM context
- [ ] 8.4 Run `./gradlew :app:compileDebugKotlin` — verify app module compiles with new imports
- [ ] 8.5 Run `./gradlew :app:assembleDebug` — verify full debug APK builds
- [ ] 8.6 Run `./gradlew :app:testDebugUnitTest` — verify existing app unit tests still pass
- [ ] 8.7 Run `rg "com.kronos.skilletapp.model.measurement" --type kotlin app/` — verify zero remaining old-package measurement references in app module
- [ ] 8.8 Run `rg "com.kronos.skilletapp.utils\." --type kotlin app/src/main/` — verify zero remaining old-package utils references in app source
- [ ] 8.9 Run `rg "com.kronos.skilletapp.utils\." --type kotlin app/src/test/` — verify zero remaining old-package utils references in app tests

## 9. Final cleanup

- [ ] 9.1 Verify `app/src/main/java/com/kronos/skilletapp/model/measurement/` directory is fully deleted
- [ ] 9.2 Verify `app/src/main/java/com/kronos/skilletapp/utils/` directory is fully deleted
- [ ] 9.3 Run `./gradlew :app:lint` — verify no lint warnings related to missing imports
- [ ] 9.4 Run `git status` — review all changed files for correctness
- [ ] 9.5 Document the module boundary in `AGENTS.md` under Architecture section:
  - Add `utils/` and `measurement/` to the module list
  - Update package layout table to include:
    - `utils/` — Shared utilities: `Fraction`, `NumberUtils`, `MiscUtils`
    - `measurement/model/` — Measurement types: `Measurement`, `MeasurementUnit`, `MeasurementConverter`
  - Note that `utils/` is a pure Kotlin JVM library (not Android), depended on by both `:app` and `:measurement`
  - Note that `measurement/` is a pure Kotlin JVM library (not Android), depends on `:utils`
