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

import com.goide.inspections.GoInspectionBase;
import com.goide.psi.GoLabelDefinition;
import com.goide.psi.GoLabeledStatement;
import com.goide.psi.GoStatement;
import com.goide.psi.GoVisitor;
import com.goide.quickfix.GoRenameToBlankQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.*;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.search.ReferencesSearch;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ExtensionImpl
public class GoUnusedLabelInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {

      @Override
      public void visitLabelDefinition(@Nonnull GoLabelDefinition o) {
        super.visitLabelDefinition(o);
        if (o.isBlank()) return;
        if (ReferencesSearch.search(o, o.getUseScope()).findFirst() == null) {
          String name = o.getName();
          holder.registerProblem(o, "Unused label <code>#ref</code> #loc", ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                 new GoRenameToBlankQuickFix(o), new GoDeleteLabelStatementQuickFix(name));
        }
      }
    };
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Declaration redundancy";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Unused label inspection";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  private static class GoDeleteLabelStatementQuickFix extends LocalQuickFixBase {
    public GoDeleteLabelStatementQuickFix(@Nullable String labelName) {
      super("Delete label " + (labelName != null ? "'" + labelName + "'" : ""), "Delete label");
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getPsiElement();
      if (element.isValid() && element instanceof GoLabelDefinition) {
        PsiElement parent = element.getParent();
        if (parent instanceof GoLabeledStatement) {
          GoStatement innerStatement = ((GoLabeledStatement)parent).getStatement();
          if (innerStatement != null) {
            parent.replace(innerStatement);
          }
        }
      }
    }
  }
}
