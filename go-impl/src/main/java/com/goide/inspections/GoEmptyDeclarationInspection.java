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

import com.goide.GoDocumentationProvider;
import com.goide.psi.*;
import com.goide.quickfix.GoDeleteQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.CleanupLocalInspectionTool;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoEmptyDeclarationInspection extends GoInspectionBase implements CleanupLocalInspectionTool {

  public final static String QUICK_FIX_NAME = "Delete empty declaration";

  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitConstDeclaration(@Nonnull GoConstDeclaration o) {
        visitDeclaration(o);
      }

      @Override
      public void visitVarDeclaration(@Nonnull GoVarDeclaration o) {
        if (o.getParent() instanceof GoFile) {
          visitDeclaration(o);
        }
      }

      @Override
      public void visitTypeDeclaration(@Nonnull GoTypeDeclaration o) {
        visitDeclaration(o);
      }

      @Override
      public void visitImportDeclaration(@Nonnull GoImportDeclaration o) {
        visitDeclaration(o);
      }

      private void visitDeclaration (PsiElement o) {
        if (o.getChildren().length == 0 &&
            GoDocumentationProvider.getCommentsForElement(o instanceof GoImportDeclaration && o.getPrevSibling() == null ?
                                                          o.getParent() : o).isEmpty() &&
            PsiTreeUtil.findChildrenOfType(o, PsiComment.class).isEmpty()) {
          holder.registerProblem(o, "Empty declaration <code>#ref</code> #loc", ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                 new GoDeleteQuickFix(QUICK_FIX_NAME, o.getClass()));
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
    return "Empty declaration inspection";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }
}
