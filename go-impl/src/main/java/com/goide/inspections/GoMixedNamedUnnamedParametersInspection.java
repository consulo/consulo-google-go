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
import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressManager;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;

import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoMixedNamedUnnamedParametersInspection extends GoGeneralInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitMethodDeclaration(@Nonnull GoMethodDeclaration o) {
        super.visitMethodDeclaration(o);
        visitDeclaration(holder, o.getSignature(), "Method");
      }

      @Override
      public void visitFunctionDeclaration(@Nonnull GoFunctionDeclaration o) {
        super.visitFunctionDeclaration(o);
        visitDeclaration(holder, o.getSignature(), "Function");
      }

      @Override
      public void visitFunctionLit(@Nonnull GoFunctionLit o) {
        super.visitFunctionLit(o);
        visitDeclaration(holder, o.getSignature(), "Closure");
      }
    };
  }

  private static void visitDeclaration(@Nonnull ProblemsHolder holder, @Nullable GoSignature signature, @Nonnull String ownerType) {
    if (signature == null) return;
    GoParameters parameters = signature.getParameters();
    visitParameterList(holder, parameters, ownerType, "parameters");

    GoResult result = signature.getResult();
    parameters = result != null ? result.getParameters() : null;
    visitParameterList(holder, parameters, ownerType, "return parameters");
  }

  private static void visitParameterList(@Nonnull ProblemsHolder holder, @Nullable GoParameters parameters,
                                         @Nonnull String ownerType, @Nonnull String what) {

    if (parameters == null || parameters.getParameterDeclarationList().isEmpty()) return;
    boolean hasNamed = false;
    boolean hasUnnamed = false;
    for (GoParameterDeclaration parameterDeclaration : parameters.getParameterDeclarationList()) {
      ProgressManager.checkCanceled();
      if (parameterDeclaration.getParamDefinitionList().isEmpty()) {
        hasUnnamed = true;
      }
      else {
        hasNamed = true;
      }

      if (hasNamed && hasUnnamed) {
        holder.registerProblem(parameters, ownerType + " has both named and unnamed " + what + " <code>#ref</code> #loc",
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        return;
      }
    }
  }

  @Nonnull
  @Override
  public LocalizeValue getDisplayName() {
    return LocalizeValue.localizeTODO("Mixed named and unnamed parameters");
  }
}
