package com.kronos.skilletapp.scraping

import com.github.michaelbull.result.*
import com.kronos.skilletapp.data.SkilletError
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

@Serializable
data class InstructionHtml(
  val text: String,
)

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

  suspend fun scrapeRecipe(url: String): Result<RecipeScrape, SkilletError> {
    return scrapeJsonLd(url)
  }

  private suspend fun scrapeJsonLd(url: String): Result<RecipeScrape, SkilletError> = extractJsonLd(url)
    .andThen { parseToJson(it) }
    .andThen { parseToScrape(it) }

  private suspend fun extractJsonLd(recipeUrl: String): Result<String, SkilletError> = skrape(AsyncFetcher) {
    request {
      url = recipeUrl
    }

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
    }.mapError {
      SkilletError("Failed to extract JSON-LD from $recipeUrl")
    }
  }

  private fun parseToJson(input: String): Result<JsonElement, SkilletError> {
    return runCatching {
      Json.parseToJsonElement(input)
    }.mapError {
      SkilletError("Error parsing JSON: ${it.message}")
    }.andThen { element ->
      val recipeJson = element.findRecipeJson()
      val websiteJson = element.findWebsiteJson()

      recipeJson.toResultOr {
        SkilletError("Failed to find recipe JSON")
      }.map { recipe ->
        buildJsonObject {
          put("recipe", recipe)
          websiteJson?.let { put("website", it) }
        }
      }
    }
  }

  private fun JsonElement.findRecipeJson(): JsonElement? {
    return if (this is JsonObject && "@type" in this && this.getValue("@type") isOrContains "Recipe" == true) {
      this
    } else if (this is JsonObject && "@type" !in this) {
      this.firstNotNullOfOrNull { it.value.findRecipeJson() }
    } else if (this is JsonArray) {
      this.firstNotNullOfOrNull { it.findRecipeJson() }
    } else {
      null
    }
  }

  private fun JsonElement.findWebsiteJson(): JsonElement? {
    return if (this is JsonObject && "@type" in this && this.getValue("@type") isOrContains "WebSite" == true) {
      this
    } else if (this is JsonObject && "@type" !in this) {
      this.firstNotNullOfOrNull { it.value.findWebsiteJson() }
    } else if (this is JsonArray) {
      this.firstNotNullOfOrNull { it.findWebsiteJson() }
    } else {
      null
    }
  }

  private infix fun JsonElement.isOrContains(s: String): Boolean = when (this) {
    is JsonPrimitive -> this.content == s
    is JsonArray -> this.any { it.jsonPrimitive.content == s }
    else -> false
  }

  private fun parseToScrape(element: JsonElement): Result<RecipeScrape, SkilletError> {
    val json = Json { ignoreUnknownKeys = true }
    return runCatching {
      json.decodeFromJsonElement<RecipeScrape>(element)
    }.mapError {
      SkilletError("Failed to parse JSON-LD: ${it.message}")
    }
  }

  private fun scrapeMicrodata(recipeUrl: String): Result<RecipeHtml, SkilletError> = skrape(HttpFetcher) {
    request {
      url = recipeUrl
    }

    runCatching {
      response {
        htmlDocument {
          relaxed = true

          val name = "[itemprop=name]" {
            findFirst { text }
          }

          val ingredients = "[itemprop=recipeIngredient]" {
            findAll { eachText }
          }

          RecipeHtml(
            name = name,
            ingredients = ingredients,
            instructions = listOf(),
            recipeYield = listOf(),
            prepTime = "",
            cookTime = ""
          )
        }
      }
    }.mapError { SkilletError("Failed to scrape microdata: ${it.message}") }
  }
}