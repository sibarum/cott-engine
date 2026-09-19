/*
 * The calculator grammar: numbers, variables, calls, and the five operators.
 *
 * Numbers become T(p, q) leaves and everything else becomes a branching node -- see Node.java. The
 * grammar knows nothing about what the operators mean; it only says how a line of text nests.
 *
 * Precedence, loosest first: + -, then * /, then unary -, then ^ which associates to the right.
 * Unary minus binding tighter than * makes -x*y into (-x)*y, and looser than ^ makes -x^2 into
 * -(x^2), which is what a calculator's user expects of both.
 *
 * There is no juxtaposition rule. A run of letters is one name -- xy is the name xy -- and 2x is not
 * an expression at all. A parser that guessed there would have to know which runs of letters are
 * words, and a standard for future work should not need a dictionary to read a line. Write 2*x.
 */
grammar Traction;

entry : expr EOF ;

expr
  : <assoc=right> expr POW expr   # Power
  | MINUS expr                    # Negate
  | expr (MUL | DIV) expr         # Scale
  | expr (PLUS | MINUS) expr      # Total
  | NUMBER                        # Number
  | ID LPAREN args? RPAREN        # Apply
  | ID                            # Name
  | LPAREN expr RPAREN            # Group
  ;

args : expr (COMMA expr)* ;

POW    : '^' ;
// The display glyphs are accepted beside the ASCII ones, so a rendered term reads back in.
MUL    : '*' | '·' | '×' ;
DIV    : '/' | '÷' ;
PLUS   : '+' ;
MINUS  : '-' | '−' ;
LPAREN : '(' ;
RPAREN : ')' ;
COMMA  : ',' ;

NUMBER : [0-9]+ ('.' [0-9]+)? ;

// Greek is in, so that a variable may be spelled the way the theory spells it: omega, pi, theta.
ID     : [a-zA-Z_Ͱ-Ͽ] [a-zA-Z_0-9Ͱ-Ͽ]* ;

WS     : [ \t\r\n]+ -> skip ;
