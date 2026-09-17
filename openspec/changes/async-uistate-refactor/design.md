## Context

Today the async data lifecycle in the UI layer is modeled by a sealed `UiState<out T>` (`data/UiState.kt`):

```kotlin
sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data object Loaded : UiState<Nothing>
  data class LoadedWithData<T>(val data: T) : UiState<T>
  data class Error(val error: SkilletError) : UiState<Nothing>
}
```

It is consumed two ways:

- **Mode A (data screens)** — `RecipeListViewModel`, `RecipeViewModel`, `CookingViewModel` build
  `combine(_isLoading, dataFlow.map { UiState.LoadedWithData(it) }.catch<UiState<T>> { … })` →
  `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)`, and the screen renders
  `LoadingContent(state = recipeState) { data -> … }`.
- **Mode B (form screen)** — `AddEditRecipeViewModel` holds `MutableStateFlow<UiState<Nothing>>` cycling
  `Loaded` ↔ `Loading` around `loadRecipe()`/`scUrl()`, and the screen wraps the whole form in
  `LoadingContent(state = uiState) { AddEditRecipeContent(…) }`.

`ui/ComposeUtils.kt` provides two `LoadingContent` overloads (a generic `<T>` and a `UiState<Nothing>` one),
both `AnimatedContent(targetState = state) { … when (targetState) … }`, with the "illegal" branch
throwing `IllegalStateException`.

The problems, verified against the source:

1. `Loaded` is ambiguous — "idle/success" on the form screen, but on a data screen the `when` reaches
   `UiState.Loaded -> throw IllegalStateException` (ComposeUtils.kt:70) and `LoadedWithData` is the *only*
   data-carrying state, so `T` is erased in the other three.
2. Every Mode A VM carries a `_isLoading = MutableStateFlow(false)` that is never set `true` (except
   `RecipeViewModel.refresh()`, which is dead code — no call site). It exists only to satisfy the `combine`
   slot. The `catch<UiState<T>>` operator (ComposeUtils.kt pipeline) is a non-idiomatic exception carrier
   keyed on the type parameter.
3. `UsedLoadedWhereYouShouldntError` / `UsedLoadedWithDataWhereYouShouldntError` (`model/SkilletError.kt:12,15`)
   exist solely to make the `when` exhaustive; the second is already referenced nowhere.
4. **Recomposition coupling** — in `RecipeScreen` (RecipeScreen.kt:103-104) and `CookingScreen`
   (CookingScreen.kt:62-63) both `recipeState` (async) and `uiState` (interaction) are collected at the top.
   `uiState` (selectedUnits/currentServings/scaledIngredients) changes on every unit selection and scaling
   change, and it is captured inside the `LoadingContent { recipe -> RecipeContent(… uiState …) }` lambda, so
   each interaction re-executes the `AnimatedContent` and re-composes the content subtree.
5. **Mode B state loss** — `AddEditRecipeScreen.kt:139` wraps the 1750-line form in `LoadingContent`; while
   `uiState == Loading` (initial load/scrape) the form is replaced by a spinner, discarding scroll/focus/IME.
   (Save already uses a separate, correct mechanism — `RecipeFormState.isSaveInProgress` → save-button disable
   + overlay spinner at AddEditRecipeScreen.kt:205-215 — which does *not* destroy the form.)

Constraints: only `:app` is touched; the repository/database, `measurement/`, `utils/`, and navigation routes
are unchanged; no new Gradle module; the existing test suite is unaffected (no test references `UiState`,
`LoadingContent`, or `RecipeListState`). See `openspec/speculation.md` for the originating rationale.

## Goals / Non-Goals

