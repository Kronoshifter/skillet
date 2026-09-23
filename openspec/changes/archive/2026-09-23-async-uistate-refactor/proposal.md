## Why

The current `UiState<T>` sealed interface (`Loading` / `Loaded` / `LoadedWithData<T>` / `Error<T>`) was carried over from a sample app and its shape has warped the way every screen and ViewModel is written. `Loaded` is ambiguous (it means "idle/success" on the form screen but is *illegal* and throws on the data screens), `LoadedWithData<T>` is the only state that carries `T` (the other three erase it), the flow pipeline forces a `catch<UiState<T>>` operator to ferry exceptions, and every data ViewModel maintains a `_isLoading` flag that is always `false` and exists only to fill the `combine(_isLoading, dataFlow)` slot. The same misshape shows up in the UI: `uiState` (which changes on every ingredient-unit selection and every scaling change) is collected at the top of `RecipeScreen`/`CookingScreen` and captured inside the `LoadingContent` content lambda, so every interaction re-executes the `AnimatedContent` and re-composes the whole content subtree. On `AddEditRecipeScreen`, a 1750-line form is torn down and replaced with a spinner for the duration of an initial load or scrape, losing scroll, focus, and IME state.

This change replaces `UiState<T>` with an `Async<T>` type that makes the "has data / has error / is loading" states explicit and exhaustively matchable, replaces the `combine`/`catch` pipeline with a Flow transformer chain (`.map` / `.catch` / `.stateIn`), adds an `AsyncContent` composable to replace both `LoadingContent` overloads, enforces the top-level ViewModel rule (ViewModel state is collected in the top-level screen composable and only plain data + `vm::method` lambdas are passed to children), and drops the form screen's full-wrap spinner in favor of a plain busy flag.

## What Changes

- **New:** `data/Async.kt` — sealed `Async<T>` (`Idle` / `Loading` / `Success<T>` / `Failure`) with `data`/`error`/`isLoading` accessors; the sealed states are used directly (`Async.Idle`, `Async.Loading`, `Async.Success(…)`, `Async.Failure(…)` — no companion factories). Replaces `UiState<T>`.
- **New:** `AsyncContent<T>` composable in `ui/ComposeUtils.kt` — single full-wrap composable handling all four states via `AnimatedContent`, replacing both `LoadingContent` overloads. `Idle` renders a spinner by default (no blank flash on cold start).
- **Modified (Mode A):** `RecipeListViewModel`, `RecipeViewModel`, `CookingViewModel` — remove `_isLoading`, `combine`, the `catch<UiState<T>>` operator, the `MutableStateFlow<Async<T>>`/`init`-collect pattern, and the `UsedLoadedWhereYouShouldntError` sentinel; expose `recipeAsync` / `cookingAsync` (and `recipeListAsync` for the list) built as a Flow transformer chain: `.map { Async.Success(it) }` / `.catch { emit(Async.Failure(…)) }` / `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), Async.Idle)`.
- **Modified (Mode A):** `RecipeViewModel` / `CookingViewModel` — `originalRecipe` moves out of `RecipeUiState`/`CookingUiState` into a private VM field; the screen derives `originalIngredients` from the async `recipe` payload (`recipe.ingredients`) instead of `uiState.originalRecipe?.ingredients`.
- **Modified (Mode B):** `AddEditRecipeViewModel` — `UiState<Nothing>` (the initial load/scrape spinner) becomes `isInitializing: StateFlow<Boolean>`; the screen renders a plain spinner-or-form conditional. The save flow (`RecipeFormState.isSaveInProgress` → save-button disable + overlay spinner) is left untouched.
- **Modified (top-level ViewModel rule):** `RecipeScreen` and `CookingScreen` — both the async state (`recipeAsync` / `cookingAsync`) and the high-churn interaction state (`uiState`) are collected in the top-level screen composable via `collectAsStateWithLifecycle()`; the `AsyncContent` content lambda passes the collected values + `vm::method` references to `RecipeContent` / `CookingContent` (which keep their exact ViewModel-free signatures); the `Cook` FAB reads `uiState.currentServings` from the top-level collection. **No child composable receives the ViewModel instance** — the earlier plan's `RecipeDetailScreen` / `CookingDetailScreen` wrappers (which required passing `vm` down to scope the `uiState` collection) are dropped, and the `uiState` recomposition coupling is retained as an accepted trade-off.
- **Modified:** `RecipeListViewModel` — drop the redundant single-field `RecipeListState` wrapper; the async payload is `List<RecipeSummary>` directly.
- **Removed:** `data/UiState.kt`, both `LoadingContent` overloads, `SkilletError` objects `UsedLoadedWhereYouShouldntError` and `UsedLoadedWithDataWhereYouShouldntError` (the latter is already unreferenced), and the dead `RecipeViewModel.refresh()`.

## Capabilities

### New Capabilities
- `ui-async-state`: The `Async<T>` state type, the `AsyncContent<T>` full-wrap composable, the Flow-transformer ViewModel pattern (`.map { Async.Success(it) }` / `.catch { emit(Async.Failure(…)) }` / `.stateIn(…, Async.Idle)`; no `combine`/`MutableStateFlow<Async<T>>`/`init`-collect), the busy-flag pattern for form screens, and the top-level ViewModel rule (the ViewModel is used only in the top-level `@Composable` of each screen — state collected there, plain data + `vm::method` lambdas passed down; no child composable ever receives the ViewModel instance).

### Modified Capabilities
- `domain-repository`: no interface change; the ViewModels that observe its `Flow`s change their consumption pattern (no `combine`/`MutableStateFlow<Async<T>>` — state is a `StateFlow` from a `.map`/`.catch`/`.stateIn(WhileSubscribed)` transformer chain).
- The four screens and four ViewModels listed above: state shape and lifecycle change per the What Changes section.

## Impact

- `data/` — `UiState.kt` deleted; `Async.kt` added.
- `ui/ComposeUtils.kt` — `LoadingContent` (both overloads) replaced by `AsyncContent<T>`.
- `ui/viewmodel/RecipeListViewModel.kt`, `RecipeViewModel.kt`, `CookingViewModel.kt` — Mode A rework; `RecipeListState` removed; `refresh()` removed; `originalRecipe` made private.
- `ui/viewmodel/AddEditRecipeViewModel.kt` — Mode B rework (`UiState<Nothing>` → `isInitializing: StateFlow<Boolean>`).
- `ui/viewmodel/RecipeFormState.kt` — unchanged (retains `isSaveInProgress`, `isRecipeSaved`, `tharBeChanges`, `userMessage`).
- `ui/screen/recipe/RecipeScreen.kt`, `ui/screen/cooking/CookingScreen.kt` — `AsyncContent` + top-level collection of both the async and `uiState` state; collected values + `vm::method` lambdas passed to `RecipeContent` / `CookingContent` (no new child composables; no child receives the ViewModel).
- `ui/screen/recipelist/RecipeListScreen.kt` — `AsyncContent` + `List<RecipeSummary>` payload.
- `ui/screen/AddEditRecipeScreen.kt` — remove `LoadingContent` wrap; spinner-or-form conditional on `isInitializing`; save overlay unchanged.
- `model/SkilletError.kt` — remove the two `UsedLoaded*` sentinel objects.
- No changes to `measurement/`, `utils/`, the repository, the database, navigation routes, or any Gradle module. No new tests required (the type is trivially testable; the existing suite is unaffected).
