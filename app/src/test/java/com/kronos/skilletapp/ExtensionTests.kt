package com.kronos.skilletapp

import com.kronos.skilletapp.model.Measurement
import com.kronos.skilletapp.utils.roundToEighth
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.math.RoundingMode

class ExtensionTests : FunSpec() {
  init {
    context("Eighths") {

      context("0.0") {
        test("Exact") {
          0.0.roundToEighth() shouldBe (0.0 plusOrMinus 0.0001)
        }
      }

      context("1.0") {
        test("Exact") {
          1.0.roundToEighth() shouldBe (1.0 plusOrMinus 0.0001)
        }

        test("Above") {
          1.05.roundToEighth() shouldBe (1.0 plusOrMinus 0.0001)
        }

        test("Below"){
          0.95.roundToEighth() shouldBe (1.0 plusOrMinus 0.0001)
        }
      }

      context("1.125") {
        test("Exact") {
          1.125.roundToEighth() shouldBe (1.125 plusOrMinus 0.0001)
        }

        test("Above") {
          1.126.roundToEighth() shouldBe (1.125 plusOrMinus 0.0001)
        }

        test("Below") {
          1.124.roundToEighth() shouldBe (1.125 plusOrMinus 0.0001)
        }
      }
    }
  }
}