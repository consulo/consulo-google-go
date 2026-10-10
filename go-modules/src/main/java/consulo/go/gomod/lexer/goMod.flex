package consulo.go.gomod.lexer;

import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.lexer.LexerBase;
import consulo.go.gomod.psi.GoModTypes;

/**
 * Lexer of go.mod and go.work. Line ends matter: a directive is one line, or a block of lines in parentheses.
 * A keyword is only a keyword where a directive starts - at the beginning of a top level line; anywhere else,
 * "go" or "tool" are just words of a module path.
 */
%%

%class _GoModLexer
%extends LexerBase
%implements GoModTypes
%unicode
%public

%function advanceImpl
%type IElementType

EOL           = \r\n | \r | \n
WS            = [ \t\f]+
LINE_COMMENT  = "//" [^\r\n]*
STRING        = \" ([^\"\\\r\n] | \\ [^\r\n])* \"?
RAW_STRING    = ` [^`\r\n]* `?
// a word runs up to white space, a quote, punctuation or "="
WORD          = [^ \t\f\r\n()\[\],\"`=]+

// LINE: the rest of a top level line; BLOCK: inside a parenthesized block of a directive
%state LINE, BLOCK

%%

<YYINITIAL> {
  {EOL}          { return EOL; }
  {WS}           { return TokenType.WHITE_SPACE; }
  {LINE_COMMENT} { return LINE_COMMENT; }

  "module"    / [ \t\f\r\n(]  { yybegin(LINE); return MODULE; }
  "go"        / [ \t\f\r\n(]  { yybegin(LINE); return GO; }
  "toolchain" / [ \t\f\r\n(]  { yybegin(LINE); return TOOLCHAIN; }
  "godebug"   / [ \t\f\r\n(]  { yybegin(LINE); return GODEBUG; }
  "require"   / [ \t\f\r\n(]  { yybegin(LINE); return REQUIRE; }
  "exclude"   / [ \t\f\r\n(]  { yybegin(LINE); return EXCLUDE; }
  "replace"   / [ \t\f\r\n(]  { yybegin(LINE); return REPLACE; }
  "retract"   / [ \t\f\r\n(]  { yybegin(LINE); return RETRACT; }
  "tool"      / [ \t\f\r\n(]  { yybegin(LINE); return TOOL; }
  "ignore"    / [ \t\f\r\n(]  { yybegin(LINE); return IGNORE; }
  "use"       / [ \t\f\r\n(]  { yybegin(LINE); return USE; }

  "("            { yybegin(BLOCK); return LPAREN; }
  [^]            { yypushback(1); yybegin(LINE); }
}

<LINE> {
  {EOL}          { yybegin(YYINITIAL); return EOL; }
  "("            { yybegin(BLOCK); return LPAREN; }
}

<BLOCK> {
  {EOL}          { return EOL; }
  ")"            { yybegin(LINE); return RPAREN; }
}

<LINE, BLOCK> {
  {WS}           { return TokenType.WHITE_SPACE; }
  {LINE_COMMENT} { return LINE_COMMENT; }
  ")"            { return RPAREN; }
  "["            { return LBRACK; }
  "]"            { return RBRACK; }
  ","            { return COMMA; }
  "=>"           { return ARROW; }
  "="            { return EQ; }
  {STRING}       { return STRING; }
  {RAW_STRING}   { return STRING; }
  {WORD}         { return IDENTIFIER; }
  [^]            { return TokenType.BAD_CHARACTER; }
}
