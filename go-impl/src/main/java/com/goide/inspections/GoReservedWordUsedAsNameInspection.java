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

import javax.annotation.Nonnull;

import com.goide.psi.*;
import com.goide.psi.impl.GoTypeReference;
import com.goide.quickfix.GoRenameQuickFix;
import com.goide.sdk.GoSdkUtil;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.ElementDescriptionUtil;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageViewTypeLocation;

public class GoReservedWordUsedAsNameInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session) {
    GoFile builtinFile = GoSdkUtil.findBuiltinFile(session.getFile());
    if (builtinFile == null) return DUMMY_VISITOR;

    return new GoVisitor() {
      @Override
      public void visitTypeSpec(@Nonnull GoTypeSpec o) {
        super.visitTypeSpec(o);
        check(o, builtinFile, holder);
      }

      @Override
      public void visitConstDefinition(@Nonnull GoConstDefinition o) {
        super.visitConstDefinition(o);
        check(o, builtinFile, holder);
      }

      @Override
      public void visitFunctionOrMethodDeclaration(@Nonnull GoFunctionOrMethodDeclaration o) {
        super.visitFunctionOrMethodDeclaration(o);
        check(o, builtinFile, holder);
      }

      @Override
      public void visitVarDefinition(@Nonnull GoVarDefinition o) {
        super.visitVarDefinition(o);
        check(o, builtinFile, holder);
      }
    };
  }

  private static void check(@Nonnull GoNamedElement element, @Nonnull GoFile builtinFile, @Nonnull ProblemsHolder holder) {
    String name = element.getName();
    if (name == null || GoTypeReference.DOC_ONLY_TYPES.contains(name)) return;

    for (GoTypeSpec builtinTypeDeclaration : builtinFile.getTypes()) {
      if (name.equals(builtinTypeDeclaration.getName())) {
        registerProblem(holder, element, builtinTypeDeclaration);
        return;
      }
    }

    ProgressManager.checkCanceled();

    for (GoFunctionDeclaration builtinFunctionsDeclaration : builtinFile.getFunctions()) {
      if (name.equals(builtinFunctionsDeclaration.getName())) {
        registerProblem(holder, element, builtinFunctionsDeclaration);
        return;
      }
    }
  }

  private static void registerProblem(@Nonnull ProblemsHolder holder,
                                      @Nonnull GoNamedElement element,
                                      @Nonnull GoNamedElement builtinElement) {
    PsiElement identifier = element.getIdentifier();
    if (identifier == null) return;

    String elementDescription = ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE);
    String builtinElementDescription = ElementDescriptionUtil.getElementDescription(builtinElement, UsageViewTypeLocation.INSTANCE);
    String message = StringUtil.capitalize(elementDescription) + " <code>#ref</code> collides with builtin " + builtinElementDescription;
    holder.registerProblem(identifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new GoRenameQuickFix(element));
  }
}