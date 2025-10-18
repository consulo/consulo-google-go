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

import com.goide.psi.GoForStatement;
import com.goide.psi.impl.GoElementFactory;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GoWithForSurrounder extends GoStatementsSurrounder {
  @Override
  public LocalizeValue getTemplateDescription() {
    return LocalizeValue.localizeTODO("for { statements }");
  }

  @Nullable
  @Override
  protected TextRange surroundStatements(@Nonnull Project project,
                                         @Nonnull PsiElement container,
                                         @Nonnull PsiElement[] statements) throws IncorrectOperationException {
    String text = StringUtil.join(statements, PsiElement::getText, "\n");
    GoForStatement forStatement = GoElementFactory.createForStatement(project, text);
    PsiElement first = ArrayUtil.getFirstElement(statements);
    PsiElement last = ArrayUtil.getLastElement(statements);
    forStatement = (GoForStatement)container.addAfter(forStatement, last);
    container.deleteChildRange(first, last);
    int offset = getOffsetLBraceOfBlock(forStatement.getBlock());
    return offset > -1 ? new TextRange(offset, offset) : null;
  }
}
