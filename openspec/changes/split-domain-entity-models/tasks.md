## 1. Room entity models (additive)

- [x] 1.1 Create Room entity models in `database/entity/`: `RecipeEntity` (flattens `RecipeTime`→`prep_time`/`cook_time` and `RecipeSource`→`source_name`/`source_url`), `IngredientEntity` (with `measurement` column), `InstructionEntity`, `EquipmentEntity`, and join entities `InstructionIngredientEntity` + `InstructionEquipmentEntity` (each with `instruction_id`, the child id, and `position`); every child row carries `recipe_id` and `position`. Every reference column SHALL carry a Room `@ForeignKey` with `onDelete = ForeignKey.CASCADE`: child tables (`IngredientEntity`, `InstructionEntity`, `EquipmentEntity`) reference `recipe(id)` via `recipe_id`; join tables reference both `instruction(id)` via `instruction_id` and `ingredient(id)`/`equipment(id)` via their child-id columns. Verify: `./gradlew :app:assembleDebug` compiles the new entities, the domain models are unchanged, and FK declarations are present on all reference columns (child tables: one FK to `recipe(id)`; join tables: two FKs each).

## 2. DAOs, Measurement converter, mapper (additive)

- [ ] 2.1 Add per-table DAOs `RecipeDao`, `IngredientDao`, `InstructionDao`, `EquipmentDao` with upsert, `deleteByRecipeId`, and ordered selects (`ORDER BY position`); put the `instruction_ingredient`/`instruction_equipment` ordered selects on `InstructionDao`. Verify: `./gradlew :app:assembleDebug` compiles the new DAOs.
- [ ] 2.2 Add a single Room `TypeConverter` for `Measurement` ⇄ `String` (kotlinx.serialization JSON reusing the existing `Json` config, preserving the `measurement_type` discriminator) in its own new file `database/MeasurementConverters.kt` — NOT in `database/Converters.kt` (which holds `RecipeConverters` and is deleted in task 6.2). Verify: a unit test round-trips a named-unit and a `Custom`-unit `Measurement` through the converter with quantity and unit intact.
- [ ] 2.3 Add `RecipeMapper` (domain graph ⇄ entity rows + join rows): key child rows by domain `id`, set `position` = index in the source list, dedupe ingredients/equipment shared across recipe and instructions. Verify: a unit test maps a graph to entities and back, asserting fields, child lists, ordering, and that a shared ingredient produces exactly one row.

## 3. Database v2 and DI

- [ ] 3.1 Bump `RecipeDatabase` to `version = 2` with the new entities and per-table DAOs, and remove the old single-table `Recipe` entity + `RecipeDao` from the `@Database`. Verify: `./gradlew :app:assembleDebug` compiles and a v2 schema JSON is generated in `app/schemas/`.
- [ ] 3.2 In `SkilletApp.kt`, register the per-table DAOs and `RecipeMapper` as Koin singles, and add `fallbackToDestructiveMigration()` plus a `RoomDatabase.Callback` enabling `PRAGMA foreign_keys = ON` in `onSupportOpen` to the Room builder (enforces the `@ForeignKey`/CASCADE constraints declared in task 1.1). (The `ScrapeRecipe` factory arity change is intentionally deferred to task 5.1, where the class change and its DI wiring land atomically.) Verify: the FK-enabling `RoomDatabase.Callback` is registered on the Room builder, the Koin graph resolves at startup (existing instrumented context test), and `./gradlew :app:assembleDebug` is green.

## 4. Repository collapse (coordinated with the ViewModel)

- [ ] 4.1 Add `val id: String = Uuid.random().toString()` to the domain `Recipe` so IDs exist at model creation. Verify: `./gradlew :app:assembleDebug` compiles and existing `Recipe` constructions still resolve.
- [ ] 4.2 Collapse the `RecipeRepository` interface so `createRecipe(recipe: Recipe)` and `updateRecipe(id: String, recipe: Recipe)` take a domain `Recipe`. Verify: `./gradlew :app:assembleDebug` compiles the interface.
- [ ] 4.3 Rewrite `RecipeRepositoryImpl`: reads load the relational rows and assemble a domain `Recipe` (ordered by `position`); writes map to entities and persist atomically via `database.withTransaction { … }` (upsert recipe row, replace child + join rows by `recipe_id`); `createRecipe` returns the model's existing id and `updateRecipe` never mints ids. Verify: a repository unit test asserts create returns the model id, a re-read preserves all ids, and an update replaces the stored graph.
- [ ] 4.4 Update all ViewModel call sites to the collapsed `createRecipe`/`updateRecipe` signatures. Verify: `./gradlew :app:assembleDebug` compiles with no stale 14-arg calls.

## 5. Scraping and form state

- [ ] 5.1 Change `ScrapeRecipe` to return `Result<ScrapedRecipe, RecipeScrapeError>` with raw ingredient/instruction strings, remove its `IngredientParser` dependency, and update the `ScrapeRecipe` Koin factory in `SkilletApp.kt` to the new single-arg constructor (the class change and its DI wiring ship together in this task). Verify: `ScrapeRecipeTests` pass, `SkilletApp.kt`'s `ScrapeRecipe` factory is updated to the new constructor arity with `./gradlew :app:assembleDebug` green, and `rg "import com.kronos.skilletapp.ui" app/src/main/java/com/kronos/skilletapp/domain` returns nothing.
- [ ] 5.2 Extract `RecipeState` from `AddEditRecipeViewModel.kt` into `ui/viewmodel/RecipeFormState.kt`, rename it `RecipeFormState`, and update all references. Verify: `./gradlew :app:assembleDebug` compiles and no `RecipeState` type remains in the codebase.
- [ ] 5.3 Update `AddEditRecipeViewModel` to convert a `ScrapedRecipe` into `RecipeFormState`, parsing raw ingredient strings via an injected `IngredientParser`. Verify: `./gradlew :app:assembleDebug` compiles and a scrape→form-state test (or existing VM test) maps fields and parsed ingredients.

## 6. Cleanup and domain purity

- [ ] 6.1 Strip Room annotations (`@Entity`, `@PrimaryKey`, `@Embedded`, `@TypeConverters`) from the domain models in `model/`, keeping `@Serializable` and all field names/shapes. Verify: no domain model is `@Entity` and `./gradlew :app:assembleDebug` compiles.
- [ ] 6.2 Delete `database/Converters.kt` (`RecipeConverters`). Verify: the file is removed, no references remain, and `./gradlew :app:assembleDebug` compiles.

## 7. Verification

- [ ] 7.1 Generate the v2 Room schema JSON into `app/schemas/`. Verify: `./gradlew :app:assembleDebug` emits a `2.json` schema and no `1.json` is left required.
- [ ] 7.2 Run the full gate: `./gradlew :app:assembleDebug`, `./gradlew test`, `./gradlew :app:lint`. Verify: all three commands pass.
- [ ] 7.3 Confirm the no-`ui`-import invariant and the "Measurement is the only converted type" invariant. Verify: `rg "import com.kronos.skilletapp.ui" app/src/main/java/com/kronos/skilletapp/domain app/src/main/java/com/kronos/skilletapp/data` returns nothing, and no `list-of-T` ⇄ `String` JSON converter remains in `database/`.
