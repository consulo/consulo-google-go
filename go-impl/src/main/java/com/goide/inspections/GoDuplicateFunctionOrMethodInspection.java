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
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.sdk.GoPackageUtil;
import com.goide.stubs.index.GoFunctionIndex;
import com.goide.stubs.index.GoIdFilter;
import com.goide.stubs.index.GoMethodIndex;
import com.goide.stubs.types.GoMethodDeclarationStubElementType;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressManager;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.IdFilter;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.util.lang.Comparing;

import jakarta.annotation.Nonnull;

import static com.goide.GoConstants.INIT;
import static com.goide.GoConstants.MAIN;

@ExtensionImpl
public class GoDuplicateFunctionOrMethodInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitMethodDeclaration(@Nonnull GoMethodDeclaration method) {
        if (method.isBlank()) return;

        String methodName = method.getName();
        if (methodName == null) return;

        String typeText = GoMethodDeclarationStubElementType.calcTypeText(method);
        if (typeText == null) return;

        GoFile file = method.getContainingFile();
        GlobalSearchScope scope = GoPackageUtil.packageScope(file);
        IdFilter idFilter = GoIdFilter.getFilesFilter(scope);
        Module module = ModuleUtilCore.findModuleForPsiElement(file);
        String key = file.getPackageName() + "." + typeText;
        GoMethodIndex.process(key, file.getProject(), scope, idFilter, declaration -> {
          ProgressManager.checkCanceled();
          if (!method.isEquivalentTo(declaration)) {
            if (Comparing.equal(declaration.getName(), methodName)
                && GoPsiImplUtil.allowed(declaration.getContainingFile(), file, module)) {
              PsiElement identifier = method.getNameIdentifier();
              holder.registerProblem(identifier == null ? method : identifier, "Duplicate method name");
              return false;
            }
          }
          return true;
        });
      }

      @Override
      public void visitFunctionDeclaration(@Nonnull GoFunctionDeclaration func) {
        if (func.isBlank()) return;

        String funcName = func.getName();
        if (funcName == null) return;
        if (INIT.equals(funcName) && zeroArity(func)) return;

        GoFile file = func.getContainingFile();
        boolean isMainFunction = MAIN.equals(funcName) && MAIN.equals(file.getPackageName()) && zeroArity(func);
        Module module = ModuleUtilCore.findModuleForPsiElement(file);
        GlobalSearchScope scope = GoPackageUtil.packageScope(file);
        IdFilter idFilter = GoIdFilter.getFilesFilter(scope);
        GoFunctionIndex.process(funcName, file.getProject(), scope, idFilter, declaration -> {
          ProgressManager.checkCanceled();
          if (!func.isEquivalentTo(declaration) && GoPsiImplUtil.allowed(declaration.getContainingFile(), file, module)) {
            if (!isMainFunction || Comparing.equal(declaration.getContainingFile(), file)) {
              PsiElement identifier = func.getNameIdentifier();
              holder.registerProblem(identifier == null ? func : identifier, "Duplicate function name");
              return false;
            }
          }
          return true;
        });
      }
    };
  }

  private static boolean zeroArity(@Nonnull GoFunctionDeclaration o) {
    GoSignature signature = o.getSignature();
    return signature == null || signature.getParameters().getParameterDeclarationList().isEmpty();
  }

  @Nonnull
  @Override
  public LocalizeValue getGroupDisplayName() {
    return LocalizeValue.localizeTODO("Redeclared symbols");
  }

  @Nonnull
  @Override
  public LocalizeValue getDisplayName() {
    return LocalizeValue.localizeTODO("Duplicate functions and methods inspection");
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}
