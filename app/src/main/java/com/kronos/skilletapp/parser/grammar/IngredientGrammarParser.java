// Generated from /home/mmattle/skillet/app/src/main/java/com/kronos/skilletapp/parser/IngredientGrammar.g4 by ANTLR 4.13.2
package com.kronos.skilletapp.parser.grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class IngredientGrammarParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, WORD=3, WHITESPACE=4, NUMBER=5, COMMENT_START=6, NEWLINE=7, 
		ANY=8;
	public static final int
		RULE_recipe = 0, RULE_ingredient = 1, RULE_measurement = 2, RULE_quantity = 3, 
		RULE_decimal = 4, RULE_fraction = 5, RULE_range = 6, RULE_name = 7, RULE_comment = 8;
	private static String[] makeRuleNames() {
		return new String[] {
			"recipe", "ingredient", "measurement", "quantity", "decimal", "fraction", 
			"range", "name", "comment"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'/'", "'-'"
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

	@Override
	public String getGrammarFileName() { return "IngredientGrammar.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public IngredientGrammarParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RecipeContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(IngredientGrammarParser.EOF, 0); }
		public List<IngredientContext> ingredient() {
			return getRuleContexts(IngredientContext.class);
		}
		public IngredientContext ingredient(int i) {
			return getRuleContext(IngredientContext.class,i);
		}
		public RecipeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_recipe; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).enterRecipe(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).exitRecipe(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IngredientGrammarVisitor ) return ((IngredientGrammarVisitor<? extends T>)visitor).visitRecipe(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RecipeContext recipe() throws RecognitionException {
		RecipeContext _localctx = new RecipeContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_recipe);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(19); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(18);
				ingredient();
				}
				}
				setState(21); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 56L) != 0) );
			setState(23);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IngredientContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(IngredientGrammarParser.NEWLINE, 0); }
		public MeasurementContext measurement() {
			return getRuleContext(MeasurementContext.class,0);
		}
		public TerminalNode WHITESPACE() { return getToken(IngredientGrammarParser.WHITESPACE, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public IngredientContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ingredient; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).enterIngredient(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).exitIngredient(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IngredientGrammarVisitor ) return ((IngredientGrammarVisitor<? extends T>)visitor).visitIngredient(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IngredientContext ingredient() throws RecognitionException {
		IngredientContext _localctx = new IngredientContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_ingredient);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NUMBER) {
				{
				setState(25);
				measurement();
				setState(26);
				match(WHITESPACE);
				}
			}

			setState(30);
			name();
			setState(32);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT_START) {
				{
				setState(31);
				comment();
				}
			}

			setState(34);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MeasurementContext extends ParserRuleContext {
		public QuantityContext quantity() {
			return getRuleContext(QuantityContext.class,0);
		}
		public TerminalNode WHITESPACE() { return getToken(IngredientGrammarParser.WHITESPACE, 0); }
		public TerminalNode WORD() { return getToken(IngredientGrammarParser.WORD, 0); }
		public MeasurementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_measurement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).enterMeasurement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).exitMeasurement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IngredientGrammarVisitor ) return ((IngredientGrammarVisitor<? extends T>)visitor).visitMeasurement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MeasurementContext measurement() throws RecognitionException {
		MeasurementContext _localctx = new MeasurementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_measurement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(36);
			quantity();
			setState(39);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				{
				setState(37);
				match(WHITESPACE);
				setState(38);
				match(WORD);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QuantityContext extends ParserRuleContext {
		public DecimalContext decimal() {
			return getRuleContext(DecimalContext.class,0);
		}
		public FractionContext fraction() {
			return getRuleContext(FractionContext.class,0);
		}
		public RangeContext range() {
			return getRuleContext(RangeContext.class,0);
		}
		public QuantityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quantity; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).enterQuantity(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).exitQuantity(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IngredientGrammarVisitor ) return ((IngredientGrammarVisitor<? extends T>)visitor).visitQuantity(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QuantityContext quantity() throws RecognitionException {
		QuantityContext _localctx = new QuantityContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_quantity);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(44);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(41);
				decimal();
				}
				break;
			case 2:
				{
				setState(42);
				fraction();
				}
				break;
			case 3:
				{
				setState(43);
				range();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DecimalContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(IngredientGrammarParser.NUMBER, 0); }
		public DecimalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_decimal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).enterDecimal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).exitDecimal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IngredientGrammarVisitor ) return ((IngredientGrammarVisitor<? extends T>)visitor).visitDecimal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DecimalContext decimal() throws RecognitionException {
		DecimalContext _localctx = new DecimalContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_decimal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FractionContext extends ParserRuleContext {
		public List<TerminalNode> NUMBER() { return getTokens(IngredientGrammarParser.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(IngredientGrammarParser.NUMBER, i);
		}
		public List<TerminalNode> WHITESPACE() { return getTokens(IngredientGrammarParser.WHITESPACE); }
		public TerminalNode WHITESPACE(int i) {
			return getToken(IngredientGrammarParser.WHITESPACE, i);
		}
		public FractionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fraction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).enterFraction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).exitFraction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IngredientGrammarVisitor ) return ((IngredientGrammarVisitor<? extends T>)visitor).visitFraction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FractionContext fraction() throws RecognitionException {
		FractionContext _localctx = new FractionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_fraction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(48);
				match(NUMBER);
				setState(49);
				match(WHITESPACE);
				}
				break;
			}
			setState(52);
			match(NUMBER);
			setState(54); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(53);
				_la = _input.LA(1);
				if ( !(_la==T__0 || _la==WHITESPACE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(56); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__0 || _la==WHITESPACE );
			setState(58);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RangeContext extends ParserRuleContext {
		public List<DecimalContext> decimal() {
			return getRuleContexts(DecimalContext.class);
		}
		public DecimalContext decimal(int i) {
			return getRuleContext(DecimalContext.class,i);
		}
		public List<FractionContext> fraction() {
			return getRuleContexts(FractionContext.class);
		}
		public FractionContext fraction(int i) {
			return getRuleContext(FractionContext.class,i);
		}
		public List<TerminalNode> WHITESPACE() { return getTokens(IngredientGrammarParser.WHITESPACE); }
		public TerminalNode WHITESPACE(int i) {
			return getToken(IngredientGrammarParser.WHITESPACE, i);
		}
		public RangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_range; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).enterRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).exitRange(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IngredientGrammarVisitor ) return ((IngredientGrammarVisitor<? extends T>)visitor).visitRange(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangeContext range() throws RecognitionException {
		RangeContext _localctx = new RangeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_range);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(60);
				decimal();
				}
				break;
			case 2:
				{
				setState(61);
				fraction();
				}
				break;
			}
			setState(65); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(64);
				_la = _input.LA(1);
				if ( !(_la==T__1 || _la==WHITESPACE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(67); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__1 || _la==WHITESPACE );
			setState(71);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(69);
				decimal();
				}
				break;
			case 2:
				{
				setState(70);
				fraction();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NameContext extends ParserRuleContext {
		public List<TerminalNode> WORD() { return getTokens(IngredientGrammarParser.WORD); }
		public TerminalNode WORD(int i) {
			return getToken(IngredientGrammarParser.WORD, i);
		}
		public List<TerminalNode> WHITESPACE() { return getTokens(IngredientGrammarParser.WHITESPACE); }
		public TerminalNode WHITESPACE(int i) {
			return getToken(IngredientGrammarParser.WHITESPACE, i);
		}
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).exitName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IngredientGrammarVisitor ) return ((IngredientGrammarVisitor<? extends T>)visitor).visitName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_name);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(73);
					_la = _input.LA(1);
					if ( !(_la==WORD || _la==WHITESPACE) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					} 
				}
				setState(78);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			}
			setState(79);
			match(WORD);
			setState(81);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHITESPACE) {
				{
				setState(80);
				match(WHITESPACE);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CommentContext extends ParserRuleContext {
		public TerminalNode COMMENT_START() { return getToken(IngredientGrammarParser.COMMENT_START, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(IngredientGrammarParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(IngredientGrammarParser.NEWLINE, i);
		}
		public CommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).enterComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IngredientGrammarListener ) ((IngredientGrammarListener)listener).exitComment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IngredientGrammarVisitor ) return ((IngredientGrammarVisitor<? extends T>)visitor).visitComment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentContext comment() throws RecognitionException {
		CommentContext _localctx = new CommentContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_comment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			match(COMMENT_START);
			setState(85); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(84);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==NEWLINE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(87); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 382L) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\bZ\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0001\u0000\u0004\u0000\u0014\b\u0000\u000b\u0000\f\u0000\u0015"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001"+
		"\u001d\b\u0001\u0001\u0001\u0001\u0001\u0003\u0001!\b\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002(\b\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003-\b\u0003\u0001\u0004"+
		"\u0001\u0004\u0001\u0005\u0001\u0005\u0003\u00053\b\u0005\u0001\u0005"+
		"\u0001\u0005\u0004\u00057\b\u0005\u000b\u0005\f\u00058\u0001\u0005\u0001"+
		"\u0005\u0001\u0006\u0001\u0006\u0003\u0006?\b\u0006\u0001\u0006\u0004"+
		"\u0006B\b\u0006\u000b\u0006\f\u0006C\u0001\u0006\u0001\u0006\u0003\u0006"+
		"H\b\u0006\u0001\u0007\u0005\u0007K\b\u0007\n\u0007\f\u0007N\t\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007R\b\u0007\u0001\b\u0001\b\u0004\bV\b\b\u000b"+
		"\b\f\bW\u0001\b\u0001L\u0000\t\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010"+
		"\u0000\u0004\u0002\u0000\u0001\u0001\u0004\u0004\u0002\u0000\u0002\u0002"+
		"\u0004\u0004\u0001\u0000\u0003\u0004\u0001\u0000\u0007\u0007^\u0000\u0013"+
		"\u0001\u0000\u0000\u0000\u0002\u001c\u0001\u0000\u0000\u0000\u0004$\u0001"+
		"\u0000\u0000\u0000\u0006,\u0001\u0000\u0000\u0000\b.\u0001\u0000\u0000"+
		"\u0000\n2\u0001\u0000\u0000\u0000\f>\u0001\u0000\u0000\u0000\u000eL\u0001"+
		"\u0000\u0000\u0000\u0010S\u0001\u0000\u0000\u0000\u0012\u0014\u0003\u0002"+
		"\u0001\u0000\u0013\u0012\u0001\u0000\u0000\u0000\u0014\u0015\u0001\u0000"+
		"\u0000\u0000\u0015\u0013\u0001\u0000\u0000\u0000\u0015\u0016\u0001\u0000"+
		"\u0000\u0000\u0016\u0017\u0001\u0000\u0000\u0000\u0017\u0018\u0005\u0000"+
		"\u0000\u0001\u0018\u0001\u0001\u0000\u0000\u0000\u0019\u001a\u0003\u0004"+
		"\u0002\u0000\u001a\u001b\u0005\u0004\u0000\u0000\u001b\u001d\u0001\u0000"+
		"\u0000\u0000\u001c\u0019\u0001\u0000\u0000\u0000\u001c\u001d\u0001\u0000"+
		"\u0000\u0000\u001d\u001e\u0001\u0000\u0000\u0000\u001e \u0003\u000e\u0007"+
		"\u0000\u001f!\u0003\u0010\b\u0000 \u001f\u0001\u0000\u0000\u0000 !\u0001"+
		"\u0000\u0000\u0000!\"\u0001\u0000\u0000\u0000\"#\u0005\u0007\u0000\u0000"+
		"#\u0003\u0001\u0000\u0000\u0000$\'\u0003\u0006\u0003\u0000%&\u0005\u0004"+
		"\u0000\u0000&(\u0005\u0003\u0000\u0000\'%\u0001\u0000\u0000\u0000\'(\u0001"+
		"\u0000\u0000\u0000(\u0005\u0001\u0000\u0000\u0000)-\u0003\b\u0004\u0000"+
		"*-\u0003\n\u0005\u0000+-\u0003\f\u0006\u0000,)\u0001\u0000\u0000\u0000"+
		",*\u0001\u0000\u0000\u0000,+\u0001\u0000\u0000\u0000-\u0007\u0001\u0000"+
		"\u0000\u0000./\u0005\u0005\u0000\u0000/\t\u0001\u0000\u0000\u000001\u0005"+
		"\u0005\u0000\u000013\u0005\u0004\u0000\u000020\u0001\u0000\u0000\u0000"+
		"23\u0001\u0000\u0000\u000034\u0001\u0000\u0000\u000046\u0005\u0005\u0000"+
		"\u000057\u0007\u0000\u0000\u000065\u0001\u0000\u0000\u000078\u0001\u0000"+
		"\u0000\u000086\u0001\u0000\u0000\u000089\u0001\u0000\u0000\u00009:\u0001"+
		"\u0000\u0000\u0000:;\u0005\u0005\u0000\u0000;\u000b\u0001\u0000\u0000"+
		"\u0000<?\u0003\b\u0004\u0000=?\u0003\n\u0005\u0000><\u0001\u0000\u0000"+
		"\u0000>=\u0001\u0000\u0000\u0000?A\u0001\u0000\u0000\u0000@B\u0007\u0001"+
		"\u0000\u0000A@\u0001\u0000\u0000\u0000BC\u0001\u0000\u0000\u0000CA\u0001"+
		"\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000DG\u0001\u0000\u0000\u0000"+
		"EH\u0003\b\u0004\u0000FH\u0003\n\u0005\u0000GE\u0001\u0000\u0000\u0000"+
		"GF\u0001\u0000\u0000\u0000H\r\u0001\u0000\u0000\u0000IK\u0007\u0002\u0000"+
		"\u0000JI\u0001\u0000\u0000\u0000KN\u0001\u0000\u0000\u0000LM\u0001\u0000"+
		"\u0000\u0000LJ\u0001\u0000\u0000\u0000MO\u0001\u0000\u0000\u0000NL\u0001"+
		"\u0000\u0000\u0000OQ\u0005\u0003\u0000\u0000PR\u0005\u0004\u0000\u0000"+
		"QP\u0001\u0000\u0000\u0000QR\u0001\u0000\u0000\u0000R\u000f\u0001\u0000"+
		"\u0000\u0000SU\u0005\u0006\u0000\u0000TV\b\u0003\u0000\u0000UT\u0001\u0000"+
		"\u0000\u0000VW\u0001\u0000\u0000\u0000WU\u0001\u0000\u0000\u0000WX\u0001"+
		"\u0000\u0000\u0000X\u0011\u0001\u0000\u0000\u0000\r\u0015\u001c \',28"+
		">CGLQW";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}