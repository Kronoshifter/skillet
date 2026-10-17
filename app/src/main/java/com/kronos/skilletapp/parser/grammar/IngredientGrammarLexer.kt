// Generated from /home/mmattle/skillet/app/src/main/java/com/kronos/skilletapp/parser/IngredientGrammar.g4 by ANTLR 4.13.2
@file:Suppress("detekt:all")

package com.kronos.skilletapp.parser.grammar

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.RuntimeMetaData
import org.antlr.v4.runtime.Vocabulary
import org.antlr.v4.runtime.VocabularyImpl
import org.antlr.v4.runtime.atn.ATN
import org.antlr.v4.runtime.atn.ATNDeserializer
import org.antlr.v4.runtime.atn.LexerATNSimulator
import org.antlr.v4.runtime.atn.PredictionContextCache
import org.antlr.v4.runtime.dfa.DFA

@Suppress("unused")
class IngredientGrammarLexer(input: CharStream?) : Lexer(input) {
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

  override fun getChannelNames(): Array<String?> {
    return Companion.channelNames
  }

  override fun getModeNames(): Array<String?> {
    return Companion.modeNames
  }

  override fun getATN(): ATN {
    return _ATN
  }

  init {
    _interp = LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache)
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
    var channelNames: Array<String?> =
      arrayOf<String?>(
        "DEFAULT_TOKEN_CHANNEL",
        "HIDDEN",
      )

    var modeNames: Array<String?> = arrayOf<String?>("DEFAULT_MODE")

    private fun makeRuleNames(): Array<String?> {
      return arrayOf<String?>(
        "T__0",
        "T__1",
        "LOWERCASE",
        "UPPERCASE",
        "DIGIT",
        "WORD",
        "WHITESPACE",
        "NUMBER",
        "COMMENT_START",
        "NEWLINE",
        "ANY",
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
      "\u0004\u0000\bD\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001" +
        "\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004" +
        "\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007" +
        "\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0001\u0000" +
        "\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003" +
        "\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005" +
        "\u0004\u0005%\b\u0005\u000b\u0005\u000c\u0005&\u0001\u0006\u0001\u0006\u0001" +
        "\u0007\u0004\u0007,\b\u0007\u000b\u0007\u000c\u0007-\u0001\u0007\u0001\u0007" +
        "\u0004\u00072\b\u0007\u000b\u0007\u000c\u00073\u0003\u00076\b\u0007\u0001" +
        "\b\u0001\b\u0001\t\u0003\t;\b\t\u0001\t\u0001\t\u0004\t?\b\t\u000b\t\u000c" +
        "\t@\u0001\n\u0001\n\u0000\u0000\u000b\u0001\u0001\u0003\u0002\u0005\u0000" +
        "\u0007\u0000\t\u0000\u000b\u0003\r\u0004\u000f\u0005\u0011\u0006\u0013" +
        "\u0007\u0015\b\u0001\u0000\u0007\u0001\u0000az\u0001\u0000AZ\u0001\u0000" +
        "09\u0002\u0000--__\u0002\u0000\t\t  \u0002\u0000,,..\u0002\u0000((,,I" +
        "\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000" +
        "\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000" +
        "\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000" +
        "\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0001" +
        "\u0017\u0001\u0000\u0000\u0000\u0003\u0019\u0001\u0000\u0000\u0000\u0005" +
        "\u001b\u0001\u0000\u0000\u0000\u0007\u001d\u0001\u0000\u0000\u0000\t\u001f" +
        "\u0001\u0000\u0000\u0000\u000b$\u0001\u0000\u0000\u0000\r(\u0001\u0000" +
        "\u0000\u0000\u000f+\u0001\u0000\u0000\u0000\u00117\u0001\u0000\u0000\u0000" +
        "\u0013>\u0001\u0000\u0000\u0000\u0015B\u0001\u0000\u0000\u0000\u0017\u0018" +
        "\u0005/\u0000\u0000\u0018\u0002\u0001\u0000\u0000\u0000\u0019\u001a\u0005" +
        "-\u0000\u0000\u001a\u0004\u0001\u0000\u0000\u0000\u001b\u001c\u0007\u0000" +
        "\u0000\u0000\u001c\u0006\u0001\u0000\u0000\u0000\u001d\u001e\u0007\u0001" +
        "\u0000\u0000\u001e\b\u0001\u0000\u0000\u0000\u001f \u0007\u0002\u0000" +
        "\u0000 \n\u0001\u0000\u0000\u0000!%\u0003\u0005\u0002\u0000\"%\u0003\u0007" +
        "\u0003\u0000#%\u0007\u0003\u0000\u0000$!\u0001\u0000\u0000\u0000$\"\u0001" +
        "\u0000\u0000\u0000$#\u0001\u0000\u0000\u0000%&\u0001\u0000\u0000\u0000" +
        "&$\u0001\u0000\u0000\u0000&\'\u0001\u0000\u0000\u0000\'\u000c\u0001\u0000" +
        "\u0000\u0000()\u0007\u0004\u0000\u0000)\u000e\u0001\u0000\u0000\u0000" +
        "*,\u0003\t\u0004\u0000+*\u0001\u0000\u0000\u0000,-\u0001\u0000\u0000\u0000" +
        "-+\u0001\u0000\u0000\u0000-.\u0001\u0000\u0000\u0000.5\u0001\u0000\u0000" +
        "\u0000/1\u0007\u0005\u0000\u000002\u0003\t\u0004\u000010\u0001\u0000\u0000" +
        "\u000023\u0001\u0000\u0000\u000031\u0001\u0000\u0000\u000034\u0001\u0000" +
        "\u0000\u000046\u0001\u0000\u0000\u00005/\u0001\u0000\u0000\u000056\u0001" +
        "\u0000\u0000\u00006\u0010\u0001\u0000\u0000\u000078\u0007\u0006\u0000" +
        "\u00008\u0012\u0001\u0000\u0000\u00009;\u0005\r\u0000\u0000:9\u0001\u0000" +
        "\u0000\u0000:;\u0001\u0000\u0000\u0000;<\u0001\u0000\u0000\u0000<?\u0005" +
        "\n\u0000\u0000=?\u0005\r\u0000\u0000>:\u0001\u0000\u0000\u0000>=\u0001" +
        "\u0000\u0000\u0000?@\u0001\u0000\u0000\u0000@>\u0001\u0000\u0000\u0000" +
        "@A\u0001\u0000\u0000\u0000A\u0014\u0001\u0000\u0000\u0000BC\t\u0000\u0000" +
        "\u0000C\u0016\u0001\u0000\u0000\u0000\t\u0000$&-35:>@\u0000"
    val _ATN: ATN = ATNDeserializer().deserialize(_serializedATN.toCharArray())

    init {
      _decisionToDFA = arrayOfNulls<DFA>(_ATN.getNumberOfDecisions())
      for (i in 0..<_ATN.getNumberOfDecisions()) {
        _decisionToDFA[i] = DFA(_ATN.getDecisionState(i), i)
      }
    }
  }
}
