package com.kronos.skilletapp

import com.github.michaelbull.result.unwrap
import com.kronos.skilletapp.scraping.RecipeScraper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class RecipeScrapingTests : FunSpec({
  context("Scraping") {
    val scraper = RecipeScraper()
    val pancakesUrl = "https://www.allrecipes.com/recipe/21014/good-old-fashioned-pancakes/"
    val garlicShellsUrl = "https://iowagirleats.com/creamy-garlic-shells/"

    test("Pancakes") {
      val extracted = scraper.scrapeRecipe(pancakesUrl).unwrap()

      extracted shouldNotBe null
      extracted.name shouldBe "Good Old-Fashioned Pancakes"
    }


    test("Garlic Shells") {
      val json = scraper.scrapeRecipe(garlicShellsUrl).unwrap()

      json shouldNotBe null
      json.name shouldBe "Creamy Garlic Shells"
    }
  }
})