**Goals:**
- Replace `UiState<T>` with a sealed `Async<T>` that is exhaustive, carries `T` only in `Success`, and exposes `data`/`error`/`isLoading` accessors.
- Collapse the Mode A pipeline to a Flow transformer chain (`.map` / `.catch` / `.stateIn`) (no `combine`, no `MutableStateFlow<Async<T>>`, no `init`-collect, no `catch<UiState<T>>`, no dead `_isLoading`).
- Replace both `LoadingContent` overloads with one `AsyncContent<T>` composable.
- Break the recomposition coupling in `RecipeScreen`/`CookingScreen` by scoping `uiState` collection into a child composable.
- Remove the Mode B full-wrap spinner (replace with a busy flag + conditional); keep the save flow untouched.
- Delete the now-dead sentinels, `UiState.kt`, `RecipeListState`, and `RecipeViewModel.refresh()`.

**Non-Goals:**
- No changes to `measurement/`, `utils/`, `:app` Gradle config, the repository, the database, or navigation.
- No new Gradle module. No new unit tests required (optional; the type is trivially testable).
- No change to `RecipeFormState`'s field set or to the save/scrape business logic.
- No fix for the pre-existing latent `isSaveInProgress` reset gap (documented in Risks, deliberately preserved).

## Decisions

**1. `Async<T>` sealed interface with accessors — no companion; construct the sealed states directly.**

```kotlin
// data/Async.kt
package com.kronos.skilletapp.data

import com.kronos.skilletapp.model.SkilletError

sealed interface Async<out T> {
  val data: T? get() = null
  val error: SkilletError? get() = null
  val isLoading: Boolean get() = false

  data object Idle : Async<Nothing>
  data object Loading : Async<Nothing> { override val isLoading: Boolean get() = true }
  data class Success<out T>(override val data: T) : Async<T>
  data class Failure(override val error: SkilletError) : Async<Nothing>
}
```

