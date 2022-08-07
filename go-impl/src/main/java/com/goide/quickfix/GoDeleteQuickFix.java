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

import consulo.language.ast.IElementType;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.util.lang.ObjectUtil;

import javax.annotation.Nonnull;

public class GoDeleteQuickFix extends LocalQuickFixBase {
  private final Class<? extends PsiElement> myClazz;
  private final IElementType myElementType;

  public GoDeleteQuickFix(@Nonnull String name, @Nonnull Class<? extends PsiElement> clazz) {
    super(name);
    myClazz = clazz;
    myElementType = null;
  }
  
  public GoDeleteQuickFix(@Nonnull String name, @Nonnull IElementType elementType) {
    super(name);
    myClazz = PsiElement.class;
    myElementType = elementType;
  }

  @Override
  public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
    WriteCommandAction.runWriteCommandAction(project, () -> {
      PsiElement element = ObjectUtil.tryCast(descriptor.getStartElement(), myClazz);
      if (element != null && (myElementType == null || element.getNode().getElementType() == myElementType)) {
        element.delete();
      }
    });
  }
}