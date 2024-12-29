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

import com.goide.psi.*;
import com.goide.quickfix.GoDeleteQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import java.util.List;

import static com.goide.GoTypes.TRIPLE_DOT;

@ExtensionImpl
public class GoFunctionVariadicParameterInspection extends GoGeneralInspectionBase {
  private static final GoDeleteQuickFix DELETE_QUICK_FIX = new GoDeleteQuickFix("Delete ...", TRIPLE_DOT);

  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitCompositeElement(@Nonnull GoCompositeElement o) {
        if (o instanceof GoSignatureOwner) {
          GoSignature signature = ((GoSignatureOwner)o).getSignature();
          if (signature != null) {
            checkResult(signature, holder);
            checkParameters(signature, holder);
          }
        }
      }
    };
  }

  private static void checkResult(@Nonnull GoSignature o, @Nonnull ProblemsHolder holder) {
    GoResult result = o.getResult();
    if (result == null) return;
    GoParameters parameters = result.getParameters();
    if (parameters == null) return;
    for (GoParameterDeclaration declaration : parameters.getParameterDeclarationList()) {
      PsiElement dot = declaration.getTripleDot();
      if (dot != null) {
        holder.registerProblem(dot, "Cannot use <code>...</code> in output argument list", ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                               DELETE_QUICK_FIX);
      }
    }
  }

  private static void checkParameters(@Nonnull GoSignature o, @Nonnull ProblemsHolder holder) {
    GoParameters parameters = o.getParameters();
    List<GoParameterDeclaration> list = parameters.getParameterDeclarationList();
    int size = list.size();
    for (int i = 0; i < size; i++) {
      GoParameterDeclaration declaration = list.get(i);
      PsiElement dot = declaration.getTripleDot();
      if (dot != null) {
        if (declaration.getParamDefinitionList().size() > 1 || i != size - 1) {
          holder.registerProblem(dot, "Can only use <code>...</code> as final argument in list", ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                 DELETE_QUICK_FIX);
        }
      }
    }
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Incorrect variadic parameter";
  }
}
