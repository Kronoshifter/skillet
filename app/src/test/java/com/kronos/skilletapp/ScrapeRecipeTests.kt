package com.kronos.skilletapp

import com.github.michaelbull.result.unwrap
import com.kronos.skilletapp.domain.scraping.ScrapeRecipe
import com.kronos.skilletapp.model.RecipeScrapeError
import com.kronos.skilletapp.scraping.InstructionHtml
import com.kronos.skilletapp.scraping.RecipeHtml
import com.kronos.skilletapp.scraping.RecipeScrape
import com.kronos.skilletapp.scraping.RecipeScraper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ScrapeRecipeTests :
  FunSpec({
    context("ScrapeRecipe Tests") {
      // Mock scraper that returns test data
      class MockScraper : RecipeScraper {
        override suspend fun scrapeRecipe(url: String): com.github.michaelbull.result.Result<RecipeScrape, RecipeScrapeError> {
          return com.github.michaelbull.result.Ok(
            RecipeScrape(
              recipe =
                RecipeHtml(
                  name = "Mock Pancakes",
                  description = "Mock description",
                  ingredients = listOf("1 cup flour", "2 eggs", "1/2 cup milk"),
                  instructions = listOf(InstructionHtml("Mix ingredients"), InstructionHtml("Cook on griddle")),
                  prepTime = "PT5M",
                  cookTime = "PT10M",
                  recipeYield = listOf("4"),
                ),
              website = null,
            )
          )
        }
      }

      test("ScrapeRecipe with mocked scraper returns parsed data") {
        val mock = MockScraper()
        val useCase = ScrapeRecipe(mock)

        val result = useCase("https://example.com/pancakes")
        val scraped = result.unwrap()

        scraped.name shouldBe "Mock Pancakes"
        scraped.description shouldBe "Mock description"
        scraped.servings shouldBe 4
        scraped.sourceUrl shouldBe "https://example.com/pancakes"
        scraped.ingredients.size shouldBe 3
        scraped.instructions.size shouldBe 2
      }
    }
  })
