## Context

Today the persistence layer is a single Room `@Entity` `Recipe` (in `model/`) that doubles as the app's domain model, with `ingredients`/`instructions`/`equipment` stored as JSON-blob columns decoded by `RecipeConverters` (which silently return `emptyList()` on any decode failure). `RecipeRepositoryImpl` mints a fresh UUID in `createRecipe`, and the 14-arg `createRecipe`/`updateRecipe` rebuild the entity from flat args. `ScrapeRecipe` returns `ui.viewmodel.RecipeState` directly, so the domain package imports `ui`. The Room DB is version 1, single-table.

Constraints: only `:app` is touched (no new Gradle module); domain-model field names/shapes must be preserved (the ANTLR `parser/` package builds them); the domain package must not import `ui`; Room 2.8, min SDK 30. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Split domain models (`model/`) from Room entity models (new `database/entity/`), connected by a mapper.
- Store recipes relationally: `recipe`, `ingredient`, `instruction`, `equipment` tables plus `instruction_ingredient` / `instruction_equipment` join tables, all carrying a `position` column.
- Replace `RecipeConverters` with a single `Measurement` type-converter.
- Preserve IDs across upsert; make writes atomic; keep read order = stored order; keep N–M ingredient/equipment identity.
- Collapse `createRecipe`/`updateRecipe` to a domain `Recipe` payload.
- Make `ScrapeRecipe` return `ScrapedRecipe` and remove the domain→`ui` import; extract/rename `RecipeState` to `RecipeFormState`.

**Non-Goals:**
- No new Gradle module; no changes to `:utils` / `:measurement`.
- No changes to domain-model field names/shapes (the parser contract).
- No `UiState` → `Async` refactor (separate change, sequenced after this one).
- No data-preserving schema migration (v1 → v2 is destructive by design).
- No change to cross-dimension measurement conversion behavior.

## Decisions

**1. Package split inside `:app`; no new module.**
Domain models stay in `com.kronos.skilletapp.model` but lose their Room annotations (`@Entity`, `@PrimaryKey`, `@Embedded`, `@TypeConverters`) — keeping `@Serializable` and all field names. New Room entities go in `com.kronos.skilletapp.database.entity`. The mapper lives in `com.kronos.skilletapp.data`.
- Alternative: a new `:persistence` Gradle module (mirroring `:measurement`). Rejected — the `:app`-only constraint; a module adds build surface for a change that is otherwise contained.

**2. `RecipeTime` / `RecipeSource` flatten to scalar columns on the `recipe` row.**
The domain keeps the VOs (`Recipe.time: RecipeTime`, `Recipe.source: RecipeSource`); the `recipe` entity stores `prep_time`, `cook_time`, `source_name`, `source_url` as plain Int/Text columns, and the mapper bridges VO ↔ columns.
- Rationale: the VOs have no identity or independent lifecycle, so `@Embedded` (which would leak the domain VO types into the entity layer) buys nothing; flattening keeps the schema explicit and queryable.
- Alternative: keep `@Embedded` VOs on the entity. Rejected — couples entities to domain VO types.

**3. Explicit join tables with `position`, not `@Relation` (durable — do not re-open).**
`instruction_ingredient` and `instruction_equipment` are explicit entities carrying `instruction_id`, `*_id`, and `position`. DAOs select them `ORDER BY position`; the mapper/repository reassembles them onto each instruction.
- Rationale (durable): Room `@Relation` — including many-to-many via `EntityAssocs` — returns associated collections that are unordered; there is no `ORDER BY` on the generated association query, so the `position` column could not survive the round-trip. That would break the "reads restore stored order" invariant. Explicit join tables + `ORDER BY position` DAO selects + mapper reassembly is the only mechanism that guarantees stored order is restored.
- Alternative: `@Relation` + `associateBy`. Rejected — cannot guarantee order; and because the ordering gap cannot be filled, this path collapses into explicit join tables anyway, so it is rejected outright to avoid re-litigation.

