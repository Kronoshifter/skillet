## Context

The Skillet app is an Android recipe app with three Gradle modules (`:app`, `:utils`, `:measurement`). It currently has no domain/use-case layer. The `RecipeRepository` class combines Room DAO calls (data access) with entity construction (domain logic). Form validation and scraping logic live in ViewModels. The user has staged changes adding the Koin compiler plugin — these must be preserved.

See `proposal.md` for the full motivation and scope.

## Goals / Non-Goals

**Goals:**
- Introduce a domain layer with use cases that encapsulate business logic
- Extract `RecipeRepository` into an interface + implementation for testability
- Resolve the scaling drift problem with `currentServings: Int` as single source of truth
- Make scraping logic testable by extracting it into a use case
- Preserve user's staged Koin compiler plugin changes

**Non-Goals:**
- Data-entity separation (Room-coupled models → pure domain entities) — deferred
- ANTLR parser relocation — parsing stays in `app` module
- Room schema migration — version 1 only
- CI, pre-commit hooks, or testing infrastructure changes

## Decisions

### Decision 1: Use `currentServings: Int` as single source of truth for scaling

**Rationale:** The current system has two independent state values (`scale: Float` and `servings: Int`) that can drift apart due to floating-point arithmetic. Using a single `Int` value eliminates this class of bug entirely.

**Alternatives considered:**
- Keep both values and add a reconciler — adds complexity, doesn't eliminate the root cause
- Use `Double` instead of `Float` for scale — still a separate source of truth, just more precise

**Implementation:**
- `RecipeUiState` and `CookingUiState` drop `scale: Float`; derive it as `currentServings / baseServings.toFloat()`
- Preset buttons (1/2x, 1x, 2x) become "set servings to X" — `currentServings = baseServings * presetMultiplier`
- The `ScaleRecipe` use case receives `targetServings: Int` and computes the factor internally

### Decision 2: UseCase pattern with Kotlin `invoke` operator

**Rationale:** Kotlin's `operator fun invoke()` provides a clean DSL-like usage pattern that matches the project's existing style (e.g., `result-kotlin`'s `ok()`/`err()`).

**Implementation:**
```kotlin
class ScaleRecipe {
    operator fun invoke(recipe: Recipe, targetServings: Int): ScaledRecipe { ... }
}
// Usage: val scaled = scaleRecipe(recipe, targetServings)
```

### Decision 3: Repository interface with Koin `bind` DSL

**Rationale:** An interface enables mocking in unit tests and DI flexibility. The Koin `bind` DSL is the idiomatic way to bind an interface in Koin 4.

**Implementation:**
```kotlin
interface RecipeRepository {
    suspend fun fetchRecipe(id: String): Recipe
    fun observeRecipe(id: String): Flow<Recipe>
    // ... other methods
}

class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val converter: RecipeConverters,
) : RecipeRepository { ... }
```

### Decision 4: Koin compiler plugin compatibility

**Rationale:** The user has staged changes adding the Koin compiler plugin (`io.insert-koin.compiler.plugin`). The plugin generates Koin code at compile time, which changes how `single`, `singleOf`, and `viewModelOf` are resolved.

**Constraints:**
- Do NOT use `import org.koin.core.module.dsl.*` wildcard imports (user already changed this)
- Use explicit imports for DSL functions
- The `singleOf(::ClassName)` syntax may need adjustment with the compiler plugin — the Smith should verify compilation after changes

### Decision 5: Incremental five-phase approach

**Rationale:** A big-bang refactor would be high-risk. The five-phase approach lets each phase be independently tested and verified.

**Phases:**
1. Scaling refactor (most impactful, lowest risk — fixes the floating-point drift bug)
2. Repository interface (enables mocking for all subsequent phases)
3. Validation use case (moves business rules out of ViewModel)
4. Scraping use case (makes scraping testable)
5. Data-entity separation (deferred — large effort, Room-coupled models)

**Parallelization:** Phases are listed sequentially for clarity but many tasks within and across phases are independent. The following phases can be executed in parallel:
- Phase 2 (UI state refactoring: tasks 2.1–2.4) and Phase 5 (repository interface: tasks 5.1–5.4) have no cross-dependencies and can proceed simultaneously.
- Phase 7 (validation: tasks 7.1–7.3) and Phase 8 (scraping: tasks 8.1–8.4) are independent of each other and can run in parallel.
- Within each phase, creation tasks (e.g., 7.1, 8.1) must precede their corresponding test tasks (7.3, 8.3), but other tasks in the same phase may proceed in parallel once the creation task is done.

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| Koin compiler plugin breaks existing bindings | Verify compilation after each phase; use explicit imports; the Smith should run `./gradlew :app:assembleDebug` to catch issues |
| Navigation param change (`scale: Float` → `currentServings: Int`) breaks deep links | Update `Route.Cooking` and `SkilletNavigationActions.navigateToCooking` atomically in Phase 1 |
| Compose previews break due to Koin changes | Use `KoinPreview` (not `@Preview`); update inline Koin modules in previews |
| `ScaleRecipe` depends on `MeasurementConverter` from `:measurement` module | The `:measurement` module is already pure JVM and depended on by `:app` — no circular dependency risk |
| `validateForm()` currently returns `Result<RecipeState, InvalidFormError>` — the domain use case should return `Result<Unit, InvalidFormError>` or `Result<Recipe, InvalidFormError>` | The use case returns `Result<Unit, InvalidFormError>` — on success it validates and returns `ok()`, on failure returns `err(error)` |

## Migration Plan

1. **Phase 1** (Scaling): Create `ScaledRecipe`, `ScaleRecipe`. Refactor `RecipeUiState` to use `currentServings`. Update `RecipeScreen` scaling controls. Update `Route.Cooking` and `CookingViewModel` to use `currentServings`. Update `CookingScreen` to derive scale.
2. **Phase 2** (Repository): Create `RecipeRepository` interface. Rename class to `RecipeRepositoryImpl`. Update Koin bindings. Update all ViewModels to depend on interface.
3. **Phase 3** (Validation): Create `ValidateRecipe` use case. Move `validateForm()` logic from `AddEditRecipeViewModel`. Update `saveRecipe()` to delegate.
4. **Phase 4** (Scraping): Create `ScrapedRecipe` domain type and `ScrapeRecipe` use case. Move scraping orchestration from `AddEditRecipeViewModel`. Add `ScrapedRecipe`→`RecipeState` conversion. Update `scrapeRecipe()` to delegate.
5. **Phase 5** (Deferred): Data-entity separation.

**Rollback:** Each phase is a discrete set of file changes. If a phase causes issues, revert that phase's changes only.

## Open Questions

1. **Koin compiler plugin syntax:** The exact syntax for `singleOf(::ClassName)` with the compiler plugin is unknown. The Smith should verify compilation and adjust if needed.
2. **Default servings for new recipes:** Currently `servings = 0` in `RecipeState`. Should `ValidateRecipe` accept servings = 0 for new recipes being created, or should the minimum be 1? — Decision: minimum 1 for both create and edit (enforced by validation).
3. **Navigation history:** When `Route.Cooking` changes from `scale: Float` to `currentServings: Int`, does any existing deep link URL need updating? — Decision: deep links for `Route.Cooking` are not currently used in production; update `basePath` mapping if needed.
