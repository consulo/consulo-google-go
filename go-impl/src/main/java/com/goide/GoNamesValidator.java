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
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.editor.refactoring.NamesValidator;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ExtensionImpl
public class GoNamesValidator implements NamesValidator {
  @Override
  public boolean isKeyword(@Nonnull String name, Project project) {
    return GoParserDefinition.KEYWORDS.contains(getLexerType(name));
  }

  @Override
  public boolean isIdentifier(@Nonnull String name, Project project) {
    return getLexerType(name) == GoTypes.IDENTIFIER;
  }

  @Nullable
  private static IElementType getLexerType(@Nonnull String text) {
    GoLexer lexer = new GoLexer();
    lexer.start(text);
    return lexer.getTokenEnd() == text.length() ? lexer.getTokenType() : null;
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}
