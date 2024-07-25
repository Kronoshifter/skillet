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
		T__0=1, T__1=2, T__2=3, WORD=4, WHITESPACE=5, NUMBER=6, COMMENT_START=7, 
		NEWLINE=8, ANY=9;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "LOWERCASE", "UPPERCASE", "DIGIT", "WORD", "WHITESPACE", 
			"NUMBER", "COMMENT_START", "NEWLINE", "ANY"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'/'", "'-'", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, "WORD", "WHITESPACE", "NUMBER", "COMMENT_START", 
			"NEWLINE", "ANY"
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
		"\u0004\u0000\tH\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002"+
		"\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005"+
		"\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0004\u0006)\b\u0006"+
		"\u000b\u0006\f\u0006*\u0001\u0007\u0001\u0007\u0001\b\u0004\b0\b\b\u000b"+
		"\b\f\b1\u0001\b\u0001\b\u0004\b6\b\b\u000b\b\f\b7\u0003\b:\b\b\u0001\t"+
		"\u0001\t\u0001\n\u0003\n?\b\n\u0001\n\u0001\n\u0004\nC\b\n\u000b\n\f\n"+
		"D\u0001\u000b\u0001\u000b\u0000\u0000\f\u0001\u0001\u0003\u0002\u0005"+
		"\u0003\u0007\u0000\t\u0000\u000b\u0000\r\u0004\u000f\u0005\u0011\u0006"+
		"\u0013\u0007\u0015\b\u0017\t\u0001\u0000\u0006\u0001\u0000az\u0001\u0000"+
		"AZ\u0001\u000009\u0002\u0000\t\t  \u0002\u0000,,..\u0002\u0000((,,M\u0000"+
		"\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000"+
		"\u0005\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f"+
		"\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013"+
		"\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017"+
		"\u0001\u0000\u0000\u0000\u0001\u0019\u0001\u0000\u0000\u0000\u0003\u001b"+
		"\u0001\u0000\u0000\u0000\u0005\u001d\u0001\u0000\u0000\u0000\u0007\u001f"+
		"\u0001\u0000\u0000\u0000\t!\u0001\u0000\u0000\u0000\u000b#\u0001\u0000"+
		"\u0000\u0000\r(\u0001\u0000\u0000\u0000\u000f,\u0001\u0000\u0000\u0000"+
		"\u0011/\u0001\u0000\u0000\u0000\u0013;\u0001\u0000\u0000\u0000\u0015B"+
		"\u0001\u0000\u0000\u0000\u0017F\u0001\u0000\u0000\u0000\u0019\u001a\u0005"+
		"/\u0000\u0000\u001a\u0002\u0001\u0000\u0000\u0000\u001b\u001c\u0005-\u0000"+
		"\u0000\u001c\u0004\u0001\u0000\u0000\u0000\u001d\u001e\u0005)\u0000\u0000"+
		"\u001e\u0006\u0001\u0000\u0000\u0000\u001f \u0007\u0000\u0000\u0000 \b"+
		"\u0001\u0000\u0000\u0000!\"\u0007\u0001\u0000\u0000\"\n\u0001\u0000\u0000"+
		"\u0000#$\u0007\u0002\u0000\u0000$\f\u0001\u0000\u0000\u0000%)\u0003\u0007"+
		"\u0003\u0000&)\u0003\t\u0004\u0000\')\u0005_\u0000\u0000(%\u0001\u0000"+
		"\u0000\u0000(&\u0001\u0000\u0000\u0000(\'\u0001\u0000\u0000\u0000)*\u0001"+
		"\u0000\u0000\u0000*(\u0001\u0000\u0000\u0000*+\u0001\u0000\u0000\u0000"+
		"+\u000e\u0001\u0000\u0000\u0000,-\u0007\u0003\u0000\u0000-\u0010\u0001"+
		"\u0000\u0000\u0000.0\u0003\u000b\u0005\u0000/.\u0001\u0000\u0000\u0000"+
		"01\u0001\u0000\u0000\u00001/\u0001\u0000\u0000\u000012\u0001\u0000\u0000"+
		"\u000029\u0001\u0000\u0000\u000035\u0007\u0004\u0000\u000046\u0003\u000b"+
		"\u0005\u000054\u0001\u0000\u0000\u000067\u0001\u0000\u0000\u000075\u0001"+
		"\u0000\u0000\u000078\u0001\u0000\u0000\u00008:\u0001\u0000\u0000\u0000"+
		"93\u0001\u0000\u0000\u00009:\u0001\u0000\u0000\u0000:\u0012\u0001\u0000"+
		"\u0000\u0000;<\u0007\u0005\u0000\u0000<\u0014\u0001\u0000\u0000\u0000"+
		"=?\u0005\r\u0000\u0000>=\u0001\u0000\u0000\u0000>?\u0001\u0000\u0000\u0000"+
		"?@\u0001\u0000\u0000\u0000@C\u0005\n\u0000\u0000AC\u0005\r\u0000\u0000"+
		"B>\u0001\u0000\u0000\u0000BA\u0001\u0000\u0000\u0000CD\u0001\u0000\u0000"+
		"\u0000DB\u0001\u0000\u0000\u0000DE\u0001\u0000\u0000\u0000E\u0016\u0001"+
		"\u0000\u0000\u0000FG\t\u0000\u0000\u0000G\u0018\u0001\u0000\u0000\u0000"+
		"\t\u0000(*179>BD\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}