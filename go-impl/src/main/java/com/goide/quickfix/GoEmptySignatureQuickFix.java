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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.Nls;
import com.goide.psi.GoFunctionDeclaration;
import com.goide.psi.GoSignature;
import com.goide.psi.impl.GoElementFactory;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;

public class GoEmptySignatureQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
  public static final String QUICK_FIX_NAME = "Fix signature";

  public GoEmptySignatureQuickFix(@Nonnull GoFunctionDeclaration functionDeclaration) {
    super(functionDeclaration);
  }

  @Nonnull
  @Override
  public String getText() {
    return QUICK_FIX_NAME;
  }

  @Nls
  @Nonnull
  @Override
  public String getFamilyName() {
    return QUICK_FIX_NAME;
  }

  @Override
  public void invoke(@Nonnull Project project,
                     @Nonnull PsiFile file,
                     @Nullable Editor editor,
                     @Nonnull PsiElement startElement,
                     @Nonnull PsiElement endElement) {
    GoFunctionDeclaration function = ObjectUtils.tryCast(startElement, GoFunctionDeclaration.class);
    GoSignature signature = function != null ? function.getSignature() : null;
    if (signature == null) return;
    signature.replace(GoElementFactory.createFunctionSignatureFromText(project, ""));
  }
}