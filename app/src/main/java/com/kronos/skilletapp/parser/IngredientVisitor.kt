package com.kronos.skilletapp.parser

import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.parser.grammar.IngredientGrammarBaseVisitor
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser
import com.kronos.skilletapp.utils.Fraction
import com.kronos.skilletapp.utils.removePunctuation

class IngredientVisitor : IngredientGrammarBaseVisitor<Ingredient>() {
  fun visitIngredients(ctx: IngredientGrammarParser.RecipeContext) = ctx.ingredient().map { visitIngredient(it) }

  override fun visitIngredient(ctx: IngredientGrammarParser.IngredientContext): Ingredient {
    val name = ctx.name()?.text ?: ""

    val unit = MeasurementUnit.fromName(ctx.measurement()?.WORD()?.text)
    val quantity = with(ctx.measurement()?.quantity()) {
      this?.decimal()?.text?.toFloatOrNull() ?: this?.fraction()?.let {
        when (it.NUMBER().size) {
          2 -> Fraction(numerator = it.NUMBER(0).text.toInt(), denominator = it.NUMBER(1).text.toInt())
          else -> Fraction(whole = it.NUMBER(0).text.toInt(), numerator = it.NUMBER(1).text.toInt(), denominator = it.NUMBER(2).text.toInt())
        }.decimal
      }
    } ?: 0f
    val measurement = Measurement(quantity, unit)

    val comment = ctx.comment()?.text?.removePunctuation()?.trim()

    return Ingredient(name = name, comment = comment, measurement = measurement, raw = ctx.text.trimEnd())
  }
}