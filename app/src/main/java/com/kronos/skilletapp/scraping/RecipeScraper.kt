package com.kronos.skilletapp.scraping

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.recoverCatching
import com.github.michaelbull.result.runCatching
import com.kronos.skilletapp.data.SkilletError
import com.kronos.skilletapp.parser.IngredientParser
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.script
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed interface Html {
  @SerialName("@type") val type: String
}

@Serializable
data class RecipeHtml(
  @SerialName("@type") override val type: String,
  val name: String,
  val description: String = "",
  @SerialName("recipeIngredient") val ingredients: List<String>,
  @SerialName("recipeInstructions") val instructions: List<InstructionHtml>,
  val prepTime: String,
  val cookTime: String,
  @Serializable(with = ListOrStringSerializer::class) val recipeYield: String,
) : Html

@Serializable
data class InstructionHtml(
  val text: String,
)

@Serializable
data class JsonLd(
  @SerialName("@graph") val list: List<Html>
)

class RecipeScraper(private val recipeParser: IngredientParser) {

  fun scrapeRecipe(url: String) = extractJsonLd(url).andThen { parseJsonLd(it) }

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

  private fun parseJsonLd(input: String): Result<RecipeHtml, SkilletError> {
    val json = Json { ignoreUnknownKeys = true }
    return runCatching {
      json.decodeFromString<RecipeHtml>(input)
    }.recoverCatching {
      json.decodeFromString<List<RecipeHtml>>(input).first()
    }.mapError {
      SkilletError("Failed to parse JSON-LD: ${it.message}")
    }
  }


}