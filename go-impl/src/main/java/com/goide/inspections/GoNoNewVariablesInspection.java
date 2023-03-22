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
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import java.util.List;

@ExtensionImpl
public class GoNoNewVariablesInspection extends GoGeneralInspectionBase {
  public static final String QUICK_FIX_NAME = "Replace with '='";

  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitShortVarDeclaration(@Nonnull GoShortVarDeclaration o) {
        visitVarDefinitionList(o, o.getVarDefinitionList());
      }

      @Override
      public void visitRecvStatement(@Nonnull GoRecvStatement o) {
        visitVarDefinitionList(o, o.getVarDefinitionList());
      }

      @Override
      public void visitRangeClause(@Nonnull GoRangeClause o) {
        visitVarDefinitionList(o, o.getVarDefinitionList());
      }

      private void visitVarDefinitionList(@Nonnull PsiElement o, @Nonnull List<GoVarDefinition> list) {
        GoVarDefinition first = ContainerUtil.getFirstItem(list);
        GoVarDefinition last = ContainerUtil.getLastItem(list);
        if (first == null || last == null) return;
        if (hasNonNewVariables(list)) {
          TextRange textRange = TextRange.create(first.getStartOffsetInParent(), last.getStartOffsetInParent() + last.getTextLength());
          holder.registerProblem(o, textRange, "No new variables on left side of :=", new MyLocalQuickFixBase());
        }
      }
    };
  }

  public static boolean hasNonNewVariables(@Nonnull List<GoVarDefinition> list) {
    if (list.isEmpty()) return false;
    for (GoVarDefinition def : list) {
      if (def.isBlank()) continue;
      PsiReference reference = def.getReference();
      if (reference == null || reference.resolve() == null) return false;
    }
    return true;
  }

  public static void replaceWithAssignment(@Nonnull Project project, @Nonnull PsiElement element) {
    if (element instanceof GoShortVarDeclaration) {
      PsiElement parent = element.getParent();
      if (parent instanceof GoSimpleStatement) {
        String left = GoPsiImplUtil.joinPsiElementText(((GoShortVarDeclaration)element).getVarDefinitionList());
        String right = GoPsiImplUtil.joinPsiElementText(((GoShortVarDeclaration)element).getRightExpressionsList());
        parent.replace(GoElementFactory.createAssignmentStatement(project, left, right));
      }
    }
    else if (element instanceof GoRangeClause) {
      String left = GoPsiImplUtil.joinPsiElementText(((GoRangeClause)element).getVarDefinitionList());
      GoExpression rangeExpression = ((GoRangeClause)element).getRangeExpression();
      String right = rangeExpression != null ? rangeExpression.getText() : "";
      element.replace(GoElementFactory.createRangeClauseAssignment(project, left, right));
    }
    else if (element instanceof GoRecvStatement) {
      String left = GoPsiImplUtil.joinPsiElementText(((GoRecvStatement)element).getVarDefinitionList());
      GoExpression recvExpression = ((GoRecvStatement)element).getRecvExpression();
      String right = recvExpression != null ? recvExpression.getText() : "";
      element.replace(GoElementFactory.createRecvStatementAssignment(project, left, right));
    }
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "No new variables on left side of :=";
  }

  private static class MyLocalQuickFixBase extends LocalQuickFixBase {
    public MyLocalQuickFixBase() {
      super(QUICK_FIX_NAME);
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getStartElement();
      if (element.isValid()) {
        replaceWithAssignment(project, element);
      }
    }
  }
}