- `Success<T>` is the only data-carrying state; `T` is never erased elsewhere (fixes `LoadedWithData`'s erasure).
- `Idle` (haven't started) is distinct from `Loading` (fetching) per the objective.
- **No companion object:** construct the states directly — `Async.Idle`, `Async.Loading`, `Async.Success(data)`, `Async.Failure(error)`. A `when` over `Async<T>` already references the sealed subtypes by name, so a parallel `idle()`/`success()`/… factory API is redundant.
- Rejected: a companion of factory functions (`idle()`/`loading()`/`success()`/`failure()`) — a second, redundant construction surface for values that are already reachable as `Async.Idle`/`Async.Loading`/`Async.Success(…)`/`Async.Failure(…)`.
- Rejected: a `data class Async<T>(val data, val error, val isLoading)` triple — loses exhaustive matching and lets illegal states be constructed.
- Rejected: a raw `Flow` without `stateIn` — we *want* a `StateFlow` so back-navigation retains the last value and there is no resubscription churn; the Mode A pipeline therefore ends in `.stateIn(…)` (see Decision 3).

**2. `AsyncContent<T>` replaces both `LoadingContent` overloads; `AnimatedContent` is retained; `Idle` renders a spinner by default.**

```kotlin
// ui/ComposeUtils.kt
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> AsyncContent(
  state: Async<T>,
  modifier: Modifier = Modifier,
  idle: (@Composable () -> Unit)? = null,
  loading: (@Composable () -> Unit)? = null,
  error: (@Composable (SkilletError) -> Unit)? = null,
  content: @Composable (data: T) -> Unit,
) {
  AnimatedContent(
    targetState = state,
    label = "Async",
    modifier = Modifier.fillMaxSize(),
  ) { targetState ->
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().then(modifier)) {
      when (targetState) {
        Async.Idle -> (idle ?: { CircularProgressIndicator() })()
        Async.Loading -> (loading ?: { CircularProgressIndicator() })()
        is Async.Failure -> (error ?: { err -> Text(err.message) })(targetState.error)
        is Async.Success -> content(targetState.data)
      }
    }
  }
}
```

- Keeps `AnimatedContent` (spec: "The `AnimatedContent` wrapper is untouched") so loading→content transitions still animate.
- **Idle-flash decision (resolves Edge Case 1):** data screens initialize their flow to `Async.Idle`. Because
  `AsyncContent` renders `Idle` with the *same spinner* as `Loading` by default (the `idle` slot defaults to the
  loading spinner), a cold start shows a spinner (not a blank) until the first `Success`. This preserves the
  `Idle`/`Loading` distinction semantically while eliminating the blank-flash. The `idle` slot stays overridable.
- Default `error` slot renders `Text(error.message)`, matching the old `LoadingContent` default.
- Both old `LoadingContent` overloads are removed (the `<T>` and the `UiState<Nothing>` form). Mode B no longer
  needs a composable for its spinner (see Decision 5), so only `AsyncContent` remains.

**3. Mode A ViewModels: Flow transformer chain (`.map` / `.catch` / `.stateIn`); no `MutableStateFlow<Async<T>>`, no `init`-collect.**

`RecipeViewModel` (representative; `CookingViewModel` is identical in shape, seeded from `args.currentServings`):

```kotlin
class RecipeViewModel(
  private val recipeRepository: RecipeRepository,
  private val handle: SavedStateHandle,
  private val scaleRecipe: ScaleRecipe,
) : ViewModel() {
  private val args = handle.toRoute<Route.Recipe>()
  private val recipeId = args.recipeId

  private var originalRecipe: Recipe? = null            // private; not observable state

  private val _uiState = MutableStateFlow(RecipeUiState())
  val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

  val recipeAsync: StateFlow<Async<Recipe>> =
    recipeRepository
      .observeRecipe(recipeId)
      .onEach { recipe ->                               // one-time scaling baseline (first emission only)
        if (originalRecipe == null) {
          originalRecipe = recipe
          _uiState.update {
            it.copy(
              currentServings = recipe.servings,
              scaledIngredients = scaleRecipe(recipe, recipe.servings).scaledIngredients,
            )
          }
        }
      }
      .map { Async.Success(it) }
      .catch { emit(Async.Failure(RecipeCouldNotBeLoadedError("Could not load recipe"))) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), Async.Idle)

  fun selectUnit(ingredient: Ingredient, unit: MeasurementUnit?) {
    _uiState.update { it.copy(selectedUnits = it.selectedUnits + (ingredient to unit)) }
  }

  fun setScaling(servings: Int) {
    val original = originalRecipe ?: return
    _uiState.update { it.copy(currentServings = servings, scaledIngredients = scaleRecipe(original, servings).scaledIngredients) }
  }
}
```

- `RecipeUiState` loses `originalRecipe` (now a private field) and keeps `selectedUnits`, `currentServings`,
  `scaledIngredients`. `RecipeListState` is deleted; the list payload is `List<RecipeSummary>`.
- **Property naming:** `recipeAsync` (not `recipeState` or `uiState`). The `*Async` suffix signals the property
  is an `Async<T>`-typed flow, distinct from interaction state (`uiState`).
- **`.onEach` one-time baseline:** `originalRecipe` is set on the first emission via
  `.onEach { if (originalRecipe == null) … }`. This is impure (a side-effect in the transformer chain) but safe
  because `originalRecipe` is never reset to `null`, so the guard makes the assignment effectively one-time. The
  alternative — a separate `launch`/`collect` for the baseline — would reintroduce the `init`-collect pattern that
  was rejected. (There is no built-in `onFirst` operator in kotlinx.coroutines; `onEach` + null guard is the
  idiomatic equivalent.)
- **`.stateIn(WhileSubscribed(5000L), Async.Idle)`:** the pipeline ends in `stateIn` so the property is a
  `StateFlow` that retains the last emission across navigation. The upstream `Flow` is only actively collected
  while subscribed (+5 s grace), but the `StateFlow` value persists for the ViewModel's lifetime. Initial value
  is `Async.Idle`.
- **`.catch { emit(…) }` (resolves Edge Case 4):** the old `catch<UiState<T>>` is gone; the standard kotlinx `catch`
  operator's handler is a `FlowCollector` suspend lambda, so it must `emit` the replacement value:
  `.catch { emit(Async.Failure(…)) }`. The failure terminates the flow, preserving the old "error sticks" behavior.
- `setScaling`/`selectUnit` are unchanged except they read the private `originalRecipe` instead of
  `_uiState.value.originalRecipe` (same null-guard semantics).
- `refresh()` is **deleted** (dead code: sets `_isLoading` which no longer exists and has no call site; its body
  was already a commented-out no-op).

**4. `originalRecipe` becomes private; screens derive `originalIngredients` from the async payload.**

The async payload **is** the original (unscaled, as-stored) `Recipe`. `observeRecipe` returns the DB recipe;
scaling only mutates in-memory `uiState.scaledIngredients`. Therefore the screen's `originalIngredients` can be
`recipe.ingredients` (the payload passed into `RecipeContent`/`CookingContent`), so `originalRecipe` no longer
needs to be observable state. This removes a field from `RecipeUiState`/`CookingUiState` and eliminates the
`uiState.originalRecipe?.ingredients ?: emptyList()` idiom in both screens.
- Verified: today `uiState.originalRecipe` is set to the same `recipe` object that populates `recipeAsync`, so
   `originalIngredients = recipe.ingredients` is behavior-preserving.

**5. Mode B (`AddEditRecipeViewModel`): replace `UiState<Nothing>` with `isInitializing: StateFlow<Boolean>`; leave the save flow alone.**

The `UiState<Nothing>` (`_uiState`) drives **only** the initial load/scrape spinner (`loadRecipe`/`scUrl`), set
`Loading` at start and `Loaded` at end. Save is a **separate** mechanism — `RecipeFormState.isSaveInProgress`
(set in `saveRecipe`, read to disable the save button at AddEditRecipeScreen.kt:129 and to show an overlay
spinner at AddEditRecipeScreen.kt:205-215) — which already works without destroying the form. (The spec's
Mode B example wrapped `saveRecipe` in an `isSaving` flag; in the real code that role is already filled by
`isSaveInProgress`. We therefore do not move save logic.)

```kotlin
private val _isInitializing = MutableStateFlow(false)
val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

private fun loadRecipe(id: String) {
  _isInitializing.update { true }
  viewModelScope.launch {
    // … fetch + populate _recipeState (unchanged) …
    _isInitializing.update { false }
  }
}
fun scUrl(url: String) {
  _isInitializing.update { true }
  viewModelScope.launch {
    // … scrape + populate _recipeState (unchanged) …
    _isInitializing.update { false }
  }
}
```

- `saveRecipe`, `createRecipe`, `updateRecipe`, and all `RecipeFormState` updates are **untouched**.
- The `_uiState`/`uiState` fields are removed; `isInitializing` (initial `false`, so the form shows immediately
  for the new-recipe path where `init` does nothing) preserves the current "form visible until a load/scrape
  starts" behavior.
- Screen: replace `LoadingContent(state = uiState) { … }` with a plain conditional (no animation needed — this
  spinner only appears on first load/scrape):

```kotlin
val isInitializing by vm.isInitializing.collectAsStateWithLifecycle()
if (isInitializing) {
  Box(Modifier.fillMaxSize().padding(paddingValues).align(Alignment.Center)) { CircularProgressIndicator() }
} else {
  AddEditRecipeContent(… /*unchanged*/ )
  LaunchedEffect(recipeState.isRecipeSaved) { … }        // unchanged
  recipeState.userMessage?.let { … }                      // unchanged
}
```

The save overlay (AddEditRecipeScreen.kt:205-215) and the save-button disable (line 129) are unchanged.

**6. Recomposition scoping in `RecipeScreen` and `CookingScreen`.**

`RecipeScreen` (top level reads only the async state; interaction state moved into a child):

```kotlin
@Composable
fun RecipeScreen(onBack: () -> Unit, onEdit: () -> Unit, onCook: (Int) -> Unit, vm: RecipeViewModel = koinViewModel()) {
  val recipeAsync by vm.recipeAsync.collectAsStateWithLifecycle()   // ONLY async at top
  // … BackHandler, pagerState, fabTransition, isFabExpanded, scrollBehavior (unchanged) …
  Scaffold(
    topBar = { … },
    floatingActionButton = {
      fabTransition.AnimatedVisibility(…) {
        ExtendedFloatingActionButton(
          text = { Text("Cook") },
          icon = { … },
          onClick = { onCook(vm.uiState.value.currentServings) },   // lazy read; no Compose subscription
          expanded = isFabExpanded,
        )
      }
    },
  ) { paddingValues ->
    AsyncContent(state = recipeAsync, modifier = Modifier.fillMaxSize().padding(paddingValues)) { recipe ->
      RecipeDetailScreen(recipe = recipe, vm = vm,
        pagerState = pagerState, ingredientListState = ingredientListState,
        instructionsListState = instructionsListState, topAppBarScrollBehavior = scrollBehavior)
    }
  }
}

@Composable
private fun RecipeDetailScreen(
  recipe: Recipe, vm: RecipeViewModel,
  pagerState: PagerState, ingredientListState: LazyListState,
  instructionsListState: LazyListState, topAppBarScrollBehavior: TopAppBarScrollBehavior,
) {
  val uiState by vm.uiState.collectAsStateWithLifecycle()   // scoped: only this subtree re-composes
  RecipeContent(
    recipe = recipe,
    currentServings = uiState.currentServings,
    selectedUnits = uiState.selectedUnits,
    onScalingChanged = vm::setScaling,
    onUnitSelect = vm::selectUnit,
    scaledIngredients = uiState.scaledIngredients,
    originalIngredients = recipe.ingredients,               // from payload, not uiState
    pagerState = pagerState,
    ingredientListState = ingredientListState,
    instructionsListState = instructionsListState,
    topAppBarScrollBehavior = topAppBarScrollBehavior,
    modifier = Modifier.fillMaxSize(),
  )
}
```

- `uiState` is now collected **only** inside `RecipeDetailScreen`, so `selectedUnits`/`currentServings`
  changes invalidate that subtree (and below) but NOT `RecipeScreen` or the `AsyncContent`/`AnimatedContent`
  wrapper.
- The FAB reads `vm.uiState.value.currentServings` **at click time** (a snapshot read of the `StateFlow`, not a
  Compose subscription) so the top level does not re-compose on every interaction. The FAB label is static
  ("Cook") and its behavior only depends on the *current* servings, so a lazy read is correct.
- `RecipeContent` keeps its exact signature (it is previewed with a Koin-provided `Recipe`); `RecipeDetailScreen`
  is a thin new wrapper that owns the `uiState` collection.
- `CookingScreen` follows the same pattern: top level keeps `cookingAsync`, wraps in `AsyncContent`, and defers to
  a new `CookingDetailScreen(recipe, vm, onBack)` which collects `uiState` (scoped) and builds the `Scaffold`
  (moved out of the old `LoadingContent` lambda) + `CookingContent(…, originalIngredients = recipe.ingredients, …)`.
  `CookingContent` keeps its exact signature (it is previewed).

**7. `RecipeListViewModel` payload is `List<RecipeSummary>`; `RecipeListState` deleted.**

```kotlin
val recipeListAsync: StateFlow<Async<List<RecipeSummary>>> =
  recipeRepository
    .observeRecipeSummaries()
    .distinctUntilChanged()
    .map { Async.Success(it) }
    .catch { emit(Async.Failure(RecipeCouldNotBeLoadedError("Could not load recipes"))) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), Async.Idle)
```

- `data class RecipeListState(val recipes: List<RecipeSummary>)` is deleted (single-field wrapper, no value).
- `RecipeListScreen` collects `vm.recipeListAsync`, wraps in `AsyncContent`, and passes `recipes` (the `List`)
  straight to `RecipeListContent(recipes = …)` instead of `data.recipes`.
- `sharedRecipe`, `showSharedUrl`, `_savedSortType`, and the SpeedDial/bottom-sheet UI are unchanged.

## Risks / Trade-offs

- [Pre-existing latent bug: `RecipeFormState.isSaveInProgress` is set `true` (AddEditRecipeViewModel.kt:71) but
  never reset to `false`. It is masked today because a successful save sets `isRecipeSaved` → the screen navigates
  away before the stuck flag matters.] → **Preserved, not fixed** (out of scope; the refactor must not change save
  behavior). Flagged here so it is not mistaken for a regression. A follow-up may add the reset in `finally`.
- [`.catch { }` only catches the *first* failure (a terminated Room `Flow` does not re-emit).]
  → Matches today's `catch` behavior (error sticks). Not a regression.
- [`stateIn(WhileSubscribed(5000L))` stops the *upstream* 5 s after the last subscriber leaves, but the
  `StateFlow` value persists for the ViewModel's lifetime.] → The last emission (success or failure) survives
  navigation, so re-entering a screen does not flash a fresh idle placeholder. This is the same retention the old
  pipeline's `stateIn(WhileSubscribed)` provided.
- [FAB lazy `vm.uiState.value.currentServings` read.] → Correct because the click only needs the current value;
  if `setScaling` were animated into the button label this would need to become a subscription (it is not).
- [`AsyncContent` adds an `Idle` arm; any screen initializing to `Idle` shows a spinner (not blank).] → Intended
  (Edge Case 1 resolution); if a screen wants a distinct idle placeholder it passes the `idle` slot.
- [Two `SkilletError` sentinels removed; `UsedLoadedWithDataWhereYouShouldntError` is already unreferenced.]
  → `UsedLoadedWhereYouShouldntError`'s three uses are in the exact `when` arms being deleted, so removing both
  after the VMs migrate is safe.

## Migration Plan

1. **Additive type + composable (no breakage):** add `data/Async.kt`; add `AsyncContent<T>` to `ui/ComposeUtils.kt`
   *alongside* the existing `LoadingContent` overloads (old code still compiles).
2. **Mode A VMs one at a time:** `RecipeListViewModel` → `RecipeViewModel` → `CookingViewModel` (each: drop
   `_isLoading`/`combine`/`catch<UiState<T>>`/`MutableStateFlow<Async<T>>`; build the `*Async` property as a Flow
   transformer chain (`.map { Async.Success(it) }` / `.catch { emit(Async.Failure(…)) }` / `.stateIn(…, Async.Idle)`); make
   `originalRecipe` private, delete `refresh()` on `RecipeViewModel`). After each, the matching screen is
   updated so the module compiles.
3. **Mode A screens one at a time (matching order):** `RecipeListScreen` → `RecipeScreen` → `CookingScreen`
   (`AsyncContent` + child-composable scoping for Recipe/Cooking).
4. **Mode B:** `AddEditRecipeViewModel` (`UiState<Nothing>` → `isInitializing`) + `AddEditRecipeScreen`
   (remove `LoadingContent` wrap, conditional spinner). Save flow untouched.
5. **Cleanup:** delete `data/UiState.kt`, remove both `LoadingContent` overloads from `ui/ComposeUtils.kt`, remove the
   `companion object` from `data/Async.kt` (already committed; switch all consumers to `Async.Idle`/`Async.Success(…)`/
   `Async.Failure(…)` directly), and remove `UsedLoadedWhereYouShouldntError` and `UsedLoadedWithDataWhereYouShouldntError`
   from `model/SkilletError.kt`.
6. **Gate:** `./gradlew :app:assembleDebug`, `./gradlew test`, `./gradlew :app:lint`, and
   `rg "UiState|LoadingContent|UsedLoaded|RecipeListState" app/src` returns nothing.

Rollback: revert the code; no persisted-state or schema change is involved.

## Open Questions

- None blocking. All spec-vs-code divergences are resolved: (a) both `UsedLoaded*` sentinels are removed (the
  second is already dead); (b) `RecipeListState` is dropped (single-field wrapper); (c) Mode B's `UiState<Nothing>`
  is the load/scrape spinner (not save) and is replaced by `isInitializing`, while the save flow
  (`isSaveInProgress`) is left as-is; (d) `originalRecipe` is made private and screens use `recipe.ingredients`;
   (e) `AsyncContent` keeps `AnimatedContent` and renders `Idle` as a spinner by default to avoid a blank flash.
