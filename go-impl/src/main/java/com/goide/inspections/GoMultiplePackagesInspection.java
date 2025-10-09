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
import com.goide.psi.GoFile;
import com.goide.psi.GoPackageClause;
import com.goide.quickfix.GoMultiplePackagesQuickFix;
import com.goide.sdk.GoPackageUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.scratch.ScratchUtil;
import consulo.language.psi.PsiDirectory;

import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

@ExtensionImpl
public class GoMultiplePackagesInspection extends GoGeneralInspectionBase {
  @Override
  protected void checkFile(@Nonnull GoFile file, @Nonnull ProblemsHolder problemsHolder) {
    if (ScratchUtil.isScratch(file.getVirtualFile())) return;
    GoPackageClause packageClause = file.getPackage();
    if (packageClause != null) {
      String packageName = file.getPackageName();
      if (packageName == null || packageName.equals(GoConstants.DOCUMENTATION)) return;
      PsiDirectory dir = file.getContainingDirectory();
      Collection<String> packages = GoPackageUtil.getAllPackagesInDirectory(dir, null, true);
      packages.remove(GoConstants.DOCUMENTATION);
      if (packages.size() > 1) {
        Collection<LocalQuickFix> fixes = new ArrayList<>();
        if (problemsHolder.isOnTheFly()) {
          fixes.add(new GoMultiplePackagesQuickFix(packageClause, packageName, packages, true));
        }
        else {
          for (String name : packages) {
            fixes.add(new GoMultiplePackagesQuickFix(packageClause, name, packages, false));
          }
        }
        problemsHolder.registerProblem(packageClause, "Multiple packages in directory", fixes.toArray(new LocalQuickFix[fixes.size()]));
      }
    }
  }

  @Nonnull
  @Override
  public LocalizeValue getDisplayName() {
    return LocalizeValue.localizeTODO("Multiple packages in directory declaration");
  }
}