**4. `Measurement` is the only converted type (single column).**
Each `ingredient` row stores `measurement` as one column; a `TypeConverter` does `Measurement` ⇄ `String` (kotlinx.serialization JSON, reusing the existing `Json { ignoreUnknownKeys = true }` config; the `measurement_type` discriminator round-trips named and `Custom` units). Quantity and unit live inside that single column.
- Rationale: matches "Measurement is the only converted type" with minimal converter surface. SQL never needs `quantity` to be a numeric column (scaling is domain-side).
- Alternative: `quantity REAL` + `unit TEXT` (two columns, converter on `MeasurementUnit`). Rejected for simplicity / less surface.

**5. Mapper is a Koin-injected `RecipeMapper`.**
A stateless mapper (domain graph ⇄ entity set + join rows) bound via `singleOf`. Child rows are keyed by the domain `id`; join rows reference ids; `position` = index in the source list. Ingredients/equipment referenced at both the recipe level and per instruction are deduped by `id` when materializing rows.
- Alternative: top-level extension functions. Chose a class for DI consistency and easy mocking in tests.

**6. Atomic writes via `RoomDatabase.withTransaction`.**
`RecipeRepositoryImpl` holds the `RecipeDatabase` and runs every create/update inside `db.withTransaction { … }`: upsert the recipe row, replace the child rows for the recipe id (delete-by-recipeId, then insert with fresh positions), and rewrite the join rows. A mid-write failure rolls back the whole graph.
- Alternative: a `@Transaction` on a single DAO method. `withTransaction` is preferred because the write spans several DAOs.

**7. IDs minted once at domain-model creation.**
`Recipe` gains `val id: String = Uuid.random().toString()` (matching `Ingredient`/`Instruction`/`Equipment`, which already default). `createRecipe(recipe)` persists the model's existing id and returns it; `updateRecipe` never mints ids. Upsert-by-id keeps stability.

**8. `ScrapeRecipe` returns `ScrapedRecipe`; parsing moves to the ViewModel.**
`ScrapeRecipe.invoke` returns `Result<ScrapedRecipe, RecipeScrapeError>` with raw ingredient/instruction strings (the `ScrapedRecipe` DTO already declares `List<String>`). `ScrapeRecipe` drops its now-unused `IngredientParser` dependency, and its Koin factory in `SkilletApp.kt` is updated to the new single-arg constructor in the SAME step as this class change (the constructor currently takes both `scraper` and `parser`, so the DI wiring must land atomically with the class to keep the build green — it is NOT part of the earlier Database/DI wiring step). `AddEditRecipeViewModel` parses raw strings via an injected `IngredientParser` when building `RecipeFormState`.
- Rationale: keeps `ScrapeRecipe` a pure scrape→DTO concern, matches the DTO's `List<String>` shape, and removes the domain→`ui` import.
- Trade-off: the ViewModel gains an `IngredientParser` dependency (see Risks).
- Alternative: keep parsing in `ScrapeRecipe` and change `ScrapedRecipe` to carry parsed `Ingredient`s. Rejected — would change the DTO shape and the existing `domain-scraping` "raw strings" contract.

**9. `RecipeFormState` is a standalone type.**
`RecipeState` (currently declared in `AddEditRecipeViewModel.kt`) is extracted to `ui/viewmodel/RecipeFormState.kt` and renamed `RecipeFormState`; all references update. Same fields, including `tharBeChanges`, `isRecipeSaved`, `isSaveInProgress`, `userMessage`.

**10. Destructive v1 → v2 migration.**
Bump `@Database(version = 2)`; register `fallbackToDestructiveMigration()` in the Room builder. No custom `Migration` object — the fallback rebuilds the schema and discards v1 data (accepted).

**11. Per-table DAOs.**
`RecipeDao`, `IngredientDao`, `InstructionDao`, `EquipmentDao`, each with upsert, delete-by-recipeId, and ordered selects. The join selects live on `InstructionDao`.
- Alternative: one combined DAO. Per-table is clearer for the transaction scope and query ordering.

