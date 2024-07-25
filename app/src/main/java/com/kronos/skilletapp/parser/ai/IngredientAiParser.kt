package com.kronos.skilletapp.parser.ai

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.decapitalize
import com.google.android.gms.tasks.Task
import com.google.android.gms.tflite.java.TfLite
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.IngredientType
import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.model.MeasurementUnit
import com.kronos.skilletapp.parser.grammar.IngredientGrammarLexer
import com.kronos.skilletapp.parser.grammar.IngredientGrammarParser
import com.kronos.skilletapp.utils.normalizeWhitespace
import com.kronos.skilletapp.utils.removePunctuation
import org.antlr.v4.runtime.CharStreams
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.util.*

class IngredientAiParser(context: Context) {

  val initializeTask: Task<Void> by lazy { TfLite.initialize(context) }

  private val wordMap: MutableMap<String, Float> = mutableMapOf()

  init {
    initializeTask.addOnSuccessListener {
      val modelName = "ingredient_parser_model.tflite"
      val interpreterOption = InterpreterApi.Options().setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY)
      val modelBuffer = FileUtil.loadMappedFile(context, modelName)
      interpreter = InterpreterApi.create(modelBuffer, interpreterOption)
      interpreter.allocateTensors()
    }.addOnFailureListener { e ->
      Log.e("Interpreter", "Failed to initialize interpreter", e)
    }

    context.assets.open("word_index.txt").reader().forEachLine {
      val (word, index) = it.split(":")
      wordMap[word.removeSurrounding("'")] = index.trim().toFloat()
    }
  }

  private lateinit var interpreter: InterpreterApi

  fun init() {}

  fun parseIngredient(input: String): Ingredient {
    val inputVector = padInputVector(vectorizeInputString(preProcessInputString(input)), 60)

    val outputTensor = interpreter.getOutputTensor(0)
    val output = FloatArray(outputTensor.numElements())
    interpreter.run(inputVector.toFloatArray(), output)

    return Ingredient("test", measurement = Measurement(1.0, MeasurementUnit.Ounce))
  }

  private fun preProcessInputString(text: String): String {
    return text.removePunctuation().normalizeWhitespace().lowercase().trim()
  }

  private fun vectorizeInputString(input: String) = input.split(" ").map { wordMap[it] ?: 1f }

  private fun padInputVector(input: List<Float>, maxLength: Int): List<Float> {
    val padding = List(maxLength - input.size) { 0f }
    return input + padding
  }
}