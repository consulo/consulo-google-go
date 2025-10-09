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

import com.goide.psi.GoStatement;
import com.goide.psi.impl.GoElementFactory;
import consulo.codeEditor.Editor;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalQuickFixAndIntentionActionOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GoReplaceWithReturnStatementQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
  public static final LocalizeValue QUICK_FIX_NAME = LocalizeValue.localizeTODO("Replace with 'return'");

  public GoReplaceWithReturnStatementQuickFix(@Nullable PsiElement element) {
    super(element);
  }

  @Nonnull
  @Override
  public LocalizeValue getText() {
    return QUICK_FIX_NAME;
  }

  @Override
  public void invoke(@Nonnull Project project,
                     @Nonnull PsiFile file,
                     @Nullable Editor editor,
                     @Nonnull PsiElement startElement,
                     @Nonnull PsiElement endElement) {
    WriteCommandAction.runWriteCommandAction(project, () -> {
      if (startElement instanceof GoStatement) {
        startElement.replace(GoElementFactory.createReturnStatement(project));
      }
    });
  }
}