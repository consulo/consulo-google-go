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

import com.goide.GoLanguage;
import com.goide.psi.GoNamedElement;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionConfidence;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.util.lang.ThreeState;

import javax.annotation.Nonnull;

@ExtensionImpl(order = "last")
public class GoCompletionConfidence extends CompletionConfidence {
  @Nonnull
  @Override
  public ThreeState shouldSkipAutopopup(@Nonnull PsiElement context, @Nonnull PsiFile psiFile, int offset) {
    return context instanceof GoNamedElement && ((GoNamedElement)context).isBlank() ? ThreeState.YES : ThreeState.UNSURE;
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}
