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

package com.goide.completion;

import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.language.editor.AutoPopupController;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;

import jakarta.annotation.Nonnull;

public class SingleCharInsertHandler implements InsertHandler<LookupElement> {
  private final char myChar;

  public SingleCharInsertHandler(char aChar) {
    myChar = aChar;
  }

  @Override
  public void handleInsert(@Nonnull InsertionContext context, LookupElement item) {
    Editor editor = context.getEditor();
    int tailOffset = context.getTailOffset();
    Document document = editor.getDocument();
    context.commitDocument();
    boolean staysAtChar = document.getTextLength() > tailOffset &&
                          document.getCharsSequence().charAt(tailOffset) == myChar;

    context.setAddCompletionChar(false);
    if (!staysAtChar) {
      document.insertString(tailOffset, String.valueOf(myChar));
    }
    editor.getCaretModel().moveToOffset(tailOffset + 1);

    AutoPopupController.getInstance(context.getProject()).scheduleAutoPopup(editor);
  }
}