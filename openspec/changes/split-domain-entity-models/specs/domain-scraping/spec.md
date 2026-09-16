## ADDED Requirements

### Requirement: The domain layer SHALL NOT depend on UI types

Types in the domain package SHALL NOT import or return any type from the `ui` package. `ScrapeRecipe` SHALL return the domain `ScrapedRecipe` type rather than a UI form-state type.

#### Scenario: ScrapeRecipe returns a domain type
- **WHEN** `ScrapeRecipe` succeeds for a valid URL
- **THEN** it returns a `ScrapedRecipe` and does not reference any type from the `ui` package

#### Scenario: Domain source has no ui imports
- **WHEN** the domain package sources are inspected
- **THEN** no domain file imports a symbol from `com.kronos.skilletapp.ui`

## MODIFIED Requirements

### Requirement: ScrapeRecipe use case SHALL orchestrate scraping pipeline

The `ScrapeRecipe` use case SHALL accept a URL, invoke `RecipeScraper.scrapeRecipe(url)`, and return a `ScrapedRecipe` (domain type) containing the raw scraped fields, ready for the ViewModel to parse and convert into its form-state type. `ScrapeRecipe` SHALL NOT parse ingredients; ingredient parsing is a ViewModel concern.

#### Scenario: Valid URL produces a ScrapedRecipe
- **WHEN** `ScrapeRecipe` receives a valid recipe URL and the scraper succeeds
- **THEN** it returns a `ScrapedRecipe` with name, description, servings, prepTime, cookTime, sourceUrl, sourceName, ingredients (raw strings), and instructions (raw strings) populated

#### Scenario: Invalid URL returns error state
- **WHEN** `ScrapeRecipe` receives a URL and the scraper fails
- **THEN** it returns an error result with a user-facing message

#### Scenario: Scraped ingredients are parsed through IngredientParser
- **WHEN** raw ingredient strings are produced by scraping
- **THEN** they are parsed via `IngredientParser.parseIngredient()` in the ViewModel into the form's `Ingredient` list, while `ScrapeRecipe` returns them as raw strings

### Requirement: ViewModel SHALL convert ScrapedRecipe to RecipeFormState

The `AddEditRecipeViewModel` SHALL convert a `ScrapedRecipe` into its UI form-state type for UI consumption, mapping domain fields to form-state fields. The form-state type SHALL be `RecipeFormState`, a standalone type (not nested in the ViewModel file), and the ViewModel SHALL parse the raw ingredient strings via `IngredientParser` when populating the form's ingredient list.

#### Scenario: ScrapedRecipe converts to RecipeFormState
- **WHEN** the ViewModel receives a `ScrapedRecipe` from `ScrapeRecipe`
- **THEN** it maps `ScrapedRecipe` fields into `RecipeFormState`, parsing the raw ingredient strings into the form's `Ingredient` list and mapping the instructions