**12. Referential integrity: `@ForeignKey` on every reference column + FK enforcement enabled.**
Every reference column SHALL be declared with a Room `@ForeignKey` carrying `onDelete = ForeignKey.CASCADE`:
- Child tables (`ingredient`, `instruction`, `equipment`) reference `recipe(id)` via `recipe_id`.
- Join tables (`instruction_ingredient`, `instruction_equipment`) reference BOTH `instruction(id)` via `instruction_id` AND `ingredient(id)` / `equipment(id)` via their child-id columns.
- SQLite gotcha (part of the decision): SQLite ships with `foreign_keys=OFF`, so these constraints are inert unless `PRAGMA foreign_keys = ON` is enabled per connection via a `RoomDatabase.Callback` (`onSupportOpen`). The constraint declarations land in G1 (task 1.1) on the entities; the pragma-enabling callback is registered in G3 (task 3.2) in the Room builder, alongside the v2 bump (task 3.1). The v2 schema is generated in G3 AFTER both are in place — adding FKs after `2.json` exists would force another version bump + destructive migration on a schema that has never held production data.
- Interaction with the G4 write path: the replace pattern (delete children by `recipe_id`, then insert, inside `RoomDatabase.withTransaction`) is compatible with the constraints because it deletes before re-inserting; `CASCADE` additionally makes any future direct parent-delete order-independent (deleting a recipe cascades to its children and join rows).
- Relationship to Decision 3: complementary, not competing. Decision 3 rejects `@Relation` for read-ordering reasons (kept); Decision 12 adds `@ForeignKey`/CASCADE row-integrity on the same reference columns. Both stand.

## Risks / Trade-offs

- [Destructive migration wipes user data on first launch] → accepted per scope; called out in the change summary; the v2 `app/schemas/` JSON is checked in.
- [`observeRecipes`/`observeRecipe` reassemble full graphs per emission → N+1 queries] → acceptable for personal-recipe scale; could later be served from a recipe-only summary query. Flagged, not solved here.
- [ViewModel now depends on `IngredientParser`] → it is a pure, DI-injected class; a minor layering cost that keeps `ScrapeRecipe` clean.
- [`fetchRecipe`/`observeRecipe` for a missing id: Room returns null → NPE risk carried over from today] → pre-existing, not worsened, not fixed in this change.
- [Deleting `RecipeConverters` removes silent `emptyList()` masking] → `SkilletError`/parse-error semantics shift: a storage decode failure now surfaces instead of silently emptying; surfaced errors are better than silent data loss.
- [Multi-table `Flow` reassembly] → implement with the recipe-row `Flow` driving `flatMapLatest { assemble(id) }`; correctness-first, not yet optimized.

## Migration Plan

1. Add entity models, per-table DAOs, `RecipeMapper`, and the `Measurement` converter (additive; the v1 table still exists at version 1).
2. Bump `@Database` to version 2, add `fallbackToDestructiveMigration()`, register a `RoomDatabase.Callback` enabling `PRAGMA foreign_keys = ON`, and register the new DAOs + mapper in Koin.
3. Rewrite `RecipeRepositoryImpl` (entity-flow composition, `withTransaction`) and collapse the `RecipeRepository` interface.
4. Fix `ScrapeRecipe` (return `ScrapedRecipe`, drop the parser dep, and update its `SkilletApp.kt` Koin factory to the new single-arg constructor) and extract/rename `RecipeFormState`; update the ViewModel and any screens that referenced `RecipeState`.
5. Delete `RecipeConverters`; strip Room annotations from the domain models; add the `Recipe.id` default.
6. Generate the v2 `app/schemas/` JSON; `./gradlew :app:assembleDebug` and `./gradlew test` green.

Rollback: revert the code. The v2 DB, once built, is empty-by-design, so there is no user-data rollback (accepted).

## Open Questions

- None blocking. The `RecipeTime`/`RecipeSource` flat-vs-`@Embedded` choice is resolved as flat (Decision 2); the parse-location question is resolved as "ViewModel" (Decision 8).
