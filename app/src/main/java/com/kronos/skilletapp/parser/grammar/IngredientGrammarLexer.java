// Generated from /home/mmattle/SkilletApp/app/src/main/java/com/kronos/skilletapp/parser/IngredientGrammar.g4 by ANTLR 4.13.1
package com.kronos.skilletapp.parser.grammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class IngredientGrammarLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, WORD=3, WHITESPACE=4, NUMBER=5, COMMENT_START=6, NEWLINE=7, 
		ANY=8;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "LOWERCASE", "UPPERCASE", "DIGIT", "WORD", "WHITESPACE", 
			"NUMBER", "COMMENT_START", "NEWLINE", "ANY"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'/'", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, "WORD", "WHITESPACE", "NUMBER", "COMMENT_START", "NEWLINE", 
			"ANY"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public IngredientGrammarLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "IngredientGrammar.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\bD\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0004\u0005%\b\u0005\u000b\u0005\f\u0005&\u0001\u0006\u0001\u0006\u0001"+
		"\u0007\u0004\u0007,\b\u0007\u000b\u0007\f\u0007-\u0001\u0007\u0001\u0007"+
		"\u0004\u00072\b\u0007\u000b\u0007\f\u00073\u0003\u00076\b\u0007\u0001"+
		"\b\u0001\b\u0001\t\u0003\t;\b\t\u0001\t\u0001\t\u0004\t?\b\t\u000b\t\f"+
		"\t@\u0001\n\u0001\n\u0000\u0000\u000b\u0001\u0001\u0003\u0002\u0005\u0000"+
		"\u0007\u0000\t\u0000\u000b\u0003\r\u0004\u000f\u0005\u0011\u0006\u0013"+
		"\u0007\u0015\b\u0001\u0000\u0006\u0001\u0000az\u0001\u0000AZ\u0001\u0000"+
		"09\u0002\u0000\t\t  \u0002\u0000,,..\u0002\u0000((,,I\u0000\u0001\u0001"+
		"\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u000b\u0001"+
		"\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000"+
		"\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000"+
		"\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0001\u0017\u0001\u0000"+
		"\u0000\u0000\u0003\u0019\u0001\u0000\u0000\u0000\u0005\u001b\u0001\u0000"+
		"\u0000\u0000\u0007\u001d\u0001\u0000\u0000\u0000\t\u001f\u0001\u0000\u0000"+
		"\u0000\u000b$\u0001\u0000\u0000\u0000\r(\u0001\u0000\u0000\u0000\u000f"+
		"+\u0001\u0000\u0000\u0000\u00117\u0001\u0000\u0000\u0000\u0013>\u0001"+
		"\u0000\u0000\u0000\u0015B\u0001\u0000\u0000\u0000\u0017\u0018\u0005/\u0000"+
		"\u0000\u0018\u0002\u0001\u0000\u0000\u0000\u0019\u001a\u0005)\u0000\u0000"+
		"\u001a\u0004\u0001\u0000\u0000\u0000\u001b\u001c\u0007\u0000\u0000\u0000"+
		"\u001c\u0006\u0001\u0000\u0000\u0000\u001d\u001e\u0007\u0001\u0000\u0000"+
		"\u001e\b\u0001\u0000\u0000\u0000\u001f \u0007\u0002\u0000\u0000 \n\u0001"+
		"\u0000\u0000\u0000!%\u0003\u0005\u0002\u0000\"%\u0003\u0007\u0003\u0000"+
		"#%\u0005_\u0000\u0000$!\u0001\u0000\u0000\u0000$\"\u0001\u0000\u0000\u0000"+
		"$#\u0001\u0000\u0000\u0000%&\u0001\u0000\u0000\u0000&$\u0001\u0000\u0000"+
		"\u0000&\'\u0001\u0000\u0000\u0000\'\f\u0001\u0000\u0000\u0000()\u0007"+
		"\u0003\u0000\u0000)\u000e\u0001\u0000\u0000\u0000*,\u0003\t\u0004\u0000"+
		"+*\u0001\u0000\u0000\u0000,-\u0001\u0000\u0000\u0000-+\u0001\u0000\u0000"+
		"\u0000-.\u0001\u0000\u0000\u0000.5\u0001\u0000\u0000\u0000/1\u0007\u0004"+
		"\u0000\u000002\u0003\t\u0004\u000010\u0001\u0000\u0000\u000023\u0001\u0000"+
		"\u0000\u000031\u0001\u0000\u0000\u000034\u0001\u0000\u0000\u000046\u0001"+
		"\u0000\u0000\u00005/\u0001\u0000\u0000\u000056\u0001\u0000\u0000\u0000"+
		"6\u0010\u0001\u0000\u0000\u000078\u0007\u0005\u0000\u00008\u0012\u0001"+
		"\u0000\u0000\u00009;\u0005\r\u0000\u0000:9\u0001\u0000\u0000\u0000:;\u0001"+
		"\u0000\u0000\u0000;<\u0001\u0000\u0000\u0000<?\u0005\n\u0000\u0000=?\u0005"+
		"\r\u0000\u0000>:\u0001\u0000\u0000\u0000>=\u0001\u0000\u0000\u0000?@\u0001"+
		"\u0000\u0000\u0000@>\u0001\u0000\u0000\u0000@A\u0001\u0000\u0000\u0000"+
		"A\u0014\u0001\u0000\u0000\u0000BC\t\u0000\u0000\u0000C\u0016\u0001\u0000"+
		"\u0000\u0000\t\u0000$&-35:>@\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}