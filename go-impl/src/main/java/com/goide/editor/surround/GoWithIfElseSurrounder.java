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

package com.goide.editor.surround;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GoWithIfElseSurrounder extends GoStatementsSurrounder {
  @Override
  public String getTemplateDescription() {
    return "if { statements } else { }";
  }

  @Nullable
  @Override
  protected TextRange surroundStatements(@Nonnull Project project,
                                         @Nonnull PsiElement container,
                                         @Nonnull PsiElement[] statements) throws IncorrectOperationException {
    return surroundStatementsWithIfElse(project, container, statements, true);
  }
}
