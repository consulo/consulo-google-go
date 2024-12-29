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

package com.goide.completion;

import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorEx;
import consulo.codeEditor.action.EditorActionHandler;
import consulo.codeEditor.action.EditorActionManager;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.template.Template;
import consulo.language.editor.template.TemplateManager;
import consulo.project.Project;
import consulo.ui.ex.action.IdeActions;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
public class BracesInsertHandler implements InsertHandler<LookupElement> {
  public static final BracesInsertHandler ONE_LINER = new BracesInsertHandler(true);
  public static final BracesInsertHandler INSTANCE = new BracesInsertHandler(false);
  
  private final boolean myOneLine;

  private BracesInsertHandler(boolean oneLine) {
    myOneLine = oneLine;
  }

  @Override
  public void handleInsert(@Nonnull InsertionContext context, LookupElement item) {
    Editor editor = context.getEditor();
    CharSequence documentText = context.getDocument().getImmutableCharSequence();
    int offset = skipWhiteSpaces(editor.getCaretModel().getOffset(), documentText);
    if (documentText.charAt(offset) != '{') {
      Project project = context.getProject();
      Template template = TemplateManager.getInstance(project).createTemplate("braces", "go", myOneLine ? "{$END$}" : " {\n$END$\n}");
      template.setToReformat(true);
      TemplateManager.getInstance(project).startTemplate(editor, template);
    }
    else {
      editor.getCaretModel().moveToOffset(offset);
      ApplicationManager.getApplication().runWriteAction(() -> {
        EditorActionHandler enterAction = EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_START_NEW_LINE);
        enterAction.execute(editor, editor.getCaretModel().getCurrentCaret(), ((EditorEx)editor).getDataContext());
      });
    }
  }

  private static int skipWhiteSpaces(int offset, @Nonnull CharSequence documentText) {
    while (offset < documentText.length() && StringUtil.isWhiteSpace(documentText.charAt(offset))) {
      offset += 1;
    }
    return Math.min(documentText.length() - 1, offset);
  }
}