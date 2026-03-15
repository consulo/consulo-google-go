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

import com.goide.psi.GoBlock;
import com.goide.psi.GoIfStatement;
import com.goide.psi.impl.GoElementFactory;
import consulo.codeEditor.Editor;
import consulo.document.util.TextRange;
import consulo.language.editor.surroundWith.Surrounder;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;

import org.jspecify.annotations.Nullable;

public abstract class GoStatementsSurrounder implements Surrounder {
  @Override
  public boolean isApplicable(PsiElement[] elements) {
    return true;
  }

  @Override
  @Nullable
  public TextRange surroundElements(Project project,
                                    Editor editor,
                                    PsiElement[] elements) throws IncorrectOperationException {
    PsiElement container = elements[0].getParent();
    if (container == null) return null;
    return surroundStatements(project, container, elements);
  }

  @Nullable
  protected abstract TextRange surroundStatements(Project project,
                                                  PsiElement container,
                                                  PsiElement[] statements) throws IncorrectOperationException;

  @Nullable
  protected TextRange surroundStatementsWithIfElse(Project project,
                                                   PsiElement container,
                                                   PsiElement[] statements,
                                                   boolean withElse) {
    PsiElement first = ArrayUtil.getFirstElement(statements);
    PsiElement last = ArrayUtil.getLastElement(statements);
    String block = StringUtil.join(statements, PsiElement::getText, "\n");
    GoIfStatement ifStatement = GoElementFactory.createIfStatement(project, "", block, withElse ? "" : null);
    ifStatement = (GoIfStatement)container.addAfter(ifStatement, last);
    container.deleteChildRange(first, last);
    int offset = getOffsetLBraceOfBlock(ifStatement.getBlock());
    return offset > -1 ? new TextRange(offset, offset) : null;
  }

  protected int getOffsetLBraceOfBlock(@Nullable GoBlock block) {
    return block != null ? block.getLbrace().getTextRange().getStartOffset() : -1;
  }
}
