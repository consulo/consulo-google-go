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

package com.goide.editor.surround;

import consulo.codeEditor.Editor;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;


public class GoWithIfExpressionSurrounder extends GoBoolExpressionSurrounderBase {
  @Override
  public String getTemplateDescription() {
    return "if expression { }";
  }

  @Nullable
  @Override
  public TextRange surroundElements(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiElement[] elements)
    throws IncorrectOperationException {
    return surroundExpressionWithIfElse(elements, false);
  }
}
