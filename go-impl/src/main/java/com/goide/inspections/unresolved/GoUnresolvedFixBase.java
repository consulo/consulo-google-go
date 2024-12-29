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

package com.goide.inspections.unresolved;

import com.goide.psi.GoReferenceExpressionBase;
import com.goide.refactor.GoRefactoringUtil;
import consulo.codeEditor.Editor;
import consulo.language.editor.inspection.LocalQuickFixAndIntentionActionOnPsiElement;
import consulo.language.editor.template.Template;
import consulo.language.editor.template.TemplateManager;
import consulo.language.editor.template.TemplateSettings;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.AttachmentFactoryUtil;
import consulo.project.Project;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Map;

public abstract class GoUnresolvedFixBase extends LocalQuickFixAndIntentionActionOnPsiElement {
  @Nonnull
  private final String myName;
  @Nonnull
  private final String myWhat;
  @Nonnull
  private final String myTemplateId;

  public GoUnresolvedFixBase(@Nonnull PsiElement element, @Nonnull String name, @Nonnull String what, @Nonnull String templateId) {
    super(element);
    myName = name;
    myWhat = what;
    myTemplateId = templateId;
  }

  @Nonnull
  @Override
  public String getText() {
    return "Create " + myWhat + " '" + myName + "'";
  }

  @Nonnull
  @Override
  public String getFamilyName() {
    return "Create " + myWhat;
  }

  @Override
  public void invoke(@Nonnull Project project,
                     @Nonnull PsiFile file,
                     @Nullable Editor editor,
                     @Nonnull PsiElement startElement,
                     @Nonnull PsiElement endElement) {
    if (editor == null) {
      LOG.error("Cannot run quick fix without editor: " + getClass().getSimpleName(),
          AttachmentFactoryUtil.createAttachment(file.getVirtualFile()));
      return;
    }
    PsiElement reference = PsiTreeUtil.getNonStrictParentOfType(startElement, GoReferenceExpressionBase.class);
    PsiElement anchor = reference != null ? findAnchor(reference) : null;
    if (anchor == null) {
      LOG.error("Cannot find anchor for " + myWhat + " (GoUnresolvedFixBase), offset: " + editor.getCaretModel().getOffset(),
          AttachmentFactoryUtil.createAttachment(file.getVirtualFile()));
      return;
    }
    Template template = TemplateSettings.getInstance().getTemplateById(myTemplateId);
    if (template == null) {
      LOG.error("Cannot find anchor for " + myWhat + " (GoUnresolvedFixBase), offset: " + editor.getCaretModel().getOffset(),
          AttachmentFactoryUtil.createAttachment(file.getVirtualFile()));
      return;
    }
    int start = anchor.getTextRange().getStartOffset();
    editor.getCaretModel().moveToOffset(start);
    template.setToReformat(true);
    TemplateManager.getInstance(project).startTemplate(editor, template, true, Map.of("NAME", myName), null);
  }

  @Nullable
  protected PsiElement findAnchor(@Nonnull PsiElement reference) {
    PsiFile file = reference.getContainingFile();
    return GoRefactoringUtil.findAnchor(GoRefactoringUtil.getOccurrences(reference, file), file);
  }
}

