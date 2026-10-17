// Generated from /home/mmattle/skillet/app/src/main/java/com/kronos/skilletapp/parser/IngredientGrammar.g4 by ANTLR 4.13.2
@file:Suppress("detekt:all")

package com.kronos.skilletapp.parser.grammar

import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser.CommentContext
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser.DecimalContext
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser.FractionContext
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser.IngredientContext
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser.MeasurementContext
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser.NameContext
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser.QuantityContext
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser.RecipeContext
import org.antlr.v4.runtime.tree.ParseTreeListener

/** This interface defines a complete listener for a parse tree produced by [IngredientGrammarParser]. */
interface IngredientGrammarListener : ParseTreeListener {
  /**
   * Enter a parse tree produced by [IngredientGrammarParser.recipe].
   *
   * @param ctx the parse tree
   */
  fun enterRecipe(ctx: RecipeContext?)

  /**
   * Exit a parse tree produced by [IngredientGrammarParser.recipe].
   *
   * @param ctx the parse tree
   */
  fun exitRecipe(ctx: RecipeContext?)

  /**
   * Enter a parse tree produced by [IngredientGrammarParser.ingredient].
   *
   * @param ctx the parse tree
   */
  fun enterIngredient(ctx: IngredientContext?)

  /**
   * Exit a parse tree produced by [IngredientGrammarParser.ingredient].
   *
   * @param ctx the parse tree
   */
  fun exitIngredient(ctx: IngredientContext?)

  /**
   * Enter a parse tree produced by [IngredientGrammarParser.measurement].
   *
   * @param ctx the parse tree
   */
  fun enterMeasurement(ctx: MeasurementContext?)

  /**
   * Exit a parse tree produced by [IngredientGrammarParser.measurement].
   *
   * @param ctx the parse tree
   */
  fun exitMeasurement(ctx: MeasurementContext?)

  /**
   * Enter a parse tree produced by [IngredientGrammarParser.quantity].
   *
   * @param ctx the parse tree
   */
  fun enterQuantity(ctx: QuantityContext?)

  /**
   * Exit a parse tree produced by [IngredientGrammarParser.quantity].
   *
   * @param ctx the parse tree
   */
  fun exitQuantity(ctx: QuantityContext?)

  /**
   * Enter a parse tree produced by [IngredientGrammarParser.decimal].
   *
   * @param ctx the parse tree
   */
  fun enterDecimal(ctx: DecimalContext?)

  /**
   * Exit a parse tree produced by [IngredientGrammarParser.decimal].
   *
   * @param ctx the parse tree
   */
  fun exitDecimal(ctx: DecimalContext?)

  /**
   * Enter a parse tree produced by [IngredientGrammarParser.fraction].
   *
   * @param ctx the parse tree
   */
  fun enterFraction(ctx: FractionContext?)

  /**
   * Exit a parse tree produced by [IngredientGrammarParser.fraction].
   *
   * @param ctx the parse tree
   */
  fun exitFraction(ctx: FractionContext?)

  /**
   * Enter a parse tree produced by [IngredientGrammarParser.range].
   *
   * @param ctx the parse tree
   */
  fun enterRange(ctx: IngredientGrammarParser.RangeContext?)

  /**
   * Exit a parse tree produced by [IngredientGrammarParser.range].
   *
   * @param ctx the parse tree
   */
  fun exitRange(ctx: IngredientGrammarParser.RangeContext?)

  /**
   * Enter a parse tree produced by [IngredientGrammarParser.name].
   *
   * @param ctx the parse tree
   */
  fun enterName(ctx: NameContext?)

  /**
   * Exit a parse tree produced by [IngredientGrammarParser.name].
   *
   * @param ctx the parse tree
   */
  fun exitName(ctx: NameContext?)

  /**
   * Enter a parse tree produced by [IngredientGrammarParser.comment].
   *
   * @param ctx the parse tree
   */
  fun enterComment(ctx: CommentContext?)

  /**
   * Exit a parse tree produced by [IngredientGrammarParser.comment].
   *
   * @param ctx the parse tree
   */
  fun exitComment(ctx: CommentContext?)
}
