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

package com.goide.codeInsight.imports;

import com.goide.util.GoExecutor;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.intention.HighPriorityAction;
import consulo.language.psi.PsiElement;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.project.Project;


public class GoGetPackageFix extends LocalQuickFixBase implements HighPriorityAction {
  private final String myPackage;

  public GoGetPackageFix(String packageName) {
    super(LocalizeValue.of("go get -t " + packageName + "/..."));
    myPackage = packageName;
  }

  @Override
  public void applyFix(Project project, ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getPsiElement();
    if (element != null) {
      GoExecutor.goGet(project, ModuleUtilCore.findModuleForPsiElement(element.getContainingFile()), myPackage, true);
    }
  }
}
