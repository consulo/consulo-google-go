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

package com.goide.inspections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.goide.psi.GoFile;
import com.goide.psi.GoVisitor;
import com.goide.psi.impl.GoPsiImplUtil;
import com.intellij.codeInspection.*;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;

abstract public class GoInspectionBase extends LocalInspectionTool {
  protected static final GoVisitor DUMMY_VISITOR = new GoVisitor() { };

  @Nonnull
  @Override
  public final PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly, @Nonnull LocalInspectionToolSession session) {
    GoFile file = ObjectUtils.tryCast(session.getFile(), GoFile.class);
    return file != null && GoPsiImplUtil.allowed(file, null, ModuleUtilCore.findModuleForPsiElement(file))
           ? buildGoVisitor(holder, session)
           : DUMMY_VISITOR;
  }

  @Nonnull
  @Override
  public final PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly) {
    throw new IllegalStateException();
  }

  @Nullable
  @Override
  public final ProblemDescriptor[] checkFile(@Nonnull PsiFile file, @Nonnull InspectionManager manager, boolean isOnTheFly) {
    throw new IllegalStateException();
  }

  @Nonnull
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitFile(PsiFile file) {
        checkFile((GoFile)file, holder);
      }
    };
  }

  protected void checkFile(@Nonnull GoFile file, @Nonnull ProblemsHolder problemsHolder) {
  }
}