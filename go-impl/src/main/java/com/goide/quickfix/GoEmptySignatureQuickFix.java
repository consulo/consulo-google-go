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

import com.goide.psi.GoFunctionDeclaration;
import com.goide.psi.GoSignature;
import com.goide.psi.impl.GoElementFactory;
import consulo.codeEditor.Editor;
import consulo.language.editor.inspection.LocalQuickFixAndIntentionActionOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.lang.ObjectUtil;
import org.jetbrains.annotations.Nls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GoEmptySignatureQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
  public static final LocalizeValue QUICK_FIX_NAME = LocalizeValue.localizeTODO("Fix signature");

  public GoEmptySignatureQuickFix(@Nonnull GoFunctionDeclaration functionDeclaration) {
    super(functionDeclaration);
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
    GoFunctionDeclaration function = ObjectUtil.tryCast(startElement, GoFunctionDeclaration.class);
    GoSignature signature = function != null ? function.getSignature() : null;
    if (signature == null) return;
    signature.replace(GoElementFactory.createFunctionSignatureFromText(project, ""));
  }
}