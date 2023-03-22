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

import com.goide.psi.GoAssignmentStatement;
import com.goide.psi.GoIfStatement;
import com.goide.psi.GoShortVarDeclaration;
import com.goide.psi.GoVisitor;
import com.goide.psi.impl.GoElementFactory;
import com.goide.psi.impl.GoPsiImplUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.project.Project;

import javax.annotation.Nonnull;

import static consulo.language.editor.inspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;

@ExtensionImpl
public class GoUsedAsValueInCondition extends GoInspectionBase {
  public static final String QUICK_FIX_NAME = "Convert to '==''";

  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitAssignmentStatement(@Nonnull GoAssignmentStatement o) {
        if (o.getParent() != null && o.getParent() instanceof GoIfStatement && ((GoIfStatement)o.getParent()).getExpression() == null) {
          String left = GoPsiImplUtil.joinPsiElementText(o.getLeftHandExprList().getExpressionList());
          String right = GoPsiImplUtil.joinPsiElementText(o.getExpressionList());
          holder.registerProblem(o, left + " = " + right + " used as value", GENERIC_ERROR_OR_WARNING, 
                                 new GoAssignmentToComparisonQuickFix());
        }
      }

      @Override
      public void visitShortVarDeclaration(@Nonnull GoShortVarDeclaration o) {
        PsiElement parent = o.getParent();
        if (parent != null) {
          PsiElement gradParent = parent.getParent();
          if (gradParent instanceof GoIfStatement && ((GoIfStatement)gradParent).getExpression() == null) {
            String left = GoPsiImplUtil.joinPsiElementText(o.getVarDefinitionList());
            String right = GoPsiImplUtil.joinPsiElementText(o.getRightExpressionsList());
            holder.registerProblem(o, left + " := " + right + " used as value", GENERIC_ERROR_OR_WARNING,
                                   new GoAssignmentToComparisonQuickFix());
          }
        }
      }
    };
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Control flow issues";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Used as value in condition";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  private static class GoAssignmentToComparisonQuickFix extends LocalQuickFixBase {
    private GoAssignmentToComparisonQuickFix() {
      super(QUICK_FIX_NAME);
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getPsiElement();
      if (element instanceof GoAssignmentStatement) {
        String left = GoPsiImplUtil.joinPsiElementText(((GoAssignmentStatement)element).getLeftHandExprList().getExpressionList());
        String right = GoPsiImplUtil.joinPsiElementText(((GoAssignmentStatement)element).getExpressionList());
        element.replace(GoElementFactory.createExpression(project, left + " == " + right));
      }
      else if (element instanceof GoShortVarDeclaration) {
        String left = GoPsiImplUtil.joinPsiElementText(((GoShortVarDeclaration)element).getVarDefinitionList());
        String right = GoPsiImplUtil.joinPsiElementText(((GoShortVarDeclaration)element).getRightExpressionsList());
        element.replace(GoElementFactory.createComparison(project, left + " == " + right));
      }
    }
  }
}
