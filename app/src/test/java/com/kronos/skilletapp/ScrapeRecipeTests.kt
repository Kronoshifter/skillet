package com.kronos.skilletapp

import com.github.michaelbull.result.unwrap
import com.kronos.skilletapp.domain.scraping.ScrapeRecipe
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.model.RecipeScrapeError
import com.kronos.skilletapp.parser.IngredientParser
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
        val parser = IngredientParser()
        val useCase = ScrapeRecipe(mock, parser)

        val result = useCase("https://example.com/pancakes")
        val state = result.unwrap()

        state.name shouldBe "Mock Pancakes"
        state.description shouldBe "Mock description"
        state.servings shouldBe 4
        state.source shouldBe "https://example.com/pancakes"
        state.ingredients.size shouldBe 3
        state.instructions.size shouldBe 2
      }
    }
  })
