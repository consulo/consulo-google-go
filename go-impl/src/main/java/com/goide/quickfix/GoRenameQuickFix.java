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

package com.goide.quickfix;

import com.goide.psi.GoNamedElement;
import consulo.application.ApplicationManager;
import consulo.dataContext.DataContext;
import consulo.dataContext.DataManager;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.editor.refactoring.action.RefactoringActionHandler;
import consulo.language.editor.refactoring.action.RefactoringActionHandlerFactory;
import consulo.language.editor.refactoring.rename.RenameHandler;
import consulo.language.editor.refactoring.rename.RenameHandlerRegistry;
import consulo.language.psi.ElementDescriptionUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.usage.UsageViewTypeLocation;
import consulo.util.concurrent.AsyncResult;

import jakarta.annotation.Nonnull;
import java.util.function.Consumer;

public class GoRenameQuickFix extends LocalQuickFixOnPsiElement {
  private final LocalizeValue myText;

  public GoRenameQuickFix(@Nonnull GoNamedElement element) {
    super(element);
    myText = LocalizeValue.localizeTODO("Rename " + ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE));
  }

  @Override
  public void invoke(@Nonnull Project project,
                     @Nonnull PsiFile file,
                     @Nonnull PsiElement startElement,
                     @Nonnull PsiElement endElement) {
    if (!FileModificationService.getInstance().preparePsiElementsForWrite(startElement)) return;

    Runnable runnable = () -> {
      AsyncResult<DataContext> dataContextContainer = DataManager.getInstance().getDataContextFromFocus();
      dataContextContainer.doWhenDone(new Consumer<DataContext>() {
        @Override
        public void accept(DataContext dataContext) {
          RenameHandler renameHandler = RenameHandlerRegistry.getInstance().getRenameHandler(dataContext);
          if (renameHandler != null) {
            renameHandler.invoke(project, new PsiElement[]{startElement}, dataContext);
          }
          else {
            RefactoringActionHandler renameRefactoringHandler = RefactoringActionHandlerFactory.getInstance().createRenameHandler();
            renameRefactoringHandler.invoke(project, new PsiElement[]{startElement}, dataContext);
          }
        }
      });
    };
    ApplicationManager.getApplication().invokeLater(runnable, project.getDisposed());
  }

  @Nonnull
  @Override
  public LocalizeValue getText() {
    return myText;
  }
}
