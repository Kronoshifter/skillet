package com.kronos.skilletapp

import com.kronos.skilletapp.utils.Fraction
import com.kronos.skilletapp.utils.toFraction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

class FractionTests : FunSpec({
  context("Basic") {
    test("5/10") {
      val f = Fraction(5, 10)
      f.numerator shouldBe 5
      f.denominator shouldBe 10
      f.decimal shouldBe (0.5f plusOrMinus 0.001f)
    }

    test("3/4") {
      val f = Fraction(3, 4)
      f.numerator shouldBe 3
      f.denominator shouldBe 4
      f.decimal shouldBe (0.75f plusOrMinus 0.001f)
    }

    test("9/4") {
      val f = Fraction(9, 4)
      f.numerator shouldBe 9
      f.denominator shouldBe 4
      f.decimal shouldBe (2.25f plusOrMinus 0.001f)
    }

    test("12/4") {
      val f = Fraction(12, 4)
      f.numerator shouldBe 12
      f.denominator shouldBe 4
      f.decimal shouldBe (3.0f plusOrMinus 0.001f)
    }
  }

  context("Reduce") {
    test("5/10 to 1/2") {
      val f = Fraction(5, 10).reduce()
      f.numerator shouldBe 1
      f.denominator shouldBe 2
      f.decimal shouldBe (0.5f plusOrMinus 0.001f)
    }

    test("90/30 to 3/1") {
      val f = Fraction(90, 30).reduce()
      f.numerator shouldBe 3
      f.denominator shouldBe 1
      f.decimal shouldBe (3.0f plusOrMinus 0.001f)
    }
  }

  context("From Double") {
    test("0.5f") {
      val f = 0.5f.toFraction()
      f.numerator shouldBe 1
      f.denominator shouldBe 2
      f.decimal shouldBe (0.5f plusOrMinus 0.001f)
    }

    test("0.75f") {
      val f = 0.75f.toFraction()
      f.numerator shouldBe 3
      f.denominator shouldBe 4
      f.decimal shouldBe (0.75f plusOrMinus 0.001f)
    }

    test("2.5f") {
      val f = 2.5f.toFraction()
      f.numerator shouldBe 5
      f.denominator shouldBe 2
      f.decimal shouldBe (2.5f plusOrMinus 0.001f)
    }

    test("0.333f") {
      val f = 0.333f.toFraction()
      f.numerator shouldBe 333
      f.denominator shouldBe 1000
      f.decimal shouldBe (0.333f plusOrMinus 0.001f)
    }
  }

  context("Rounding") {
    context("Eighths") {
      test("1/8") {
        val f = Fraction(1, 8).roundToEighth()
        f.numerator shouldBe 1
        f.denominator shouldBe 8
        f.decimal shouldBe (0.125f plusOrMinus 0.001f)
      }

      test("7/24") {
        val f = Fraction(7, 24).roundToEighth()
        f.numerator shouldBe 2
        f.denominator shouldBe 8
        f.decimal shouldBe (0.25f plusOrMinus 0.001f)
      }

      test("17/32") {
        val f = Fraction(17, 32).roundToEighth()
        f.numerator shouldBe 4
        f.denominator shouldBe 8
        f.decimal shouldBe (0.5f plusOrMinus 0.001f)
      }
    }

    context("Thirds") {
      test("1/3") {
        val f = Fraction(1, 3).roundToThird()
        f.numerator shouldBe 1
        f.denominator shouldBe 3
        f.decimal shouldBe (0.333f plusOrMinus 0.001f)
      }

      test("7/24") {
        val f = Fraction(7, 24).roundToThird()
        f.numerator shouldBe 1
        f.denominator shouldBe 3
        f.decimal shouldBe (0.333f plusOrMinus 0.001f)
      }

      test("0.333f") {
        val f = 0.333f.toFraction().roundToThird()
        f.numerator shouldBe 1
        f.denominator shouldBe 3
        f.decimal shouldBe (0.333f plusOrMinus 0.001f)
      }
    }

    context("Auto Choose") {
      test("7/24") {
        val f = Fraction(7, 24).roundToNearestFraction()
        f.numerator shouldBe 1
        f.denominator shouldBe 3
        f.decimal shouldBe (0.333f plusOrMinus 0.001f)
      }

      test("17/32") {
        val f = Fraction(17, 32).roundToNearestFraction()
        f.numerator shouldBe 4
        f.denominator shouldBe 8
        f.decimal shouldBe (0.5f plusOrMinus 0.001f)
      }

      test("0.6542368f") {
        val f = 0.6542368f.toFraction().roundToNearestFraction()
        f.numerator shouldBe 2
        f.denominator shouldBe 3
        f.decimal shouldBe (0.666f plusOrMinus 0.001f)
      }
    }
  }

})