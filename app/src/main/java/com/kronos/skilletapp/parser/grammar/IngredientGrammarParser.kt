// Generated from /home/mmattle/skillet/app/src/main/java/com/kronos/skilletapp/parser/IngredientGrammar.g4 by ANTLR 4.13.2
@file:Suppress("detekt:all")

package com.kronos.skilletapp.parser.grammar

import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.RuntimeMetaData
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.Vocabulary
import org.antlr.v4.runtime.VocabularyImpl
import org.antlr.v4.runtime.atn.ATN
import org.antlr.v4.runtime.atn.ATNDeserializer
import org.antlr.v4.runtime.atn.ParserATNSimulator
import org.antlr.v4.runtime.atn.PredictionContextCache
import org.antlr.v4.runtime.dfa.DFA
import org.antlr.v4.runtime.tree.ParseTreeListener
import org.antlr.v4.runtime.tree.ParseTreeVisitor
import org.antlr.v4.runtime.tree.TerminalNode

@Suppress("unused")
class IngredientGrammarParser(input: TokenStream?) : Parser(input) {
  @Deprecated("")
  override fun getTokenNames(): Array<String?> {
    return Companion.tokenNames
  }

  override fun getVocabulary(): Vocabulary {
    return VOCABULARY
  }

  override fun getGrammarFileName(): String {
    return "IngredientGrammar.g4"
  }

  override fun getRuleNames(): Array<String?> {
    return Companion.ruleNames
  }

  override fun getSerializedATN(): String {
    return _serializedATN
  }

  override fun getATN(): ATN {
    return _ATN
  }

  class RecipeContext(parent: ParserRuleContext?, invokingState: Int) : ParserRuleContext(parent, invokingState) {
    fun EOF(): TerminalNode? {
      return getToken(EOF, 0)
    }

    fun ingredient(): MutableList<IngredientContext?>? {
      return getRuleContexts<IngredientContext?>(IngredientContext::class.java)
    }

    fun ingredient(i: Int): IngredientContext? {
      return getRuleContext<IngredientContext?>(IngredientContext::class.java, i)
    }

    override fun getRuleIndex(): Int {
      return RULE_recipe
    }

    override fun enterRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.enterRecipe(this)
    }

