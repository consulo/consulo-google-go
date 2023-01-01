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

import com.goide.psi.GoVarDefinition;
import com.goide.psi.GoVarSpec;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GoDeleteVarDefinitionQuickFix extends LocalQuickFixBase {
  public GoDeleteVarDefinitionQuickFix(@Nullable String variableName) {
    super("Delete variable " + (variableName != null ? "'" + variableName + "'" : ""), "Delete variable");
  }

  @Override
  public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getPsiElement();
    if (element.isValid() && element instanceof GoVarDefinition) {
      PsiElement parent = element.getParent();
      if (parent instanceof GoVarSpec) {
        ((GoVarSpec)parent).deleteDefinition((GoVarDefinition)element);
      }
    }
  }
}
