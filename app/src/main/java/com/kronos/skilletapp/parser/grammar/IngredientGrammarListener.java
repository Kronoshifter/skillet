// Generated from /home/mmattle/SkilletApp/app/src/main/java/com/kronos/skilletapp/parser/IngredientGrammar.g4 by ANTLR 4.13.1
package com.kronos.skilletapp.parser.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link IngredientGrammarParser}.
 */
public interface IngredientGrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link IngredientGrammarParser#recipe}.
	 * @param ctx the parse tree
	 */
	void enterRecipe(IngredientGrammarParser.RecipeContext ctx);
	/**
	 * Exit a parse tree produced by {@link IngredientGrammarParser#recipe}.
	 * @param ctx the parse tree
	 */
	void exitRecipe(IngredientGrammarParser.RecipeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IngredientGrammarParser#ingredient}.
	 * @param ctx the parse tree
	 */
	void enterIngredient(IngredientGrammarParser.IngredientContext ctx);
	/**
	 * Exit a parse tree produced by {@link IngredientGrammarParser#ingredient}.
	 * @param ctx the parse tree
	 */
	void exitIngredient(IngredientGrammarParser.IngredientContext ctx);
	/**
	 * Enter a parse tree produced by {@link IngredientGrammarParser#measurement}.
	 * @param ctx the parse tree
	 */
	void enterMeasurement(IngredientGrammarParser.MeasurementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IngredientGrammarParser#measurement}.
	 * @param ctx the parse tree
	 */
	void exitMeasurement(IngredientGrammarParser.MeasurementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IngredientGrammarParser#quantity}.
	 * @param ctx the parse tree
	 */
	void enterQuantity(IngredientGrammarParser.QuantityContext ctx);
	/**
	 * Exit a parse tree produced by {@link IngredientGrammarParser#quantity}.
	 * @param ctx the parse tree
	 */
	void exitQuantity(IngredientGrammarParser.QuantityContext ctx);
	/**
	 * Enter a parse tree produced by {@link IngredientGrammarParser#decimal}.
	 * @param ctx the parse tree
	 */
	void enterDecimal(IngredientGrammarParser.DecimalContext ctx);
	/**
	 * Exit a parse tree produced by {@link IngredientGrammarParser#decimal}.
	 * @param ctx the parse tree
	 */
	void exitDecimal(IngredientGrammarParser.DecimalContext ctx);
	/**
	 * Enter a parse tree produced by {@link IngredientGrammarParser#fraction}.
	 * @param ctx the parse tree
	 */
	void enterFraction(IngredientGrammarParser.FractionContext ctx);
	/**
	 * Exit a parse tree produced by {@link IngredientGrammarParser#fraction}.
	 * @param ctx the parse tree
	 */
	void exitFraction(IngredientGrammarParser.FractionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IngredientGrammarParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(IngredientGrammarParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link IngredientGrammarParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(IngredientGrammarParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link IngredientGrammarParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(IngredientGrammarParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link IngredientGrammarParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(IngredientGrammarParser.CommentContext ctx);
}