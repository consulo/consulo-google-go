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

import consulo.codeEditor.Editor;
import consulo.codeEditor.util.EditorModificationUtil;
import consulo.language.editor.completion.AutoCompletionPolicy;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionProvider;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.PrioritizedLookupElement;
import consulo.language.editor.template.Template;
import consulo.language.editor.template.TemplateManager;
import consulo.language.editor.template.TemplateSettings;
import consulo.language.util.ProcessingContext;
import consulo.util.lang.ObjectUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GoKeywordCompletionProvider implements CompletionProvider {
  public static final InsertHandler<LookupElement> EMPTY_INSERT_HANDLER = (context, element) -> {

  };

  private final int myPriority;
  @Nullable
  private final InsertHandler<LookupElement> myInsertHandler;
  @Nullable private final AutoCompletionPolicy myCompletionPolicy;
  @Nonnull
  private final String[] myKeywords;

  public GoKeywordCompletionProvider(int priority, String... keywords) {
    this(priority, null, null, keywords);
  }

  public GoKeywordCompletionProvider(int priority, @Nullable AutoCompletionPolicy completionPolicy, @Nonnull String... keywords) {
    this(priority, null, completionPolicy, keywords);
  }

  public GoKeywordCompletionProvider(int priority, @Nullable InsertHandler<LookupElement> insertHandler, @Nonnull String... keywords) {
    this(priority, insertHandler, null, keywords);
  }

  private GoKeywordCompletionProvider(int priority,
                                      @Nullable InsertHandler<LookupElement> insertHandler,
                                      @Nullable AutoCompletionPolicy completionPolicy,
                                      @Nonnull String... keywords) {
    myPriority = priority;
    myInsertHandler = insertHandler;
    myCompletionPolicy = completionPolicy;
    myKeywords = keywords;
  }

  @Override
  public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result) {
    for (String keyword : myKeywords) {
      result.addElement(createKeywordLookupElement(keyword));
    }
  }

  @Nonnull
  private LookupElement createKeywordLookupElement(@Nonnull String keyword) {
    InsertHandler<LookupElement> insertHandler = ObjectUtil.chooseNotNull(myInsertHandler,
                                                                                 createTemplateBasedInsertHandler("go_lang_" + keyword));
    LookupElement result = createKeywordLookupElement(keyword, myPriority, insertHandler);
    return myCompletionPolicy != null ? myCompletionPolicy.applyPolicy(result) : result;
  }

  public static LookupElement createKeywordLookupElement(@Nonnull String keyword,
                                                         int priority,
                                                         @Nullable InsertHandler<LookupElement> insertHandler) {
    LookupElementBuilder builder = LookupElementBuilder.create(keyword).withBoldness(true).withInsertHandler(insertHandler);
    return PrioritizedLookupElement.withPriority(builder, priority);
  }

  @Nullable
  public static InsertHandler<LookupElement> createTemplateBasedInsertHandler(@Nonnull String templateId) {
    return (context, item) -> {
      Template template = TemplateSettings.getInstance().getTemplateById(templateId);
      Editor editor = context.getEditor();
      if (template != null) {
        editor.getDocument().deleteString(context.getStartOffset(), context.getTailOffset());
        TemplateManager.getInstance(context.getProject()).startTemplate(editor, template);
      }
      else {
        int currentOffset = editor.getCaretModel().getOffset();
        CharSequence documentText = editor.getDocument().getImmutableCharSequence();
        if (documentText.length() <= currentOffset || documentText.charAt(currentOffset) != ' ') {
          EditorModificationUtil.insertStringAtCaret(editor, " ");
        }
        else {
          EditorModificationUtil.moveCaretRelatively(editor, 1);
        }
      }
    };
  }
}
