## 1. Scaling Refactor — Domain Use Case

- [ ] 1.1 Create `ScaledRecipe` data class in `domain/scaling/ScaledRecipe.kt` with `scaledIngredients: List<Ingredient>`, `servings: Int`, `factor: Float` — verify it compiles and has no dependencies on Android or Room
- [ ] 1.2 Create `ScaleRecipe` use case in `domain/scaling/ScaleRecipe.kt` with `operator fun invoke(recipe: Recipe, targetServings: Int): ScaledRecipe` — verify it computes factor as `targetServings.toFloat() / recipe.servings` and scales all ingredient measurements
- [ ] 1.3 Write unit test for `ScaleRecipe` in `app/src/test/` that verifies scaling up (4→8 servings, factor=2.0), scaling down (4→2, factor=0.5), and identity (4→4, factor=1.0) — verify all 3 scenarios pass with `./gradlew :app:testDebugUnitTest` (BLOCKED BY: 1.2)

## 2. Scaling Refactor — UI State

- [ ] 2.1 Refactor `RecipeUiState` in `RecipeViewModel.kt`: drop `scale: Float`, add `currentServings: Int` — derive scale as `currentServings / baseServings.toFloat()` in the composable layer
- [ ] 2.2 Refactor `CookingUiState` in `CookingViewModel.kt`: drop `scale: Float`, add `currentServings: Int` — derive scale in the composable layer
- [ ] 2.3 Update `RecipeViewModel.setScaling()` to accept only `servings: Int` and compute scale internally — verify the method compiles and the ViewModel still loads recipe servings on first display
- [ ] 2.4 Update `CookingViewModel` init to set `currentServings` from `Route.Cooking` args (BLOCKED BY: 4.1) — verify it compiles

## 3. Scaling Refactor — RecipeScreen Controls

- [ ] 3.1 Refactor `ScalingControls` composable in `RecipeScreen.kt`: change `scale: Float` parameter to `currentServings: Int`, derive scale internally as `currentServings / baseServings.toFloat()` — verify the "-" button coerces to minimum 1, "+" increments by 1
- [ ] 3.2 Change preset buttons from "set scale" to "set servings": `onClick` sets `currentServings = baseServings * presetMultiplier` instead of `scale = option` — verify "2x" on a 4-serving recipe sets servings to 8
- [ ] 3.3 Update `onCook` callback in `RecipeScreen` to pass `currentServings` instead of `scale` to `onCook` — verify the Cook FAB passes the correct value
- [ ] 3.4 Update `RecipeContentPreview` composable to use `currentServings` instead of `scale` — verify the preview renders correctly with `KoinPreview`

## 4. Scaling Refactor — Navigation and CookingScreen

- [ ] 4.1 Change `Route.Cooking` in `Route.kt` from `data class Cooking(val recipeId: String, val scale: Float)` to `data class Cooking(val recipeId: String, val currentServings: Int)` — verify the route string builder handles the new param type
- [ ] 4.2 Update `SkilletNavigationActions.navigateToCooking()` signature from `(recipeId: String, scale: Float)` to `(recipeId: String, currentServings: Int)` — verify the call site in `RecipeScreen` passes `currentServings`
- [ ] 4.3 Update `SkilletNavGraph.kt` `Route.Cooking` composable to pass `currentServings` to `CookingViewModel` — verify the nav graph compiles
- [ ] 4.4 Refactor `CookingScreen` and its sub-composables (`OverviewTabContent`, `InstructionTabContent`) to derive `scale` from `currentServings / baseServings.toFloat()` — verify the cooking flow renders correctly

## 5. Repository Interface

- [ ] 5.1 Create `RecipeRepository` interface in `data/RecipeRepository.kt` (or a new `data/RecipeRepositoryInterface.kt`) with all current methods: `fetchRecipe`, `observeRecipe`, `fetchRecipes`, `observeRecipes`, `upsert`, `createRecipe`, `updateRecipe` — verify the interface compiles with correct signatures
- [ ] 5.2 Rename existing `RecipeRepository` class to `RecipeRepositoryImpl` and make it implement `RecipeRepository` — verify all method signatures match the interface
- [ ] 5.3 Update `RecipeRepositoryImpl` constructor to accept `RecipeDao` (not `RecipeDatabase`) — verify it compiles and delegates all calls correctly
- [ ] 5.4 Update all ViewModels to depend on `RecipeRepository` interface instead of concrete class — verify all 4 ViewModels compile

