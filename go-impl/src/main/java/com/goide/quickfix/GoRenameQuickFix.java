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

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.goide.psi.GoNamedElement;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.psi.ElementDescriptionUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringActionHandlerFactory;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.refactoring.rename.RenameHandlerRegistry;
import com.intellij.usageView.UsageViewTypeLocation;

public class GoRenameQuickFix extends LocalQuickFixOnPsiElement {
  private final String myText;

  public GoRenameQuickFix(@Nonnull GoNamedElement element) {
    super(element);
    myText = "Rename " + ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE);
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
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      runnable.run();
    }
    else {
      ApplicationManager.getApplication().invokeLater(runnable, project.getDisposed());
    }
  }

  @Override
  @Nonnull
  public String getFamilyName() {
    return getName();
  }

  @Nonnull
  @Override
  public String getText() {
    return myText;
  }
}