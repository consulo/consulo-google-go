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

import consulo.codeEditor.Editor;
import consulo.language.editor.inspection.LocalQuickFixAndIntentionActionOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.logging.Logger;
import consulo.project.Project;
import org.jetbrains.annotations.Nls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GoDeleteRangeQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
  private static final Logger LOG = Logger.getInstance(GoDeleteRangeQuickFix.class);
  private final String myName;
  
  public GoDeleteRangeQuickFix(@Nonnull PsiElement startElement, @Nonnull PsiElement endElement, @Nonnull String name) {
    super(startElement, endElement);
    if (!startElement.getParent().equals(endElement.getParent())) {
      LOG.error("Cannot delete range of elements with different parents");
    }
    myName = name;
  }

  @Nonnull
  @Override
  public String getText() {
    return myName;
  }

  @Nls
  @Nonnull
  @Override
  public String getFamilyName() {
    return "Delete elements";
  }

  @Override
  public void invoke(@Nonnull Project project,
                     @Nonnull PsiFile file,
                     @Nullable Editor editor,
                     @Nonnull PsiElement start,
                     @Nonnull PsiElement end) {
    if (start.isValid() && end.isValid()) {
      PsiElement parent = start.getParent();
      if (parent != null && parent.equals(end.getParent())) {
        parent.getNode().removeRange(start.getNode(), end.getNode().getTreeNext());
      }
    }
  }
}