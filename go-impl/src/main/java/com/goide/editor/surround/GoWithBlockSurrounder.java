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
import com.goide.psi.impl.GoElementFactory;
import consulo.document.util.TextRange;
import consulo.language.impl.psi.CodeEditUtil;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GoWithBlockSurrounder extends GoStatementsSurrounder {
  @Override
  public String getTemplateDescription() {
    return "{ statements }";
  }

  @Nullable
  @Override
  protected TextRange surroundStatements(@Nonnull Project project,
                                         @Nonnull PsiElement container,
                                         @Nonnull PsiElement[] statements) throws IncorrectOperationException {
    GoBlock block = GoElementFactory.createBlock(project);
    PsiElement first = ArrayUtil.getFirstElement(statements);
    PsiElement last = ArrayUtil.getLastElement(statements);
    block = (GoBlock)container.addAfter(block, last);
    block.addRangeAfter(first, last, block.getLbrace());
    CodeEditUtil.markToReformat(block.getNode(), true);
    container.deleteChildRange(first, last);
    int offset = block.getTextRange().getEndOffset();
    return new TextRange(offset, offset);
  }
}
