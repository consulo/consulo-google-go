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
import com.goide.psi.impl.GoElementFactory;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.quickfix.GoDeleteQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.inspection.*;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.project.Project;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoDeferGoInspection extends GoGeneralInspectionBase {
  public static final LocalizeValue ADD_CALL_QUICK_FIX_NAME = LocalizeValue.localizeTODO("Add function call");
  public static final LocalizeValue UNWRAP_PARENTHESES_QUICK_FIX_NAME = LocalizeValue.localizeTODO("Unwrap parentheses");
  public static final LocalizeValue REPLACE_WITH_CORRECT_DEFER_RECOVER = LocalizeValue.localizeTODO("Replace with correct defer construct");

  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @SuppressWarnings("DialogTitleCapitalization")
      @Override
      public void visitDeferStatement(@Nonnull GoDeferStatement o) {
        super.visitDeferStatement(o);
        GoExpression expression = o.getExpression();
        if (expression instanceof GoCallExpr) {
          GoCallExpr callExpr = (GoCallExpr)expression;
          if (GoPsiImplUtil.isPanic(callExpr)) {
            holder.registerProblem(o, "defer should not call panic() directly #loc", ProblemHighlightType.WEAK_WARNING,
                                   new GoDeleteQuickFix(LocalizeValue.localizeTODO("Delete statement"), GoDeferStatement.class));
            return;
          }
          if (GoPsiImplUtil.isRecover(callExpr)) {
            holder.registerProblem(o, "defer should not call recover() directly #loc", ProblemHighlightType.WEAK_WARNING,
                                   new GoReplaceWithCorrectDeferRecoverQuickFix());
            return;
          }
        }
        checkExpression(expression, "defer");
      }

      @SuppressWarnings("DialogTitleCapitalization")
      @Override
      public void visitGoStatement(@Nonnull GoGoStatement o) {
        super.visitGoStatement(o);
        GoExpression expression = o.getExpression();
        if (expression instanceof GoCallExpr) {
          GoCallExpr callExpr = (GoCallExpr)expression;
          if (GoPsiImplUtil.isPanic(callExpr)) {
            holder.registerProblem(o, "go should not call panic() directly #loc", ProblemHighlightType.WEAK_WARNING,
                                   new GoDeleteQuickFix(LocalizeValue.localizeTODO("Delete statement"), GoGoStatement.class));
            return;
          }
          if (GoPsiImplUtil.isRecover(callExpr)) {
            holder.registerProblem(o, "go should not call recover() directly #loc", ProblemHighlightType.WEAK_WARNING,
                                   new GoDeleteQuickFix(LocalizeValue.localizeTODO("Delete statement"), GoGoStatement.class));
            return;
          }
        }
        checkExpression(expression, "go");
      }

      private void checkExpression(@Nullable GoExpression o, @Nonnull String who) {
        if (o == null) return;
        if (o instanceof GoCallExpr || o instanceof GoBuiltinCallExpr) {
          if (GoPsiImplUtil.isConversionExpression(o)) {
            holder.registerProblem(o, who + " requires function call, not conversion #loc", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
          }
          return;
        }

        if (o instanceof GoParenthesesExpr) {
          holder.registerProblem(o, "Expression in " + who + " must not be parenthesized", ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                 new GoUnwrapParensExpression());
        }
        LocalQuickFix[] fixes = o.getGoType(null) instanceof GoFunctionType ? new LocalQuickFix[]{new GoAddParensQuickFix()}
                                                                            : LocalQuickFix.EMPTY_ARRAY;
        holder.registerProblem(o, "Expression in " + who + " must be function call", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixes);
      }
    };
  }

  @Nonnull
  @Override
  public LocalizeValue getDisplayName() {
    return LocalizeValue.localizeTODO("Defer/go statements check");
  }

  public static class GoAddParensQuickFix extends LocalQuickFixBase {
    public GoAddParensQuickFix() {
      super(ADD_CALL_QUICK_FIX_NAME);
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      addParensIfNeeded(project, descriptor.getStartElement());
    }

    public static PsiElement addParensIfNeeded(@Nonnull Project project, @Nullable PsiElement element) {
      if (element instanceof GoExpression && !(element instanceof GoCallExpr || element instanceof GoBuiltinCallExpr)) {
        if (((GoExpression)element).getGoType(null) instanceof GoFunctionType) {
          return element.replace(GoElementFactory.createExpression(project, element.getText() + "()"));
        }
      }
      return null;
    }
  }

  private static class GoUnwrapParensExpression extends LocalQuickFixBase {
    public GoUnwrapParensExpression() {
      super(UNWRAP_PARENTHESES_QUICK_FIX_NAME);
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getStartElement();
      if (element instanceof GoParenthesesExpr) {
        GoExpression innerExpression = ((GoParenthesesExpr)element).getExpression();
        while (innerExpression instanceof GoParenthesesExpr) {
          innerExpression = ((GoParenthesesExpr)innerExpression).getExpression();
        }
        if (innerExpression != null) {
          element.replace(GoElementFactory.createExpression(project, innerExpression.getText()));
        }
      }
    }
  }

  public static class GoReplaceWithCorrectDeferRecoverQuickFix extends LocalQuickFixBase {
    public GoReplaceWithCorrectDeferRecoverQuickFix() {
      super(REPLACE_WITH_CORRECT_DEFER_RECOVER);
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getPsiElement();
      if (element == null || !element.isValid()) return;

      if (element instanceof GoStatement) {
        element.replace(GoElementFactory.createDeferStatement(project, "func() {\n" +
                                                                       "\tif r := recover(); r != nil {\n" +
                                                                       "\t\t\n" +
                                                                       "\t}\n" +
                                                                       "\t}()}"));
      }
    }
  }
}
