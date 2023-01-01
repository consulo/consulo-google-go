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

package com.goide.editor;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.document.util.TextRange;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.action.TypedHandlerDelegate;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.project.Project;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoTypedHandler extends TypedHandlerDelegate {
  @Nonnull
  @Override
  public Result charTyped(char c, @Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file) {
    if (c != 'e') return Result.CONTINUE;
    int offset = editor.getCaretModel().getOffset();
    if (offset < 4) return Result.CONTINUE;
    TextRange from = TextRange.from(offset - 4, 4);
    String text = editor.getDocument().getText(from);
    if ("case".equals(text)) {
      PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
      ApplicationManager.getApplication().runWriteAction(() -> {
        if (project.isDisposed()) return;
        if (file == null) return;
        CodeStyleManager.getInstance(project).adjustLineIndent(file, from);
      });
    }

    return Result.CONTINUE;
  }
}
