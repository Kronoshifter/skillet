package com.kronos.skilletapp.parser

import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.parser.grammar.IngredientGrammarLexer
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class IngredientParser {

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

  fun parseIngredients(text: String): List<Ingredient> = IngredientVisitor().visitIngredients(
    IngredientGrammarParser(
      CommonTokenStream(
        IngredientGrammarLexer(
          CharStreams.fromString(
            "$text\n"
          )
        )
      )
    ).recipe()
  )
}

//TODO: investigate using Chaquopy to use ingredient-parser-nlp

//>>> ingredient = {
//  ... "name": parsed.name.text,
//  ... "raw": parsed.sentence,
//  ... "comment": parsed.comment,
//  ... "measurement": {
//    ... "quantity": parsed.amount[0].quantity,
//    ... "unit": str(parsed.amount[0].unit),
//    ... }
//  ... }