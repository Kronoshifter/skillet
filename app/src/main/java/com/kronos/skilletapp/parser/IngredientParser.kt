package com.kronos.skilletapp.parser

import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.parser.grammar.IngredientGrammarLexer
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser
import com.kronos.skilletapp.utils.isNotNullOrBlank
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class IngredientParser {
  private val visitor = IngredientVisitor()
  
  fun parseIngredient(text: String): Ingredient = visitor.visitIngredient(
    IngredientGrammarParser(
      CommonTokenStream(
        IngredientGrammarLexer(
          CharStreams.fromString(
            "${preProcess(text)}\n"
          )
        )
      )
    ).ingredient()
  )

  fun parseIngredients(text: String): List<Ingredient> = visitor.visitIngredients(
    IngredientGrammarParser(
      CommonTokenStream(
        IngredientGrammarLexer(
          CharStreams.fromString(
            "${preProcess(text)}\n"
          )
        )
      )
    ).recipe()
  )
  
  private fun preProcess(text: String): String {
    val fractionMap = mapOf(
      '\u00BC' to "1/4",
      '\u00BD' to "1/2",
      '\u00BE' to "3/4",
      '\u2150' to "1/7",
      '\u2151' to "1/9",
      '\u2152' to "1/10",
      '\u2153' to "1/3",
      '\u2154' to "2/3",
      '\u2155' to "1/5",
      '\u2156' to "2/5",
      '\u2157' to "3/5",
      '\u2158' to "4/5",
      '\u2159' to "1/6",
      '\u215A' to "5/6",
      '\u215B' to "1/8",
      '\u215C' to "3/8",
      '\u215D' to "5/8",
      '\u215E' to "7/8"
    )

    return text.replace("""(\d*)([\u00bc-\u00be\u2150-\u215e])""".toRegex()) { match ->
      val (digit, fraction) = match.destructured
      fractionMap[fraction.single()]?.let {
        if (digit.isNotNullOrBlank()) "$digit $it" else it
      } ?: match.value
    }
  }
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