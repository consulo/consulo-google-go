/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.refactor;

import com.goide.psi.GoImportSpec;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.find.FindManager;
import consulo.language.editor.refactoring.RefactoringBundle;
import consulo.language.editor.refactoring.rename.RenamePsiElementProcessor;
import consulo.language.editor.refactoring.util.CommonRefactoringUtil;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoRenameImportSpecProcessor extends RenamePsiElementProcessor {
  @Nullable
  @Override
  public PsiElement substituteElementToRename(PsiElement element, @Nullable Editor editor) {
    if (FindManager.getInstance(element.getProject()).canFindUsages(element)) {
      return element;
    }
    
    String message = RefactoringBundle.message("error.cannot.be.renamed");
    CommonRefactoringUtil.showErrorHint(element.getProject(), editor, message, RefactoringBundle.message("rename.title"), null);
    return null;
  }

  @Override
  public boolean canProcessElement(@Nonnull PsiElement element) {
    return element instanceof GoImportSpec;
  }
}
