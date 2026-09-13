package com.kronos.skilletapp.domain.scraping

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onErr
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.model.RecipeScrapeError
import com.kronos.skilletapp.parser.IngredientParser
import com.kronos.skilletapp.scraping.RecipeScraper
import com.kronos.skilletapp.ui.viewmodel.RecipeState
import kotlin.time.Duration

class ScrapeRecipe(
  private val scraper: RecipeScraper,
  private val parser: IngredientParser,
) {
  suspend operator fun invoke(url: String): Result<RecipeState, RecipeScrapeError> {
    return scraper.scrapeRecipe(url).map { scrape ->
      RecipeState(
        name = scrape.recipe.name,
        description = scrape.recipe.description,
        servings = extractServings(scrape.recipe.recipeYield),
        prepTime = scrape.recipe.prepTime.parseMinutes(),
        cookTime = scrape.recipe.cookTime.parseMinutes(),
        source = url,
        sourceName = scrape.website?.name ?: extractSourceName(url),
        ingredients = scrape.recipe.ingredients.map { parser.parseIngredient(text = it) },
        instructions = scrape.recipe.instructions.map { Instruction(text = it.text) },
        tharBeChanges = true,
      )
    }.onErr {
      // Error is already in the Result type; logging handled at UI layer
    }
  }

  private fun extractServings(recipeYield: List<String>): Int {
    val regex = """\d+""".toRegex()
    val yieldEntry = recipeYield.firstOrNull { s -> regex.matches(s) } ?: return 0
    return regex.find(yieldEntry)?.value?.toInt() ?: 0
  }

  private fun String.parseMinutes() = Duration.parseIsoString(this).inWholeMinutes.toInt()

  private fun extractSourceName(url: String): String {
    return """(\w+\.?)+\.\w+""".toRegex().find(url)?.value ?: ""
  }
}
