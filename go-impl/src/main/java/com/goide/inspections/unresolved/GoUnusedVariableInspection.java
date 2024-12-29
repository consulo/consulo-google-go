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
import com.goide.psi.*;
import com.goide.psi.impl.GoVarProcessor;
import com.goide.quickfix.GoDeleteVarDefinitionQuickFix;
import com.goide.quickfix.GoRenameToBlankQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressManager;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.util.PsiTreeUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoUnusedVariableInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitVarDefinition(@Nonnull GoVarDefinition o) {
        if (o.isBlank()) return;
        GoCompositeElement varSpec = PsiTreeUtil.getParentOfType(o, GoVarSpec.class, GoTypeSwitchGuard.class);
        GoVarDeclaration decl = PsiTreeUtil.getParentOfType(o, GoVarDeclaration.class);
        if (shouldValidate(decl) && (varSpec != null || decl != null)) {
          PsiReference reference = o.getReference();
          PsiElement resolve = reference != null ? reference.resolve() : null;
          if (resolve != null) return;
          boolean foundReference = !ReferencesSearch.search(o, o.getUseScope()).forEach(reference1 -> {
            ProgressManager.checkCanceled();
            PsiElement element = reference1.getElement();
            if (element == null) return true;
            PsiElement parent = element.getParent();
            if (parent instanceof GoLeftHandExprList) {
              PsiElement grandParent = parent.getParent();
              if (grandParent instanceof GoAssignmentStatement &&
                  ((GoAssignmentStatement)grandParent).getAssignOp().getAssign() != null) {
                GoFunctionLit fn = PsiTreeUtil.getParentOfType(element, GoFunctionLit.class);
                if (fn == null || !PsiTreeUtil.isAncestor(GoVarProcessor.getScope(o), fn, true)) {
                  return true;
                }
              }
            }
            if (parent instanceof GoShortVarDeclaration) {
              int op = ((GoShortVarDeclaration)parent).getVarAssign().getStartOffsetInParent();
              if (element.getStartOffsetInParent() < op) {
                return true;
              }
            }
            return false;
          });

          if (!foundReference) {
            reportError(o, holder);
          }
        }
      }
    };
  }

  protected void reportError(@Nonnull GoVarDefinition varDefinition, @Nonnull ProblemsHolder holder) {
    holder.registerProblem(varDefinition, "Unused variable <code>#ref</code> #loc", ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                           new GoRenameToBlankQuickFix(varDefinition), new GoDeleteVarDefinitionQuickFix(varDefinition.getName()));
  }

  protected boolean shouldValidate(@Nullable GoVarDeclaration varDeclaration) {
    return varDeclaration == null || !(varDeclaration.getParent() instanceof GoFile);
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Declaration redundancy";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Unused variable inspection";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}
