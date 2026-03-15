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
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;

import org.jspecify.annotations.Nullable;

public class GoDeleteRangeQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
  private static final Logger LOG = Logger.getInstance(GoDeleteRangeQuickFix.class);
  private final LocalizeValue myName;
  
  public GoDeleteRangeQuickFix(PsiElement startElement, PsiElement endElement, LocalizeValue name) {
    super(startElement, endElement);
    if (!startElement.getParent().equals(endElement.getParent())) {
      LOG.error("Cannot delete range of elements with different parents");
    }
    myName = name;
  }

  @Override
  public LocalizeValue getText() {
    return myName;
  }

  @Override
  public void invoke(Project project,
                     PsiFile file,
                     @Nullable Editor editor,
                     PsiElement start,
                     PsiElement end) {
    if (start.isValid() && end.isValid()) {
      PsiElement parent = start.getParent();
      if (parent != null && parent.equals(end.getParent())) {
        parent.getNode().removeRange(start.getNode(), end.getNode().getTreeNext());
      }
    }
  }
}