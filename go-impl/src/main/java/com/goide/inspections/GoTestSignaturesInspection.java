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

import com.goide.GoConstants;
import com.goide.psi.*;
import com.goide.psi.impl.GoElementFactory;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.runconfig.testing.GoTestFinder;
import com.goide.runconfig.testing.GoTestFunctionType;
import com.goide.runconfig.testing.frameworks.gotest.GotestGenerateAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.ObjectUtil;

import jakarta.annotation.Nonnull;
import java.util.List;

@ExtensionImpl
public class GoTestSignaturesInspection extends GoGeneralInspectionBase {
  @Override
  protected void checkFile(@Nonnull GoFile file, @Nonnull ProblemsHolder problemsHolder) {
    if (!GoTestFinder.isTestFile(file)) return;
    for (GoFunctionDeclaration function : file.getFunctions()) {
      GoTestFunctionType type = GoTestFunctionType.fromName(function.getName());
      if (type == null) continue;
      GoSignature signature = function.getSignature();
      if (signature == null) continue;
      List<GoParameterDeclaration> params = signature.getParameters().getParameterDeclarationList();
      if (type == GoTestFunctionType.EXAMPLE) {
        if (!params.isEmpty() || signature.getResult() != null) {
          problemsHolder.registerProblem(function.getIdentifier(), "Wrong example signature", new GoTestSignaturesQuickFix(type));
        }
      }
      else {
        GoParameterDeclaration param = ContainerUtil.getFirstItem(params);
        GoImportSpec testingImportSpec = file.getImportedPackagesMap().get(GoConstants.TESTING_PATH);
        String testingAlias = GoPsiImplUtil.getImportQualifierToUseInFile(testingImportSpec, GoConstants.TESTING_PATH);
        if (GoConstants.TESTING_PATH.equals(file.getImportPath(false))) {
          testingAlias = "";
        }
        if (signature.getResult() != null ||
            testingAlias == null ||
            params.size() != 1 ||
            param == null ||
            !param.getType().textMatches(type.getQualifiedParamType(testingAlias))) {
          problemsHolder.registerProblem(function.getIdentifier(), "Wrong test signature", new GoTestSignaturesQuickFix(type));
        }
      }
    }
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Wrong test declaration";
  }

  private static class GoTestSignaturesQuickFix extends LocalQuickFixBase {
    private final GoTestFunctionType myType;

    public GoTestSignaturesQuickFix(GoTestFunctionType type) {
      super("Fix signature");
      myType = type;
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getStartElement();
      if (element == null) return;
      GoFunctionDeclaration function = ObjectUtil.tryCast(element.getParent(), GoFunctionDeclaration.class);
      GoSignature signature = function != null ? function.getSignature() : null;
      if (signature == null) return;
      GoFile file = ObjectUtil.tryCast(signature.getContainingFile(), GoFile.class);
      if (file == null) return;

      String testingQualifier = GotestGenerateAction.importTestingPackageIfNeeded(file);
      signature.replace(GoElementFactory.createFunctionSignatureFromText(project, myType.getSignature(testingQualifier)));
    }
  }
}
