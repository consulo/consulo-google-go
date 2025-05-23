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

import com.goide.psi.GoFile;
import com.goide.psi.GoImportSpec;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

public class GoDeleteImportQuickFix extends LocalQuickFixBase {
  public static final String QUICK_FIX_NAME = "Delete import";

  public GoDeleteImportQuickFix() {
    super(QUICK_FIX_NAME);
  }

  @Override
  public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
    PsiElement element = PsiTreeUtil.getNonStrictParentOfType(descriptor.getPsiElement(), GoImportSpec.class);
    PsiFile file = element != null ? element.getContainingFile() : null;
    if (!(file instanceof GoFile)) return;

    WriteCommandAction.runWriteCommandAction(project, () -> ((GoFile)file).deleteImport((GoImportSpec)element));
  }
}