## 6. Koin Wiring — Repository

- [ ] 6.1 Update `SkilletApp.kt` `appModule`: change `singleOf(::RecipeRepository)` to use the `bind` DSL — wire `RecipeRepositoryImpl` bound to `RecipeRepository::class` — verify the Koin module compiles with the compiler plugin
- [ ] 6.2 Verify the Koin module still compiles with the staged koin compiler plugin — run `./gradlew :app:compileDebugKotlin` to confirm

## 7. Validation Use Case

- [ ] 7.1 Create `ValidateRecipe` use case in `domain/validation/ValidateRecipe.kt` with `operator fun invoke(name: String, ingredients: List<Ingredient>, instructions: List<Instruction>, servings: Int, cookTime: Int): Result<Unit, InvalidFormError>` — verify it checks all 5 validation rules (name, ingredients, instructions, servings, cookTime)
- [ ] 7.2 Update `AddEditRecipeViewModel.saveRecipe()` to call `validateRecipe()` use case instead of the private `validateForm()` method — verify the validation behavior is identical (same error messages, same conditions)
- [ ] 7.3 Write unit test for `ValidateRecipe` covering all 5 failure scenarios and the success path — verify all tests pass with `./gradlew :app:testDebugUnitTest` (BLOCKED BY: 7.1)

## 8. Scraping Use Case

- [ ] 8.1 Create `ScrapeRecipe` use case in `domain/scraping/ScrapeRecipe.kt` with `operator fun invoke(url: String, scraper: RecipeScraper, parser: IngredientParser): Result<ScrapedRecipe, ScrapeError>` — verify it orchestrates the scraper → parser → ScrapedRecipe pipeline (domain type, not ViewModel type)
- [ ] 8.2 Update `AddEditRecipeViewModel.scrapeRecipe()` to delegate to `ScrapeRecipe` use case and convert `ScrapedRecipe` to `RecipeState` using new conversion logic (see 8.4) — verify the scraped data populates `RecipeState` identically to the current implementation
- [ ] 8.3 Write unit test for `ScrapeRecipe` with mocked `RecipeScraper` and `IngredientParser` — verify it can be run as a pure JVM test (no Android instrumentation) with `./gradlew :app:testDebugUnitTest` (BLOCKED BY: 8.1)
- [ ] 8.4 Add `ScrapedRecipe.toRecipeState()` extension or conversion function in `AddEditRecipeViewModel.kt` (or a dedicated `domain/scraping/ScrapedRecipeMapper.kt`) that maps `ScrapedRecipe` fields into `RecipeState` — verify it produces identical `RecipeState` to the current scraping flow

## 9. Koin Wiring — Use Cases

- [ ] 9.1 Register `ScaleRecipe`, `ValidateRecipe`, and `ScrapeRecipe` in `appModule` using `singleOf(::ScaleRecipe)`, `singleOf(::ValidateRecipe)`, `singleOf(::ScrapeRecipe)` — verify Koin module compiles with the compiler plugin
- [ ] 9.2 Inject use cases into `AddEditRecipeViewModel` via constructor — verify DI resolves all dependencies correctly
- [ ] 9.3 Verify the full app compiles — run `./gradlew :app:assembleDebug` and confirm no compilation errors

## 10. Verification and Cleanup

- [ ] 10.1 Run all unit tests — `./gradlew :app:testDebugUnitTest` — verify no regressions
- [ ] 10.2 Run lint — `./gradlew :app:lint` — verify no new lint warnings
- [ ] 10.3 Verify the Koin compiler plugin staged changes are not overwritten — check that `app/build.gradle.kts` still has `alias(libs.plugins.koin.compiler)`, `build.gradle.kts` still has `alias(libs.plugins.koin.compiler) apply false`, and `SkilletApp.kt` has explicit Koin imports (not wildcard)
- [ ] 10.4 Verify scaling behavior end-to-end: open recipe, tap preset buttons, tap +/-, verify servings display correctly and scale is derived — no floating-point drift visible in UI
