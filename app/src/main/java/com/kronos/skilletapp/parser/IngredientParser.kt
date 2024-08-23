package com.kronos.skilletapp.parser

import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.parser.grammar.IngredientGrammarLexer
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

object IngredientParser {

  fun parseIngredient(text: String): Ingredient = IngredientVisitor().visitIngredient(
    IngredientGrammarParser(
      CommonTokenStream(
        IngredientGrammarLexer(
          CharStreams.fromString(
            "$text\n"
          )
        )
      )
    ).ingredient()
  )
}