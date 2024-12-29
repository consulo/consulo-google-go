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

import com.goide.psi.GoElement;
import com.goide.psi.GoVisitor;
import com.goide.psi.impl.GoElementFactory;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GoAddTrailingCommaInspection extends GoGeneralInspectionBase {
  public static final String QUICK_FIX_NAME = "Add comma";

  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitErrorElement(PsiErrorElement o) {
        GoElement element = PsiTreeUtil.getPrevSiblingOfType(o, GoElement.class);
        if (element != null) {
          holder.registerProblem(element, "Need trailing comma before newline in composite literal", new MyAddCommaFix());
        }
      }
    };
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Need trailing comma before newline in composite literal";
  }

  private static class MyAddCommaFix extends LocalQuickFixBase {
    private MyAddCommaFix() {super(QUICK_FIX_NAME, QUICK_FIX_NAME);}

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement e = descriptor.getPsiElement();
      if (!(e instanceof GoElement)) return;
      PsiErrorElement error = PsiTreeUtil.getNextSiblingOfType(e, PsiErrorElement.class);
      if (error == null) return;
      new WriteCommandAction.Simple(project, getName(), e.getContainingFile()) {
        @Override
        protected void run() {
          error.replace(GoElementFactory.createComma(project));
        }
      }.execute();
    }
  }
}
