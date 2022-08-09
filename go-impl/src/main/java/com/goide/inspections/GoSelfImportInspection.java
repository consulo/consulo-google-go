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

import com.goide.psi.GoFile;
import com.goide.psi.GoImportSpec;
import com.goide.quickfix.GoDeleteImportQuickFix;
import com.goide.runconfig.testing.GoTestFinder;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiDirectory;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoSelfImportInspection extends GoGeneralInspectionBase {
  @Override
  protected void checkFile(@Nonnull GoFile file, @Nonnull ProblemsHolder problemsHolder) {
    if (GoTestFinder.isTestFileWithTestPackage(file)) return;
    PsiDirectory directory = file.getContainingDirectory();
    if (directory != null) {
      for (GoImportSpec importSpec : file.getImports()) {
        PsiDirectory resolve = importSpec.getImportString().resolve();
        if (directory.equals(resolve)) {
          problemsHolder.registerProblem(importSpec, "Self import is not allowed", new GoDeleteImportQuickFix());
        }
      }
    }
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Self import";
  }
}
