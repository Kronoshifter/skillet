package com.kronos.skilletapp.parser

import com.kronos.measurement.model.Measurement
import com.kronos.measurement.model.MeasurementUnit
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.parser.grammar.IngredientGrammarBaseVisitor
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser
import com.kronos.skilletapp.utils.removePunctuation
import com.kronos.utils.Fraction

class IngredientVisitor : IngredientGrammarBaseVisitor<Ingredient>() {
  fun visitIngredients(ctx: IngredientGrammarParser.RecipeContext): List<Ingredient>? = ctx.ingredient()?.mapNotNull { visitIngredient(it) }

  override fun visitIngredient(ctx: IngredientGrammarParser.IngredientContext?): Ingredient {
    val name = ctx?.name()?.text.orEmpty()

    val unit = MeasurementUnit.fromName(ctx?.measurement()?.WORD()?.text)
    val quantity =
      with(ctx?.measurement()?.quantity()) {
        this?.decimal()?.text?.toFloatOrNull()
          ?: this?.fraction()?.let {
            when (it.NUMBER()?.size) {
              2 ->
                Fraction(
                  numerator = it.NUMBER(0)?.text?.toInt() ?: 0,
                  denominator = it.NUMBER(1)?.text?.toInt() ?: 0,
                )
              else ->
                Fraction(
                  whole = it.NUMBER(0)?.text?.toInt() ?: 0,
                  numerator = it.NUMBER(1)?.text?.toInt() ?: 0,
                  denominator = it.NUMBER(2)?.text?.toInt() ?: 0,
                )
            }.decimal
          }
      } ?: 0f
    val measurement = Measurement(quantity, unit)

    val comment = ctx?.comment()?.text?.removePunctuation()?.trim()

    return Ingredient(
      name = name,
      comment = comment,
      measurement = measurement,
      raw = ctx?.text?.trimEnd().orEmpty(),
    )
  }
}
