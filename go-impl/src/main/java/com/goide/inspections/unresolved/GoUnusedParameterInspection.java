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
import com.goide.runconfig.testing.GoTestFinder;
import com.goide.runconfig.testing.GoTestFunctionType;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressManager;
import consulo.application.util.query.Query;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;

import jakarta.annotation.Nonnull;
import java.util.List;

@ExtensionImpl
public class GoUnusedParameterInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitMethodDeclaration(@Nonnull GoMethodDeclaration o) {
        super.visitMethodDeclaration(o);
        visitDeclaration(o, false);
      }

      @Override
      public void visitFunctionDeclaration(@Nonnull GoFunctionDeclaration o) {
        super.visitFunctionDeclaration(o);
        if (GoTestFinder.isTestFile(o.getContainingFile()) && GoTestFunctionType.fromName(o.getName()) != null) {
          return;
        }
        visitDeclaration(o, true);
      }

      private void visitDeclaration(@Nonnull GoFunctionOrMethodDeclaration o, boolean checkParameters) {
        GoSignature signature = o.getSignature();
        if (signature == null) return;
        if (checkParameters) {
          GoParameters parameters = signature.getParameters();
          visitParameterList(parameters.getParameterDeclarationList(), "parameter");
        }

        GoResult result = signature.getResult();
        GoParameters returnParameters = result != null ? result.getParameters() : null;
        if (returnParameters != null) {
          visitParameterList(returnParameters.getParameterDeclarationList(), "named return parameter");
        }
      }

      private void visitParameterList(List<GoParameterDeclaration> parameters, String what) {
        for (GoParameterDeclaration parameterDeclaration : parameters) {
          for (GoParamDefinition parameter : parameterDeclaration.getParamDefinitionList()) {
            ProgressManager.checkCanceled();
            if (parameter.isBlank()) continue;

            Query<PsiReference> search = ReferencesSearch.search(parameter, parameter.getUseScope());
            if (search.findFirst() != null) continue;

            holder.registerProblem(parameter, "Unused " + what + " <code>#ref</code> #loc", ProblemHighlightType.LIKE_UNUSED_SYMBOL);
          }
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
    return "Unused parameter inspection";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }
}
