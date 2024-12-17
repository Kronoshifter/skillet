package com.kronos.skilletapp

import android.R.attr.type
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import it.skrape.core.document
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachText
import it.skrape.selects.html5.head
import it.skrape.selects.html5.script
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeScrapingTests : FunSpec({
  context("Scraping") {
    val recipeUrl = "https://www.allrecipes.com/recipe/21014/good-old-fashioned-pancakes/"

    test("Retrieve JSON-LD") {
      val extracted = withContext(Dispatchers.IO) {
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

      extracted shouldNotBe null
    }
  }
})