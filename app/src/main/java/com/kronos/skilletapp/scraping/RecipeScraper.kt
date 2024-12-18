package com.kronos.skilletapp.scraping

import com.github.michaelbull.result.*
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.parser.IngredientParser
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachText
import it.skrape.selects.html5.a
import it.skrape.selects.html5.script
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class RecipeHtml(
  val name: String,
  val description: String = "",
  @SerialName("recipeIngredient") val ingredients: List<String>,
  @SerialName("recipeInstructions") val instructions: List<InstructionHtml>,
  val prepTime: String,
  val cookTime: String,
  @Serializable(with = StringListUnwrappingSerializer::class) val recipeYield: String,
)

@Serializable
data class InstructionHtml(
  val text: String,
)

class RecipeScraper {

  fun scrapeRecipe(url: String): Result<RecipeHtml, SkilletError> {
    return scrapeJsonLd(url)
  }

  private fun scrapeJsonLd(url: String): Result<RecipeHtml, SkilletError> = extractJsonLd(url)
    .andThen { extractRecipeJsonLd(it) }
    .andThen { parseJsonLd(it) }

  private fun extractJsonLd(recipeUrl: String): Result<String, SkilletError> = skrape(HttpFetcher) {
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

  private fun extractRecipeJsonLd(input: String): Result<JsonElement, SkilletError> {
    return runCatching {
      Json.parseToJsonElement(input)
    }.mapError {
      SkilletError("Error parsing JSON: ${it.message}")
    }.andThen { element ->
      element.findRecipeJson().toResultOr { SkilletError("Could not find recipe Json") }
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

  private infix fun JsonElement.isOrContains(s: String): Boolean = when(this) {
    is JsonPrimitive -> this.content == s
    is JsonArray -> this.any { it.jsonPrimitive.content == s }
    else -> false
  }

  private fun parseJsonLd(element: JsonElement): Result<RecipeHtml, SkilletError> {
    val json = Json { ignoreUnknownKeys = true }
    return runCatching {
      json.decodeFromJsonElement<RecipeHtml>(element)
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
            recipeYield = "",
            prepTime = "",
            cookTime = ""
          )
        }
      }
    }.mapError { SkilletError("Failed to scrape microdata: ${it.message}") }
  }
}