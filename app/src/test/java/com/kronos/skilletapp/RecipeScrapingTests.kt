package com.kronos.skilletapp

import android.R.attr.type
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.andThenRecover
import com.github.michaelbull.result.recoverCatching
import com.github.michaelbull.result.runCatching
import com.github.michaelbull.result.unwrap
import com.kronos.skilletapp.parser.IngredientParser
import com.kronos.skilletapp.scraping.RecipeHtml
import com.kronos.skilletapp.scraping.RecipeScraper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.skrape.core.document
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.script
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class RecipeScrapingTests : FunSpec({
  context("Scraping") {
    val scraper = RecipeScraper(IngredientParser())
    val recipeUrl = "https://www.allrecipes.com/recipe/21014/good-old-fashioned-pancakes/"

    test("Retrieve JSON-LD") {
      val extracted = extractJsonLd(recipeUrl)

      extracted shouldNotBe null
      extracted shouldNotBe ""
    }


    test("Deserialize JSON-LD") {
      val format = Json { ignoreUnknownKeys = true }

      val extracted = extractJsonLd(recipeUrl)
      val json = runCatching {
        format.decodeFromString<RecipeHtml>(extracted)
      }.recoverCatching {
        format.decodeFromString<List<RecipeHtml>>(extracted).first()
      }.unwrap()

      json shouldNotBe null
      json.name shouldBe "Good Old-Fashioned Pancakes"
    }
  }
})

private suspend fun extractJsonLd(recipeUrl: String) = withContext(Dispatchers.IO) {
  skrape(HttpFetcher) {
    request {
      url = recipeUrl
    }

    response {
      htmlDocument {
        relaxed = true
        script {
          withAttribute = "type" to "application/ld+json"

          findFirst { html }
        }
      }
    }
  }
}
