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

import com.goide.psi.GoFile;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.lexer.Lexer;
import consulo.language.psi.PsiFile;
import consulo.language.psi.search.IndexPatternBuilder;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoIndexPatternBuilder implements IndexPatternBuilder {
  @Nullable
  @Override
  public Lexer getIndexingLexer(@Nonnull PsiFile file) {
    return file instanceof GoFile ? ((GoFile)file).getParserDefinition().createLexer(file.getLanguageVersion()) : null;
  }

  @Nullable
  @Override
  public TokenSet getCommentTokenSet(@Nonnull PsiFile file) {
    return GoParserDefinition.COMMENTS;
  }

  @Override
  public int getCommentStartDelta(IElementType tokenType) {
    return 0;
  }

  @Override
  public int getCommentEndDelta(IElementType tokenType) {
    return tokenType == GoParserDefinition.MULTILINE_COMMENT ? 2 : 0;
  }
}
