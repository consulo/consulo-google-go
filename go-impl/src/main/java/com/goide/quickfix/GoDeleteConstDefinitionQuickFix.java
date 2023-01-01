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

package com.goide.quickfix;

import com.goide.psi.GoConstDefinition;
import com.goide.psi.GoConstSpec;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GoDeleteConstDefinitionQuickFix extends LocalQuickFixBase {
  public GoDeleteConstDefinitionQuickFix(@Nullable String constName) {
    super("Delete const " + (constName != null ? "'" + constName + "'" : ""), "Delete constant");
  }

  @Override
  public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getPsiElement();
    if (element.isValid() && element instanceof GoConstDefinition) {
      PsiElement parent = element.getParent();
      if (parent instanceof GoConstSpec) {
        ((GoConstSpec)parent).deleteDefinition((GoConstDefinition)element);
      }
    }
  }
}
