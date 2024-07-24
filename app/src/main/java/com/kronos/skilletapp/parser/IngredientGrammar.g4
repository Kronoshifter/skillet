grammar IngredientGrammar;



recipe : ingredient+ EOF ;

ingredient : measurement WHITESPACE name comment? NEWLINE ;

measurement : quantity WHITESPACE WORD? ;

quantity : (decimal | fraction) ;

decimal : NUMBER ;
fraction : (NUMBER WHITESPACE)? NUMBER '/' NUMBER ;
//range : RANGE ;

name : (WORD | WHITESPACE)*? WORD WHITESPACE? ;

comment : COMMENT_START (WORD | WHITESPACE)+ ')'? ;

fragment LOWERCASE : [a-z] ;
fragment UPPERCASE : [A-Z] ;
fragment DIGIT : [0-9] ;

WORD : (LOWERCASE | UPPERCASE | '_')+ ;
WHITESPACE : (' ' | '\t') ;

NUMBER : DIGIT+ ([.,] DIGIT+)? ;
//RANGE : (DECIMAL | FRACTION) ('-' | WHITESPACE)+ (DECIMAL | FRACTION) ;

COMMENT_START : [,(] ;

NEWLINE : ('\r'? '\n' | '\r')+ ;

ANY : . ;