package com.kronos.skilletapp

import com.kronos.skilletapp.utils.Fraction
import com.kronos.skilletapp.utils.toFraction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class FractionTests : FunSpec({
  context("Basic") {
    test("5/10") {
      val f = Fraction(5, 10)
      f.numerator shouldBe 5
      f.denominator shouldBe 10
      f.decimal shouldBe (0.5 plusOrMinus 0.001)
    }

    test("3/4") {
      val f = Fraction(3, 4)
      f.numerator shouldBe 3
      f.denominator shouldBe 4
      f.decimal shouldBe (0.75 plusOrMinus 0.001)
    }

    test("9/4") {
      val f = Fraction(9, 4)
      f.numerator shouldBe 9
      f.denominator shouldBe 4
      f.decimal shouldBe (2.25 plusOrMinus 0.001)
    }

    test("12/4") {
      val f = Fraction(12, 4)
      f.numerator shouldBe 12
      f.denominator shouldBe 4
      f.decimal shouldBe (3.0 plusOrMinus 0.001)
    }
  }

  context("Reduce") {
    test("5/10 to 1/2") {
      val f = Fraction(5, 10).reduce()
      f.numerator shouldBe 1
      f.denominator shouldBe 2
      f.decimal shouldBe (0.5 plusOrMinus 0.001)
    }

    test("90/30 to 3/1") {
      val f = Fraction(90, 30).reduce()
      f.numerator shouldBe 3
      f.denominator shouldBe 1
      f.decimal shouldBe (3.0 plusOrMinus 0.001)
    }
  }

  context("From Double") {
    test("0.5") {
      val f = 0.5.toFraction()
      f.numerator shouldBe 1
      f.denominator shouldBe 2
      f.decimal shouldBe (0.5 plusOrMinus 0.001)
    }

    test("0.75") {
      val f = 0.75.toFraction()
      f.numerator shouldBe 3
      f.denominator shouldBe 4
      f.decimal shouldBe (0.75 plusOrMinus 0.001)
    }

    test("2.5") {
      val f = 2.5.toFraction()
      f.numerator shouldBe 5
      f.denominator shouldBe 2
      f.decimal shouldBe (2.5 plusOrMinus 0.001)
    }

    test("0.333") {
      val f = 0.333.toFraction()
      f.numerator shouldBe 333
      f.denominator shouldBe 1000
      f.decimal shouldBe (0.333 plusOrMinus 0.001)
    }
  }

  context("Rounding") {
    context("Eighths") {
      test("1/8") {
        val f = Fraction(1, 8).roundToEighth()
        f.numerator shouldBe 1
        f.denominator shouldBe 8
        f.decimal shouldBe (0.125 plusOrMinus 0.001)
      }

      test("7/24") {
        val f = Fraction(7, 24).roundToEighth()
        f.numerator shouldBe 2
        f.denominator shouldBe 8
        f.decimal shouldBe (0.25 plusOrMinus 0.001)
      }

      test("17/32") {
        val f = Fraction(17, 32).roundToEighth()
        f.numerator shouldBe 4
        f.denominator shouldBe 8
        f.decimal shouldBe (0.5 plusOrMinus 0.001)
      }
    }

    context("Thirds") {
      test("1/3") {
        val f = Fraction(1, 3).roundToThird()
        f.numerator shouldBe 1
        f.denominator shouldBe 3
        f.decimal shouldBe (0.333 plusOrMinus 0.001)
      }

      test("7/24") {
        val f = Fraction(7, 24).roundToThird()
        f.numerator shouldBe 1
        f.denominator shouldBe 3
        f.decimal shouldBe (0.333 plusOrMinus 0.001)
      }

      test("0.333") {
        val f = 0.333.toFraction().roundToThird()
        f.numerator shouldBe 1
        f.denominator shouldBe 3
        f.decimal shouldBe (0.333 plusOrMinus 0.001)
      }
    }

    context("Auto Choose") {
      test("7/24") {
        val f = Fraction(7, 24).roundToNearestFraction()
        f.numerator shouldBe 1
        f.denominator shouldBe 3
        f.decimal shouldBe (0.333 plusOrMinus 0.001)
      }

      test("17/32") {
        val f = Fraction(17, 32).roundToNearestFraction()
        f.numerator shouldBe 4
        f.denominator shouldBe 8
        f.decimal shouldBe (0.5 plusOrMinus 0.001)
      }

      test("0.6542368") {
        val f = 0.6542368.toFraction().roundToNearestFraction()
        f.numerator shouldBe 2
        f.denominator shouldBe 3
        f.decimal shouldBe (0.666 plusOrMinus 0.001)
      }
    }
  }

})