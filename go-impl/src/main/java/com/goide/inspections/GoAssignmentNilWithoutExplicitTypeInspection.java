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

import com.goide.GoConstants;
import com.goide.psi.*;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.psi.impl.GoReferenceExpressionImpl;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import java.util.List;

@ExtensionImpl
public class GoAssignmentNilWithoutExplicitTypeInspection extends GoGeneralInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitVarDeclaration(@Nonnull GoVarDeclaration o) {
        for (GoVarSpec spec : o.getVarSpecList()) {
          checkVar(spec);
        }
      }

      @Override
      public void visitShortVarDeclaration(@Nonnull GoShortVarDeclaration o) {
        checkVar(o);
      }

      @Override
      public void visitConstDeclaration(@Nonnull GoConstDeclaration o) {
        for (GoConstSpec spec : o.getConstSpecList()) {
          checkConst(spec);
        }
      }

      private void checkVar(@Nonnull GoVarSpec spec) {
        if (spec.getType() != null) return;
        checkExpressions(spec.getRightExpressionsList());
      }

      private void checkConst(@Nonnull GoConstSpec spec) {
        if (spec.getType() != null) return;
        checkExpressions(spec.getExpressionList());
      }

      private void checkExpressions(@Nonnull List<GoExpression> expressions) {
        for (GoExpression expr : expressions) {
          if (expr instanceof GoReferenceExpressionImpl) {
            GoReferenceExpressionImpl ref = (GoReferenceExpressionImpl) expr;
            PsiElement resolve = ref.resolve();
            if (ref.getIdentifier().textMatches(GoConstants.NIL) && resolve != null && GoPsiImplUtil.builtin(resolve)) {
              holder.registerProblem(expr, "Cannot assign nil without explicit type", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
          }
        }
      }
    };
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Assignment nil without explicit type";
  }
}
