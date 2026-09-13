## Purpose

Extracts recipe scraping orchestration from the ViewModel into a domain use case, making scraping logic testable without Android instrumentation.

## ADDED Requirements

### Requirement: ScrapedRecipe SHALL be a domain type representing scraped recipe data

The `ScrapedRecipe` type SHALL be a domain data class (not a ViewModel type) containing the fields produced by scraping. It is an intermediate result between the scraper and the ViewModel.

#### Scenario: ScrapedRecipe contains scraped fields
- **WHEN** `ScrapeRecipe` produces a `ScrapedRecipe`
- **THEN** it contains: `name: String`, `description: String?`, `servings: Int?`, `prepTimeMinutes: Int?`, `cookTimeMinutes: Int?`, `sourceUrl: String`, `sourceName: String?`, `ingredients: List<String>` (raw strings), `instructions: List<String>` (raw strings)

### Requirement: ScrapeRecipe use case SHALL orchestrate scraping pipeline

The `ScrapeRecipe` use case SHALL accept a URL, invoke `RecipeScraper.scrapeRecipe(url)`, parse the result using `IngredientParser`, and return a `ScrapedRecipe` (domain type) ready for the ViewModel to convert into `RecipeState`.

#### Scenario: Valid URL produces a ScrapedRecipe
- **WHEN** `ScrapeRecipe` receives a valid recipe URL and the scraper succeeds
- **THEN** it returns a `ScrapedRecipe` with name, description, servings, prepTime, cookTime, sourceUrl, sourceName, ingredients (raw strings), and instructions (raw strings) populated

#### Scenario: Invalid URL returns error state
- **WHEN** `ScrapeRecipe` receives a URL and the scraper fails
- **THEN** it returns an error result with a user-facing message

#### Scenario: Scraped ingredients are parsed through IngredientParser
- **WHEN** the scraper returns raw ingredient strings
- **THEN** `ScrapeRecipe` passes each through `IngredientParser.parseIngredient()` before including in the result

### Requirement: ScrapeRecipe SHALL be testable without Android

The `ScrapeRecipe` use case SHALL depend only on interfaces or pure classes (`RecipeScraper`, `IngredientParser`), not on Android framework classes.

#### Scenario: Use case can be unit tested
- **WHEN** `ScrapeRecipe` is instantiated with mockable dependencies
- **THEN** it can be invoked and its `Result<ScrapedRecipe, ScrapeError>` asserted in a pure JVM unit test

### Requirement: ViewModel SHALL convert ScrapedRecipe to RecipeState

The `AddEditRecipeViewModel` SHALL convert a `ScrapedRecipe` into `RecipeState` for UI consumption, mapping domain fields to ViewModel state fields.

#### Scenario: ScrapedRecipe converts to RecipeState
- **WHEN** the ViewModel receives a `ScrapedRecipe` from `ScrapeRecipe`
- **THEN** it maps `ScrapedRecipe` fields into `RecipeState`, including parsed ingredients and instructions
