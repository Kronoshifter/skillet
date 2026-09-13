package com.kronos.skilletapp.scraping

import com.github.michaelbull.result.Result
import com.kronos.skilletapp.model.RecipeScrapeError

interface RecipeScraper {
  suspend fun scrapeRecipe(url: String): Result<RecipeScrape, RecipeScrapeError>
}
