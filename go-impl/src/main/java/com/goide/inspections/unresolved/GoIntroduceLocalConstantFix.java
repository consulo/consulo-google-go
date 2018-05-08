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

package com.goide.inspections.unresolved;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.goide.refactor.GoRefactoringUtil;
import com.intellij.psi.PsiElement;

public class GoIntroduceLocalConstantFix extends GoUnresolvedFixBase {
  public GoIntroduceLocalConstantFix(@Nonnull PsiElement element, @Nonnull String name) {
    super(element, name, "local constant", "go_lang_const_qf");
  }

  @Nullable
  @Override
  protected PsiElement findAnchor(@Nonnull PsiElement reference) {
    return GoRefactoringUtil.findLocalAnchor(GoRefactoringUtil.getLocalOccurrences(reference));
  }
}
