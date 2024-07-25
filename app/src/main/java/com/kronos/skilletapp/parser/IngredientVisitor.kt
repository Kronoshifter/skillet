package com.kronos.skilletapp.parser

import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.parser.grammar.IngredientGrammarBaseVisitor
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser
import com.kronos.skilletapp.utils.Fraction

class IngredientVisitor : IngredientGrammarBaseVisitor<Ingredient>() {
//  override fun visitRecipe(ctx: IngredientGrammarParser.RecipeContext): Ingredient {
//    val ingredients = ctx.ingredient().map { it.visit(this) }
//    return Ingredient(ingredients)
//  }

  override fun visitIngredient(ctx: IngredientGrammarParser.IngredientContext): Ingredient {
    val name = ctx.name().text

    val unit = ctx.measurement()?.WORD()?.text?.let { unit ->
      MeasurementUnit.values.firstOrNull {
        it.name == unit || it.aliases.contains(unit)
      } ?: MeasurementUnit.Custom(unit)
    } ?: MeasurementUnit.None
    val quantity = with(ctx.measurement()?.quantity()) {
      this?.decimal()?.text?.toDoubleOrNull() ?: this?.fraction()?.let {
        when (it.NUMBER().size) {
          2 -> Fraction(numerator = it.NUMBER(0).text.toInt(), denominator = it.NUMBER(1).text.toInt())
          else -> Fraction(numerator = it.NUMBER(0).text.toInt() * it.NUMBER(2).text.toInt() + it.NUMBER(1).text.toInt(), denominator = it.NUMBER(2).text.toInt())
        }.decimal
      }
    } ?: 0.0
    val measurement = Measurement(quantity, unit)

    val comment = ctx.comment()?.WORD()?.joinToString(" ") { it.text }

    return Ingredient(name = name, comment = comment, measurement = measurement)
  }
}