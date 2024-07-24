// Generated from /home/mmattle/SkilletApp/app/src/main/java/com/kronos/skilletapp/parser/IngredientGrammar.g4 by ANTLR 4.13.1
package com.kronos.skilletapp.parser.grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link IngredientGrammarParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface IngredientGrammarVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link IngredientGrammarParser#recipe}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecipe(IngredientGrammarParser.RecipeContext ctx);
	/**
	 * Visit a parse tree produced by {@link IngredientGrammarParser#ingredient}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIngredient(IngredientGrammarParser.IngredientContext ctx);
	/**
	 * Visit a parse tree produced by {@link IngredientGrammarParser#measurement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMeasurement(IngredientGrammarParser.MeasurementContext ctx);
	/**
	 * Visit a parse tree produced by {@link IngredientGrammarParser#quantity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuantity(IngredientGrammarParser.QuantityContext ctx);
	/**
	 * Visit a parse tree produced by {@link IngredientGrammarParser#decimal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimal(IngredientGrammarParser.DecimalContext ctx);
	/**
	 * Visit a parse tree produced by {@link IngredientGrammarParser#fraction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFraction(IngredientGrammarParser.FractionContext ctx);
	/**
	 * Visit a parse tree produced by {@link IngredientGrammarParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(IngredientGrammarParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link IngredientGrammarParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(IngredientGrammarParser.CommentContext ctx);
}