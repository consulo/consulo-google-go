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

package com.goide.codeInsight.unwrap;

import com.goide.psi.GoForClause;
import com.goide.psi.GoForStatement;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.util.collection.ContainerUtil;

public class GoForUnwrapper extends GoUnwrapper {
  public GoForUnwrapper() {
    super("Unwrap for");
  }

  @Override
  public boolean isApplicableTo(PsiElement e) {
    return e instanceof GoForStatement;
  }

  @Override
  protected void doUnwrap(PsiElement element, Context context) throws IncorrectOperationException {
    GoForStatement forStatement = (GoForStatement)element;
    GoForClause forClause = forStatement.getForClause();
    if (forClause != null) {
      context.extractElement(ContainerUtil.getFirstItem(forClause.getStatementList()), forStatement);
    }
    context.extractFromBlock(forStatement.getBlock(), forStatement);
    context.delete(forStatement);
  }
}