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

package com.goide.quickfix;

import javax.annotation.Nonnull;

import com.goide.psi.GoForStatement;
import com.goide.psi.impl.GoElementFactory;
import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;

public class GoReplaceWithSelectStatementQuickFix extends LocalQuickFixBase {
  public static final String QUICK_FIX_NAME = "Replace with 'select'";

  public GoReplaceWithSelectStatementQuickFix() {
    super(QUICK_FIX_NAME);
  }

  @Nls
  @Nonnull
  @Override
  public String getFamilyName() {
    return QUICK_FIX_NAME;
  }

  @Override
  public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getPsiElement();
    if (element instanceof GoForStatement) {
      element.replace(GoElementFactory.createSelectStatement(project));
    }
  }
}