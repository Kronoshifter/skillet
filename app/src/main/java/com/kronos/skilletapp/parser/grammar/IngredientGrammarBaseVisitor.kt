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
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor

/**
 * This class provides an empty implementation of [IngredientGrammarVisitor], which can be extended to create a visitor which only needs to
 * handle a subset of the available methods.
 *
 * @param <T> The return type of the visit operation. Use [Void] for operations with no return type. </T>
 */
open class IngredientGrammarBaseVisitor<T> : AbstractParseTreeVisitor<T?>(), IngredientGrammarVisitor<T?> {
  /**
   * {@inheritDoc}
   *
   * The default implementation returns the result of calling [.visitChildren] on `ctx`.
   */
  override fun visitRecipe(ctx: RecipeContext?): T? {
    return visitChildren(ctx)
  }

  /**
   * {@inheritDoc}
   *
   * The default implementation returns the result of calling [.visitChildren] on `ctx`.
   */
  override fun visitIngredient(ctx: IngredientContext?): T? {
    return visitChildren(ctx)
  }

  /**
   * {@inheritDoc}
   *
   * The default implementation returns the result of calling [.visitChildren] on `ctx`.
   */
  override fun visitMeasurement(ctx: MeasurementContext?): T? {
    return visitChildren(ctx)
  }

  /**
   * {@inheritDoc}
   *
   * The default implementation returns the result of calling [.visitChildren] on `ctx`.
   */
  override fun visitQuantity(ctx: QuantityContext?): T? {
    return visitChildren(ctx)
  }

  /**
   * {@inheritDoc}
   *
   * The default implementation returns the result of calling [.visitChildren] on `ctx`.
   */
  override fun visitDecimal(ctx: DecimalContext?): T? {
    return visitChildren(ctx)
  }

  /**
   * {@inheritDoc}
   *
   * The default implementation returns the result of calling [.visitChildren] on `ctx`.
   */
  override fun visitFraction(ctx: FractionContext?): T? {
    return visitChildren(ctx)
  }

  /**
   * {@inheritDoc}
   *
   * The default implementation returns the result of calling [.visitChildren] on `ctx`.
   */
  override fun visitRange(ctx: IngredientGrammarParser.RangeContext?): T? {
    return visitChildren(ctx)
  }

  /**
   * {@inheritDoc}
   *
   * The default implementation returns the result of calling [.visitChildren] on `ctx`.
   */
  override fun visitName(ctx: NameContext?): T? {
    return visitChildren(ctx)
  }

  /**
   * {@inheritDoc}
   *
   * The default implementation returns the result of calling [.visitChildren] on `ctx`.
   */
  override fun visitComment(ctx: CommentContext?): T? {
    return visitChildren(ctx)
  }
}
