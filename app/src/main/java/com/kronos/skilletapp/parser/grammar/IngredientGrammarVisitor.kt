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
import org.antlr.v4.runtime.tree.ParseTreeVisitor

/**
 * This interface defines a complete generic visitor for a parse tree produced by [IngredientGrammarParser].
 *
 * @param <T> The return type of the visit operation. Use [Void] for operations with no return type. </T>
 */
interface IngredientGrammarVisitor<T> : ParseTreeVisitor<T?> {
  /**
   * Visit a parse tree produced by [IngredientGrammarParser.recipe].
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  fun visitRecipe(ctx: RecipeContext?): T?

  /**
   * Visit a parse tree produced by [IngredientGrammarParser.ingredient].
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  fun visitIngredient(ctx: IngredientContext?): T?

  /**
   * Visit a parse tree produced by [IngredientGrammarParser.measurement].
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  fun visitMeasurement(ctx: MeasurementContext?): T?

  /**
   * Visit a parse tree produced by [IngredientGrammarParser.quantity].
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  fun visitQuantity(ctx: QuantityContext?): T?

  /**
   * Visit a parse tree produced by [IngredientGrammarParser.decimal].
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  fun visitDecimal(ctx: DecimalContext?): T?

  /**
   * Visit a parse tree produced by [IngredientGrammarParser.fraction].
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  fun visitFraction(ctx: FractionContext?): T?

  /**
   * Visit a parse tree produced by [IngredientGrammarParser.range].
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  fun visitRange(ctx: IngredientGrammarParser.RangeContext?): T?

  /**
   * Visit a parse tree produced by [IngredientGrammarParser.name].
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  fun visitName(ctx: NameContext?): T?

  /**
   * Visit a parse tree produced by [IngredientGrammarParser.comment].
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  fun visitComment(ctx: CommentContext?): T?
}
