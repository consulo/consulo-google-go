/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.plan9.intel.lang.core;

import com.plan9.intel.lang.AsmIntelLanguage;
import com.plan9.intel.lang.core.lexer.AsmIntelLexer;
import com.plan9.intel.lang.core.lexer.AsmIntelTokenType;
import com.plan9.intel.lang.core.psi.AsmIntelFile;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.*;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;

import jakarta.annotation.Nonnull;

import static com.plan9.intel.lang.core.psi.AsmIntelTypes.*;

@ExtensionImpl
public class AsmIntelParserDefinition implements ParserDefinition {

  public static final IElementType LINE_COMMENT = new AsmIntelTokenType("LINE_COMMENT");

  private static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);

  private static final TokenSet COMMENTS = TokenSet.create(LINE_COMMENT);
  public static final TokenSet KEYWORDS = TokenSet.create(TEXT);
  public static final TokenSet NUMBERS = TokenSet.create(HEX, INT);
  public static final TokenSet REGISTERS = TokenSet.create(PSEUDO_REG);

  public static final IFileElementType FILE = new IFileElementType(Language.findInstance(AsmIntelLanguage.class));

  @Nonnull
  @Override
  public Language getLanguage() {
    return AsmIntelLanguage.INSTANCE;
  }

  @Nonnull
  @Override
  public Lexer createLexer(LanguageVersion languageVersion) {
    return new AsmIntelLexer();
  }

  @Override
  @Nonnull
  public TokenSet getWhitespaceTokens(LanguageVersion languageVersion) {
    return WHITE_SPACES;
  }

  @Override
  @Nonnull
  public TokenSet getCommentTokens(LanguageVersion languageVersion) {
    return COMMENTS;
  }

  @Override
  @Nonnull
  public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
    return TokenSet.EMPTY;
  }

  @Override
  @Nonnull
  public PsiParser createParser(LanguageVersion languageVersion) {
    return new PsiParser() {
      @Nonnull
      @Override
      public ASTNode parse(@Nonnull IElementType rootElement, @Nonnull PsiBuilder builder, @Nonnull LanguageVersion languageVersion) {
        PsiBuilder.Marker mark = builder.mark();
        while (!builder.eof()) {
          builder.advanceLexer();
        }
        mark.done(rootElement);
        return builder.getTreeBuilt();
      }
    };
    //return new AsmIntelParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new AsmIntelFile(viewProvider);
  }

  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }

  @Override
  @Nonnull
  public PsiElement createElement(ASTNode node) {
    return Factory.createElement(node);
  }
}
