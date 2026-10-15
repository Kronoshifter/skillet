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
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.TerminalNode

/**
 * This class provides an empty implementation of [IngredientGrammarListener], which can be extended to create a listener which only needs
 * to handle a subset of the available methods.
 */
class IngredientGrammarBaseListener : IngredientGrammarListener {
  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun enterRecipe(ctx: RecipeContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun exitRecipe(ctx: RecipeContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun enterIngredient(ctx: IngredientContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun exitIngredient(ctx: IngredientContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun enterMeasurement(ctx: MeasurementContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun exitMeasurement(ctx: MeasurementContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun enterQuantity(ctx: QuantityContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun exitQuantity(ctx: QuantityContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun enterDecimal(ctx: DecimalContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun exitDecimal(ctx: DecimalContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun enterFraction(ctx: FractionContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun exitFraction(ctx: FractionContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun enterRange(ctx: IngredientGrammarParser.RangeContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun exitRange(ctx: IngredientGrammarParser.RangeContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun enterName(ctx: NameContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun exitName(ctx: NameContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun enterComment(ctx: CommentContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun exitComment(ctx: CommentContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun enterEveryRule(ctx: ParserRuleContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun exitEveryRule(ctx: ParserRuleContext?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun visitTerminal(node: TerminalNode?) {}

  /**
   * {@inheritDoc}
   *
   * The default implementation does nothing.
   */
  override fun visitErrorNode(node: ErrorNode?) {}
}