    override fun exitRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.exitRecipe(this)
    }

    override fun <T> accept(visitor: ParseTreeVisitor<out T?>): T? {
      if (visitor is IngredientGrammarVisitor<*>) return (visitor as IngredientGrammarVisitor<out T?>).visitRecipe(this)
      else return visitor.visitChildren(this)
    }
  }

  @Throws(RecognitionException::class)
  fun recipe(): RecipeContext {
    val _localctx = RecipeContext(_ctx, getState())
    enterRule(_localctx, 0, RULE_recipe)
    var _la: Int
    try {
      enterOuterAlt(_localctx, 1)
      run {
        setState(19)
        _errHandler.sync(this)
        _la = _input.LA(1)
        do {
          run {
            run {
              setState(18)
              ingredient()
            }
          }
          setState(21)
          _errHandler.sync(this)
          _la = _input.LA(1)
        } while ((((_la) and 0x3f.inv()) == 0 && ((1L shl _la) and 56L) != 0L))
        setState(23)
        match(EOF)
      }
    } catch (re: RecognitionException) {
      _localctx.exception = re
      _errHandler.reportError(this, re)
      _errHandler.recover(this, re)
    } finally {
      exitRule()
    }
    return _localctx
  }

  class IngredientContext(parent: ParserRuleContext?, invokingState: Int) : ParserRuleContext(parent, invokingState) {
    fun name(): NameContext? {
      return getRuleContext<NameContext?>(NameContext::class.java, 0)
    }

    fun NEWLINE(): TerminalNode? {
      return getToken(NEWLINE, 0)
    }

    fun measurement(): MeasurementContext? {
      return getRuleContext<MeasurementContext?>(MeasurementContext::class.java, 0)
    }

    fun WHITESPACE(): TerminalNode? {
      return getToken(WHITESPACE, 0)
    }

    fun comment(): CommentContext? {
      return getRuleContext<CommentContext?>(CommentContext::class.java, 0)
    }

    override fun getRuleIndex(): Int {
      return RULE_ingredient
    }

    override fun enterRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.enterIngredient(this)
    }

    override fun exitRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.exitIngredient(this)
    }

    override fun <T> accept(visitor: ParseTreeVisitor<out T?>): T? {
      if (visitor is IngredientGrammarVisitor<*>) return (visitor as IngredientGrammarVisitor<out T?>).visitIngredient(this)
      else return visitor.visitChildren(this)
    }
  }

  @Throws(RecognitionException::class)
  fun ingredient(): IngredientContext {
    val _localctx = IngredientContext(_ctx, getState())
    enterRule(_localctx, 2, RULE_ingredient)
    var _la: Int
    try {
      enterOuterAlt(_localctx, 1)
      run {
        setState(28)
        _errHandler.sync(this)
        _la = _input.LA(1)
        if (_la == NUMBER) {
          run {
            setState(25)
            measurement()
            setState(26)
            match(WHITESPACE)
          }
        }

        setState(30)
        name()
        setState(32)
        _errHandler.sync(this)
        _la = _input.LA(1)
        if (_la == COMMENT_START) {
          run {
            setState(31)
            comment()
          }
        }

        setState(34)
        match(NEWLINE)
      }
    } catch (re: RecognitionException) {
      _localctx.exception = re
      _errHandler.reportError(this, re)
      _errHandler.recover(this, re)
    } finally {
      exitRule()
    }
    return _localctx
  }

  class MeasurementContext(parent: ParserRuleContext?, invokingState: Int) : ParserRuleContext(parent, invokingState) {
    fun quantity(): QuantityContext? {
      return getRuleContext<QuantityContext?>(QuantityContext::class.java, 0)
    }

    fun WHITESPACE(): TerminalNode? {
      return getToken(WHITESPACE, 0)
    }

    fun WORD(): TerminalNode? {
      return getToken(WORD, 0)
    }

    override fun getRuleIndex(): Int {
      return RULE_measurement
    }

    override fun enterRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.enterMeasurement(this)
    }

    override fun exitRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.exitMeasurement(this)
    }

    override fun <T> accept(visitor: ParseTreeVisitor<out T?>): T? {
      if (visitor is IngredientGrammarVisitor<*>) return (visitor as IngredientGrammarVisitor<out T?>).visitMeasurement(this)
      else return visitor.visitChildren(this)
    }
  }

  @Throws(RecognitionException::class)
  fun measurement(): MeasurementContext {
    val _localctx = MeasurementContext(_ctx, getState())
    enterRule(_localctx, 4, RULE_measurement)
    try {
      enterOuterAlt(_localctx, 1)
      run {
        setState(36)
        quantity()
        setState(39)
        _errHandler.sync(this)
        when (getInterpreter().adaptivePredict(_input, 3, _ctx)) {
          1 -> {
            setState(37)
            match(WHITESPACE)
            setState(38)
            match(WORD)
          }
        }
      }
    } catch (re: RecognitionException) {
      _localctx.exception = re
      _errHandler.reportError(this, re)
      _errHandler.recover(this, re)
    } finally {
      exitRule()
    }
    return _localctx
  }

  class QuantityContext(parent: ParserRuleContext?, invokingState: Int) : ParserRuleContext(parent, invokingState) {
    fun decimal(): DecimalContext? {
      return getRuleContext<DecimalContext?>(DecimalContext::class.java, 0)
    }

    fun fraction(): FractionContext? {
      return getRuleContext<FractionContext?>(FractionContext::class.java, 0)
    }

    fun range(): RangeContext? {
      return getRuleContext<RangeContext?>(RangeContext::class.java, 0)
    }

    override fun getRuleIndex(): Int {
      return RULE_quantity
    }

    override fun enterRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.enterQuantity(this)
    }

    override fun exitRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.exitQuantity(this)
    }

    override fun <T> accept(visitor: ParseTreeVisitor<out T?>): T? {
      if (visitor is IngredientGrammarVisitor<*>) return (visitor as IngredientGrammarVisitor<out T?>).visitQuantity(this)
      else return visitor.visitChildren(this)
    }
  }

  @Throws(RecognitionException::class)
  fun quantity(): QuantityContext {
    val _localctx = QuantityContext(_ctx, getState())
    enterRule(_localctx, 6, RULE_quantity)
    try {
      enterOuterAlt(_localctx, 1)
      run {
        setState(44)
        _errHandler.sync(this)
        when (getInterpreter().adaptivePredict(_input, 4, _ctx)) {
          1 -> {
            setState(41)
            decimal()
          }
          2 -> {
            setState(42)
            fraction()
          }
          3 -> {
            setState(43)
            range()
          }
        }
      }
    } catch (re: RecognitionException) {
      _localctx.exception = re
      _errHandler.reportError(this, re)
      _errHandler.recover(this, re)
    } finally {
      exitRule()
    }
    return _localctx
  }

  class DecimalContext(parent: ParserRuleContext?, invokingState: Int) : ParserRuleContext(parent, invokingState) {
    fun NUMBER(): TerminalNode? {
      return getToken(NUMBER, 0)
    }

    override fun getRuleIndex(): Int {
      return RULE_decimal
    }

    override fun enterRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.enterDecimal(this)
    }

    override fun exitRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.exitDecimal(this)
    }

    override fun <T> accept(visitor: ParseTreeVisitor<out T?>): T? {
      if (visitor is IngredientGrammarVisitor<*>) return (visitor as IngredientGrammarVisitor<out T?>).visitDecimal(this)
      else return visitor.visitChildren(this)
    }
  }

  @Throws(RecognitionException::class)
  fun decimal(): DecimalContext {
    val _localctx = DecimalContext(_ctx, getState())
    enterRule(_localctx, 8, RULE_decimal)
    try {
      enterOuterAlt(_localctx, 1)
      run {
        setState(46)
        match(NUMBER)
      }
    } catch (re: RecognitionException) {
      _localctx.exception = re
      _errHandler.reportError(this, re)
      _errHandler.recover(this, re)
    } finally {
      exitRule()
    }
    return _localctx
  }

  class FractionContext(parent: ParserRuleContext?, invokingState: Int) : ParserRuleContext(parent, invokingState) {
    fun NUMBER(): MutableList<TerminalNode?>? {
      return getTokens(NUMBER)
    }

    fun NUMBER(i: Int): TerminalNode? {
      return getToken(NUMBER, i)
    }

    fun WHITESPACE(): MutableList<TerminalNode?>? {
      return getTokens(WHITESPACE)
    }

    fun WHITESPACE(i: Int): TerminalNode? {
      return getToken(WHITESPACE, i)
    }

    override fun getRuleIndex(): Int {
      return RULE_fraction
    }

    override fun enterRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.enterFraction(this)
    }

    override fun exitRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.exitFraction(this)
    }

    override fun <T> accept(visitor: ParseTreeVisitor<out T?>): T? {
      if (visitor is IngredientGrammarVisitor<*>) return (visitor as IngredientGrammarVisitor<out T?>).visitFraction(this)
      else return visitor.visitChildren(this)
    }
  }

  @Throws(RecognitionException::class)
  fun fraction(): FractionContext {
    val _localctx = FractionContext(_ctx, getState())
    enterRule(_localctx, 10, RULE_fraction)
    var _la: Int
    try {
      enterOuterAlt(_localctx, 1)
      run {
        setState(50)
        _errHandler.sync(this)
        when (getInterpreter().adaptivePredict(_input, 5, _ctx)) {
          1 -> {
            setState(48)
            match(NUMBER)
            setState(49)
            match(WHITESPACE)
          }
        }
        setState(52)
        match(NUMBER)
        setState(54)
        _errHandler.sync(this)
        _la = _input.LA(1)
        do {
          run {
            run {
              setState(53)
              _la = _input.LA(1)
              if (!(_la == T__0 || _la == WHITESPACE)) {
                _errHandler.recoverInline(this)
              } else {
                if (_input.LA(1) == Token.EOF) matchedEOF = true
                _errHandler.reportMatch(this)
                consume()
              }
            }
          }
          setState(56)
          _errHandler.sync(this)
          _la = _input.LA(1)
        } while (_la == T__0 || _la == WHITESPACE)
        setState(58)
        match(NUMBER)
      }
    } catch (re: RecognitionException) {
      _localctx.exception = re
      _errHandler.reportError(this, re)
      _errHandler.recover(this, re)
    } finally {
      exitRule()
    }
    return _localctx
  }

  class RangeContext(parent: ParserRuleContext?, invokingState: Int) : ParserRuleContext(parent, invokingState) {
    fun decimal(): MutableList<DecimalContext?>? {
      return getRuleContexts<DecimalContext?>(DecimalContext::class.java)
    }

    fun decimal(i: Int): DecimalContext? {
      return getRuleContext<DecimalContext?>(DecimalContext::class.java, i)
    }

    fun fraction(): MutableList<FractionContext?>? {
      return getRuleContexts<FractionContext?>(FractionContext::class.java)
    }

    fun fraction(i: Int): FractionContext? {
      return getRuleContext<FractionContext?>(FractionContext::class.java, i)
    }

    fun WHITESPACE(): MutableList<TerminalNode?>? {
      return getTokens(WHITESPACE)
    }

    fun WHITESPACE(i: Int): TerminalNode? {
      return getToken(WHITESPACE, i)
    }

    override fun getRuleIndex(): Int {
      return RULE_range
    }

    override fun enterRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.enterRange(this)
    }

    override fun exitRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.exitRange(this)
    }

    override fun <T> accept(visitor: ParseTreeVisitor<out T?>): T? {
      if (visitor is IngredientGrammarVisitor<*>) return (visitor as IngredientGrammarVisitor<out T?>).visitRange(this)
      else return visitor.visitChildren(this)
    }
  }

  @Throws(RecognitionException::class)
  fun range(): RangeContext {
    val _localctx = RangeContext(_ctx, getState())
    enterRule(_localctx, 12, RULE_range)
    var _la: Int
    try {
      enterOuterAlt(_localctx, 1)
      run {
        setState(62)
        _errHandler.sync(this)
        when (getInterpreter().adaptivePredict(_input, 7, _ctx)) {
          1 -> {
            setState(60)
            decimal()
          }
          2 -> {
            setState(61)
            fraction()
          }
        }
        setState(65)
        _errHandler.sync(this)
        _la = _input.LA(1)
        do {
          run {
            run {
              setState(64)
              _la = _input.LA(1)
              if (!(_la == T__1 || _la == WHITESPACE)) {
                _errHandler.recoverInline(this)
              } else {
                if (_input.LA(1) == Token.EOF) matchedEOF = true
                _errHandler.reportMatch(this)
                consume()
              }
            }
          }
          setState(67)
          _errHandler.sync(this)
          _la = _input.LA(1)
        } while (_la == T__1 || _la == WHITESPACE)
        setState(71)
        _errHandler.sync(this)
        when (getInterpreter().adaptivePredict(_input, 9, _ctx)) {
          1 -> {
            setState(69)
            decimal()
          }
          2 -> {
            setState(70)
            fraction()
          }
        }
      }
    } catch (re: RecognitionException) {
      _localctx.exception = re
      _errHandler.reportError(this, re)
      _errHandler.recover(this, re)
    } finally {
      exitRule()
    }
    return _localctx
  }

  class NameContext(parent: ParserRuleContext?, invokingState: Int) : ParserRuleContext(parent, invokingState) {
    fun WORD(): MutableList<TerminalNode?>? {
      return getTokens(WORD)
    }

    fun WORD(i: Int): TerminalNode? {
      return getToken(WORD, i)
    }

    fun WHITESPACE(): MutableList<TerminalNode?>? {
      return getTokens(WHITESPACE)
    }

    fun WHITESPACE(i: Int): TerminalNode? {
      return getToken(WHITESPACE, i)
    }

    override fun getRuleIndex(): Int {
      return RULE_name
    }

    override fun enterRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.enterName(this)
    }

    override fun exitRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.exitName(this)
    }

    override fun <T> accept(visitor: ParseTreeVisitor<out T?>): T? {
      if (visitor is IngredientGrammarVisitor<*>) return (visitor as IngredientGrammarVisitor<out T?>).visitName(this)
      else return visitor.visitChildren(this)
    }
  }

  @Throws(RecognitionException::class)
  fun name(): NameContext {
    val _localctx = NameContext(_ctx, getState())
    enterRule(_localctx, 14, RULE_name)
    var _la: Int
    try {
      var _alt: Int
      enterOuterAlt(_localctx, 1)
      run {
        setState(76)
        _errHandler.sync(this)
        _alt = getInterpreter().adaptivePredict(_input, 10, _ctx)
        while (_alt != 1 && _alt != ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1 + 1) {
            run {
              run {
                setState(73)
                _la = _input.LA(1)
                if (!(_la == WORD || _la == WHITESPACE)) {
                  _errHandler.recoverInline(this)
                } else {
                  if (_input.LA(1) == Token.EOF) matchedEOF = true
                  _errHandler.reportMatch(this)
                  consume()
                }
              }
            }
          }
          setState(78)
          _errHandler.sync(this)
          _alt = getInterpreter().adaptivePredict(_input, 10, _ctx)
        }
        setState(79)
        match(WORD)
        setState(81)
        _errHandler.sync(this)
        _la = _input.LA(1)
        if (_la == WHITESPACE) {
          run {
            setState(80)
            match(WHITESPACE)
          }
        }
      }
    } catch (re: RecognitionException) {
      _localctx.exception = re
      _errHandler.reportError(this, re)
      _errHandler.recover(this, re)
    } finally {
      exitRule()
    }
    return _localctx
  }

  class CommentContext(parent: ParserRuleContext?, invokingState: Int) : ParserRuleContext(parent, invokingState) {
    fun COMMENT_START(): TerminalNode? {
      return getToken(COMMENT_START, 0)
    }

    fun NEWLINE(): MutableList<TerminalNode?>? {
      return getTokens(NEWLINE)
    }

    fun NEWLINE(i: Int): TerminalNode? {
      return getToken(NEWLINE, i)
    }

    override fun getRuleIndex(): Int {
      return RULE_comment
    }

    override fun enterRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.enterComment(this)
    }

    override fun exitRule(listener: ParseTreeListener?) {
      if (listener is IngredientGrammarListener) listener.exitComment(this)
    }

    override fun <T> accept(visitor: ParseTreeVisitor<out T?>): T? {
      if (visitor is IngredientGrammarVisitor<*>) return (visitor as IngredientGrammarVisitor<out T?>).visitComment(this)
      else return visitor.visitChildren(this)
    }
  }

  @Throws(RecognitionException::class)
  fun comment(): CommentContext {
    val _localctx = CommentContext(_ctx, getState())
    enterRule(_localctx, 16, RULE_comment)
    var _la: Int
    try {
      enterOuterAlt(_localctx, 1)
      run {
        setState(83)
        match(COMMENT_START)
        setState(85)
        _errHandler.sync(this)
        _la = _input.LA(1)
        do {
          run {
            run {
              setState(84)
              _la = _input.LA(1)
              if (_la <= 0 || (_la == NEWLINE)) {
                _errHandler.recoverInline(this)
              } else {
                if (_input.LA(1) == Token.EOF) matchedEOF = true
                _errHandler.reportMatch(this)
                consume()
              }
            }
          }
          setState(87)
          _errHandler.sync(this)
          _la = _input.LA(1)
        } while ((((_la) and 0x3f.inv()) == 0 && ((1L shl _la) and 382L) != 0L))
      }
    } catch (re: RecognitionException) {
      _localctx.exception = re
      _errHandler.reportError(this, re)
      _errHandler.recover(this, re)
    } finally {
      exitRule()
    }
    return _localctx
  }

  init {
    _interp = ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache)
  }

  companion object {
    init {
      RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION)
    }

    protected val _decisionToDFA: Array<DFA?>
    protected val _sharedContextCache: PredictionContextCache = PredictionContextCache()
    const val T__0: Int = 1
    const val T__1: Int = 2
    const val WORD: Int = 3
    const val WHITESPACE: Int = 4
    const val NUMBER: Int = 5
    const val COMMENT_START: Int = 6
    const val NEWLINE: Int = 7
    const val ANY: Int = 8
    const val RULE_recipe: Int = 0
    const val RULE_ingredient: Int = 1
    const val RULE_measurement: Int = 2
    const val RULE_quantity: Int = 3
    const val RULE_decimal: Int = 4
    const val RULE_fraction: Int = 5
    const val RULE_range: Int = 6
    const val RULE_name: Int = 7
    const val RULE_comment: Int = 8

    private fun makeRuleNames(): Array<String?> {
      return arrayOf<String?>(
        "recipe",
        "ingredient",
        "measurement",
        "quantity",
        "decimal",
        "fraction",
        "range",
        "name",
        "comment",
      )
    }

    val ruleNames: Array<String?> = makeRuleNames()

    private fun makeLiteralNames(): Array<String?> {
      return arrayOf<String?>(
        null,
        "'/'",
        "'-'",
      )
    }

    private val _LITERAL_NAMES: Array<String?> = makeLiteralNames()

    private fun makeSymbolicNames(): Array<String?> {
      return arrayOf<String?>(
        null,
        null,
        null,
        "WORD",
        "WHITESPACE",
        "NUMBER",
        "COMMENT_START",
        "NEWLINE",
        "ANY",
      )
    }

    private val _SYMBOLIC_NAMES: Array<String?> = makeSymbolicNames()
    val VOCABULARY: Vocabulary = VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES)

    @Deprecated("Use {@link #VOCABULARY} instead.") val tokenNames: Array<String?>

    init {
      tokenNames = arrayOfNulls<String>(_SYMBOLIC_NAMES.size)
      for (i in tokenNames.indices) {
        tokenNames[i] = VOCABULARY.getLiteralName(i)
        if (tokenNames[i] == null) {
          tokenNames[i] = VOCABULARY.getSymbolicName(i)
        }

        if (tokenNames[i] == null) {
          tokenNames[i] = "<INVALID>"
        }
      }
    }

    val _serializedATN: String =
      "\u0004\u0001\bZ\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002" +
        "\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002" +
        "\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002" +
        "\b\u0007\b\u0001\u0000\u0004\u0000\u0014\b\u0000\u000b\u0000\u000c\u0000\u0015" +
        "\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001" +
        "\u001d\b\u0001\u0001\u0001\u0001\u0001\u0003\u0001!\b\u0001\u0001\u0001" +
        "\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002(\b\u0002" +
        "\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003-\b\u0003\u0001\u0004" +
        "\u0001\u0004\u0001\u0005\u0001\u0005\u0003\u00053\b\u0005\u0001\u0005" +
        "\u0001\u0005\u0004\u00057\b\u0005\u000b\u0005\u000c\u00058\u0001\u0005\u0001" +
        "\u0005\u0001\u0006\u0001\u0006\u0003\u0006?\b\u0006\u0001\u0006\u0004" +
        "\u0006B\b\u0006\u000b\u0006\u000c\u0006C\u0001\u0006\u0001\u0006\u0003\u0006" +
        "H\b\u0006\u0001\u0007\u0005\u0007K\b\u0007\n\u0007\u000c\u0007N\t\u0007\u0001" +
        "\u0007\u0001\u0007\u0003\u0007R\b\u0007\u0001\b\u0001\b\u0004\bV\b\b\u000b" +
        "\b\u000c\bW\u0001\b\u0001L\u0000\t\u0000\u0002\u0004\u0006\b\n\u000c\u000e\u0010" +
        "\u0000\u0004\u0002\u0000\u0001\u0001\u0004\u0004\u0002\u0000\u0002\u0002" +
        "\u0004\u0004\u0001\u0000\u0003\u0004\u0001\u0000\u0007\u0007^\u0000\u0013" +
        "\u0001\u0000\u0000\u0000\u0002\u001c\u0001\u0000\u0000\u0000\u0004$\u0001" +
        "\u0000\u0000\u0000\u0006,\u0001\u0000\u0000\u0000\b.\u0001\u0000\u0000" +
        "\u0000\n2\u0001\u0000\u0000\u0000\u000c>\u0001\u0000\u0000\u0000\u000eL\u0001" +
        "\u0000\u0000\u0000\u0010S\u0001\u0000\u0000\u0000\u0012\u0014\u0003\u0002" +
        "\u0001\u0000\u0013\u0012\u0001\u0000\u0000\u0000\u0014\u0015\u0001\u0000" +
        "\u0000\u0000\u0015\u0013\u0001\u0000\u0000\u0000\u0015\u0016\u0001\u0000" +
        "\u0000\u0000\u0016\u0017\u0001\u0000\u0000\u0000\u0017\u0018\u0005\u0000" +
        "\u0000\u0001\u0018\u0001\u0001\u0000\u0000\u0000\u0019\u001a\u0003\u0004" +
        "\u0002\u0000\u001a\u001b\u0005\u0004\u0000\u0000\u001b\u001d\u0001\u0000" +
        "\u0000\u0000\u001c\u0019\u0001\u0000\u0000\u0000\u001c\u001d\u0001\u0000" +
        "\u0000\u0000\u001d\u001e\u0001\u0000\u0000\u0000\u001e \u0003\u000e\u0007" +
        "\u0000\u001f!\u0003\u0010\b\u0000 \u001f\u0001\u0000\u0000\u0000 !\u0001" +
        "\u0000\u0000\u0000!\"\u0001\u0000\u0000\u0000\"#\u0005\u0007\u0000\u0000" +
        "#\u0003\u0001\u0000\u0000\u0000$\'\u0003\u0006\u0003\u0000%&\u0005\u0004" +
        "\u0000\u0000&(\u0005\u0003\u0000\u0000\'%\u0001\u0000\u0000\u0000\'(\u0001" +
        "\u0000\u0000\u0000(\u0005\u0001\u0000\u0000\u0000)-\u0003\b\u0004\u0000" +
        "*-\u0003\n\u0005\u0000+-\u0003\u000c\u0006\u0000,)\u0001\u0000\u0000\u0000" +
        ",*\u0001\u0000\u0000\u0000,+\u0001\u0000\u0000\u0000-\u0007\u0001\u0000" +
        "\u0000\u0000./\u0005\u0005\u0000\u0000/\t\u0001\u0000\u0000\u000001\u0005" +
        "\u0005\u0000\u000013\u0005\u0004\u0000\u000020\u0001\u0000\u0000\u0000" +
        "23\u0001\u0000\u0000\u000034\u0001\u0000\u0000\u000046\u0005\u0005\u0000" +
        "\u000057\u0007\u0000\u0000\u000065\u0001\u0000\u0000\u000078\u0001\u0000" +
        "\u0000\u000086\u0001\u0000\u0000\u000089\u0001\u0000\u0000\u00009:\u0001" +
        "\u0000\u0000\u0000:;\u0005\u0005\u0000\u0000;\u000b\u0001\u0000\u0000" +
        "\u0000<?\u0003\b\u0004\u0000=?\u0003\n\u0005\u0000><\u0001\u0000\u0000" +
        "\u0000>=\u0001\u0000\u0000\u0000?A\u0001\u0000\u0000\u0000@B\u0007\u0001" +
        "\u0000\u0000A@\u0001\u0000\u0000\u0000BC\u0001\u0000\u0000\u0000CA\u0001" +
        "\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000DG\u0001\u0000\u0000\u0000" +
        "EH\u0003\b\u0004\u0000FH\u0003\n\u0005\u0000GE\u0001\u0000\u0000\u0000" +
        "GF\u0001\u0000\u0000\u0000H\r\u0001\u0000\u0000\u0000IK\u0007\u0002\u0000" +
        "\u0000JI\u0001\u0000\u0000\u0000KN\u0001\u0000\u0000\u0000LM\u0001\u0000" +
        "\u0000\u0000LJ\u0001\u0000\u0000\u0000MO\u0001\u0000\u0000\u0000NL\u0001" +
        "\u0000\u0000\u0000OQ\u0005\u0003\u0000\u0000PR\u0005\u0004\u0000\u0000" +
        "QP\u0001\u0000\u0000\u0000QR\u0001\u0000\u0000\u0000R\u000f\u0001\u0000" +
        "\u0000\u0000SU\u0005\u0006\u0000\u0000TV\b\u0003\u0000\u0000UT\u0001\u0000" +
        "\u0000\u0000VW\u0001\u0000\u0000\u0000WU\u0001\u0000\u0000\u0000WX\u0001" +
        "\u0000\u0000\u0000X\u0011\u0001\u0000\u0000\u0000\r\u0015\u001c \',28" +
        ">CGLQW"
    val _ATN: ATN = ATNDeserializer().deserialize(_serializedATN.toCharArray())

    init {
      _decisionToDFA = arrayOfNulls<DFA>(_ATN.getNumberOfDecisions())
      for (i in 0..<_ATN.getNumberOfDecisions()) {
        _decisionToDFA[i] = DFA(_ATN.getDecisionState(i), i)
      }
    }
  }
}
