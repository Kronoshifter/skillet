package com.kronos.skilletapp.domain.scraping

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onErr
import com.kronos.skilletapp.model.RecipeScrapeError
import com.kronos.skilletapp.scraping.RecipeScraper
import kotlin.time.Duration

class ScrapeRecipe(
  private val scraper: RecipeScraper,
) {
  suspend operator fun invoke(url: String): Result<ScrapedRecipe, RecipeScrapeError> {
    return scraper.scrapeRecipe(url).map { scrape ->
      ScrapedRecipe(
        name = scrape.recipe.name,
        description = scrape.recipe.description,
        servings = extractServings(scrape.recipe.recipeYield),
        prepTimeMinutes = scrape.recipe.prepTime.parseMinutes(),
        cookTimeMinutes = scrape.recipe.cookTime.parseMinutes(),
        sourceUrl = url,
        sourceName = scrape.website?.name ?: extractSourceName(url),
        ingredients = scrape.recipe.ingredients,
        instructions = scrape.recipe.instructions.map { it.text },
      )
    }.onErr {
      // Error is already in the Result type; logging handled at UI layer
    }
  }

  private fun extractServings(recipeYield: List<String>): Int? {
    val regex = """\d+""".toRegex()
    val yieldEntry = recipeYield.firstOrNull { s -> regex.matches(s) } ?: return null
    return regex.find(yieldEntry)?.value?.toInt()
  }

  private fun String.parseMinutes(): Int = Duration.parseIsoString(this).inWholeMinutes.toInt()

  private fun extractSourceName(url: String): String {
    return """(\w+\.?)+\.\w+""".toRegex().find(url)?.value ?: ""
  }
}
