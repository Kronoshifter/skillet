package com.kronos.skilletapp.parser

import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.parser.grammar.IngredientGrammarLexer
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

object IngredientParser {

  fun parseIngredient(text: String): Ingredient {
    val stream = CharStreams.fromString(text)
    val lexer = IngredientGrammarLexer(stream)
    val tokens = CommonTokenStream(lexer)
    val parser = IngredientGrammarParser(tokens)

    val context = parser.ingredient()
    val visitor = IngredientVisitor()
    return visitor.visitIngredient(context)
  }
}