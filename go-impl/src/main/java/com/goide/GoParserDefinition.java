/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goide;

import com.goide.lexer.GoLexer;
import com.goide.parser.GoParser;
import com.goide.psi.GoFile;
import com.goide.psi.GoTokenType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;

import jakarta.annotation.Nonnull;

import static com.goide.GoTypes.*;

@ExtensionImpl
public class GoParserDefinition implements ParserDefinition {
  public static final IElementType LINE_COMMENT = new GoTokenType("GO_LINE_COMMENT");
  public static final IElementType MULTILINE_COMMENT = new GoTokenType("GO_MULTILINE_COMMENT");

  public static final IElementType WS = new GoTokenType("GO_WHITESPACE");
  public static final IElementType NLS = new GoTokenType("GO_WS_NEW_LINES");

  public static final TokenSet WHITESPACES = TokenSet.create(WS, NLS);
  public static final TokenSet COMMENTS = TokenSet.create(LINE_COMMENT, MULTILINE_COMMENT);
  public static final TokenSet STRING_LITERALS = TokenSet.create(STRING, RAW_STRING, CHAR);
  public static final TokenSet NUMBERS = TokenSet.create(INT, FLOAT, FLOATI, DECIMALI, FLOATI); // todo: HEX, OCT,
  public static final TokenSet KEYWORDS = TokenSet.create(
    BREAK, CASE, CHAN, CONST, CONTINUE, DEFAULT, DEFER, ELSE, FALLTHROUGH, FOR, FUNC, GO, GOTO, IF, IMPORT,
    INTERFACE, MAP, PACKAGE, RANGE, RETURN, SELECT, STRUCT, SWITCH, TYPE_, VAR);
  public static final TokenSet OPERATORS = TokenSet.create(
    EQ, ASSIGN, NOT_EQ, NOT, PLUS_PLUS, PLUS_ASSIGN, PLUS, MINUS_MINUS, MINUS_ASSIGN, MINUS, COND_OR, BIT_OR_ASSIGN, BIT_OR,
    BIT_CLEAR_ASSIGN, BIT_CLEAR, COND_AND, BIT_AND_ASSIGN, BIT_AND, SHIFT_LEFT_ASSIGN, SHIFT_LEFT, SEND_CHANNEL, LESS_OR_EQUAL,
    LESS, BIT_XOR_ASSIGN, BIT_XOR, MUL_ASSIGN, MUL, QUOTIENT_ASSIGN, QUOTIENT, REMAINDER_ASSIGN, REMAINDER, SHIFT_RIGHT_ASSIGN,
    SHIFT_RIGHT, GREATER_OR_EQUAL, GREATER, VAR_ASSIGN);

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }

  @Nonnull
  @Override
  public Lexer createLexer(LanguageVersion languageVersion) {
    return new GoLexer();
  }

  @Nonnull
  @Override
  public PsiParser createParser(LanguageVersion languageVersion) {
    return new GoParser();
  }

  @Nonnull
  @Override
  public IFileElementType getFileNodeType() {
    return GoFileElementType.INSTANCE;
  }

  @Nonnull
  @Override
  public TokenSet getWhitespaceTokens(LanguageVersion languageVersion) {
    return WHITESPACES;
  }

  @Nonnull
  @Override
  public TokenSet getCommentTokens(LanguageVersion languageVersion) {
    return COMMENTS;
  }

  @Nonnull
  @Override
  public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
    return STRING_LITERALS;
  }

  @Nonnull
  @Override
  public PsiElement createElement(ASTNode node) {
    return Factory.createElement(node);
  }

  @Nonnull
  @Override
  public PsiFile createFile(@Nonnull FileViewProvider viewProvider) {
    return new GoFile(viewProvider);
  }

  @Nonnull
  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }
}
