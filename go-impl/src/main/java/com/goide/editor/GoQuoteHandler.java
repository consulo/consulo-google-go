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

package com.goide.editor;

import com.goide.GoTypes;
import com.intellij.codeInsight.editorActions.JavaLikeQuoteHandler;
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import javax.annotation.Nonnull;

public class GoQuoteHandler extends SimpleTokenSetQuoteHandler implements JavaLikeQuoteHandler {
  private static final TokenSet SINGLE_LINE_LITERALS = TokenSet.create(GoTypes.STRING);

  public GoQuoteHandler() {
    super(GoTypes.STRING, GoTypes.RAW_STRING, GoTypes.CHAR, TokenType.BAD_CHARACTER);
  }

  @Override
  public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
    return true;
  }

  @Override
  public TokenSet getConcatenatableStringTokenTypes() {
    return SINGLE_LINE_LITERALS;
  }

  @Override
  public String getStringConcatenationOperatorRepresentation() {
    return "+";
  }

  @Override
  public TokenSet getStringTokenTypes() {
    return SINGLE_LINE_LITERALS;
  }

  @Override
  public boolean isAppropriateElementTypeForLiteral(@Nonnull IElementType tokenType) {
    return true;
  }

  @Override
  public boolean needParenthesesAroundConcatenation(PsiElement element) {
    return false;
  }
}
