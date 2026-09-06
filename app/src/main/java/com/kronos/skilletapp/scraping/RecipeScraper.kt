package com.kronos.skilletapp.scraping

import com.github.michaelbull.result.*
import com.kronos.skilletapp.model.InvalidHtmlError
import com.kronos.skilletapp.model.JsonParseError
import com.kronos.skilletapp.model.RecipeScrapeError
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachText
import it.skrape.selects.html5.script
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class RecipeHtml(
  val name: String,
  val description: String = "",
  @SerialName("recipeIngredient") val ingredients: List<String>,
  @SerialName("recipeInstructions") val instructions: List<InstructionHtml>,
  val prepTime: String,
  val cookTime: String,
  @Serializable(with = StringListWrappingSerializer::class) val recipeYield: List<String>,
)

@Serializable data class InstructionHtml(val text: String)

@Serializable
data class WebSiteHtml(
  val url: String,
  val name: String,
  val description: String,
)

@Serializable
data class RecipeScrape(
  val recipe: RecipeHtml,
  val website: WebSiteHtml? = null,
)

class RecipeScraper {

  suspend fun scrapeRecipe(url: String): Result<RecipeScrape, RecipeScrapeError> {
    return scrapeJsonLd(url)
  }

  private suspend fun scrapeJsonLd(url: String): Result<RecipeScrape, RecipeScrapeError> =
    extractJsonLd(url).andThen { parseToJson(it) }.andThen { parseToScrape(it) }

  private suspend fun extractJsonLd(recipeUrl: String): Result<String, RecipeScrapeError> =
    skrape(AsyncFetcher) {
      request { url = recipeUrl }

      runCatching {
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
        .mapError { InvalidHtmlError("Failed to extract JSON-LD from $recipeUrl") }
    }

  private fun parseToJson(input: String): Result<JsonElement, RecipeScrapeError> {
    return runCatching {
      Json.parseToJsonElement(input)
    }
      .mapError { JsonParseError("Error parsing JSON: ${it.message}") }
      .andThen { element ->
        val recipeJson = element.findJson("Recipe")
        val websiteJson = element.findJson("WebSite")

        recipeJson
          .toResultOr { InvalidHtmlError("Failed to find recipe JSON") }
          .map { recipe ->
            buildJsonObject {
              put("recipe", recipe)
              websiteJson?.let { put("website", it) }
            }
          }
      }
  }

  private fun JsonElement.findJson(key: String): JsonElement? =
    when {
      this is JsonObject && "@type" in this && this.getValue("@type") isOrContains key -> this
      this is JsonObject && key !in this -> this.firstNotNullOfOrNull { it.value.findJson(key) }
      this is JsonArray -> this.firstNotNullOfOrNull { it.findJson(key) }
      else -> null
    }

  private infix fun JsonElement.isOrContains(s: String): Boolean =
    when (this) {
      is JsonPrimitive -> this.content == s
      is JsonArray -> this.any { it.jsonPrimitive.content == s }
      else -> false
    }

  private fun parseToScrape(element: JsonElement): Result<RecipeScrape, RecipeScrapeError> {
    val json = Json { ignoreUnknownKeys = true }
    return runCatching {
      json.decodeFromJsonElement<RecipeScrape>(element)
    }
      .mapError { JsonParseError("Failed to parse JSON-LD: ${it.message}") }
  }

  private fun scrapeMicrodata(recipeUrl: String): Result<RecipeHtml, RecipeScrapeError> =
    skrape(HttpFetcher) {
      request { url = recipeUrl }

      runCatching {
        response {
          htmlDocument {
            relaxed = true

            val name = "[itemprop=name]" { findFirst { text } }

            val ingredients = "[itemprop=recipeIngredient]" { findAll { eachText } }

            RecipeHtml(
              name = name,
              ingredients = ingredients,
              instructions = listOf(),
              recipeYield = listOf(),
              prepTime = "",
              cookTime = "",
            )
          }
        }
      }
        .mapError { InvalidHtmlError("Failed to scrape microdata: ${it.message}") }
    }
}
