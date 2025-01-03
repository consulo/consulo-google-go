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

package com.goide.psi;

import consulo.application.progress.ProgressIndicatorProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;

import jakarta.annotation.Nonnull;

public class GoRecursiveVisitor extends GoVisitor {
  @Override
  public void visitCompositeElement(@Nonnull GoCompositeElement o) {
    super.visitCompositeElement(o);
    for (PsiElement psiElement : o.getChildren()) {
      psiElement.accept(this);
      ProgressIndicatorProvider.checkCanceled();
    }
  }

  @Override
  public void visitFile(@Nonnull PsiFile file) {
    super.visitFile(file);
    for (PsiElement psiElement : file.getChildren()) {
      psiElement.accept(this);
      ProgressIndicatorProvider.checkCanceled();
    }
  }
}