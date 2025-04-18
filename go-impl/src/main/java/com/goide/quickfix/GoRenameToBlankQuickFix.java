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

import com.goide.inspections.GoNoNewVariablesInspection;
import com.goide.psi.GoNamedElement;
import com.goide.psi.GoVarDefinition;
import com.goide.psi.GoVarSpec;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

public class GoRenameToBlankQuickFix extends LocalQuickFixOnPsiElement {
  public static final String NAME = "Rename to _";

  public GoRenameToBlankQuickFix(GoNamedElement o) {
    super(o);
  }

  @Nonnull
  @Override
  public String getText() {
    return NAME;
  }

  @Override
  public void invoke(@Nonnull Project project, @Nonnull PsiFile file, @Nonnull PsiElement startElement, @Nonnull PsiElement endElement) {
    if (startElement.isValid() && startElement instanceof GoNamedElement) {
      ((GoNamedElement)startElement).setName("_");

      if (startElement instanceof GoVarDefinition) {
        PsiElement parent = startElement.getParent();
        if (parent instanceof GoVarSpec) {
          if (GoNoNewVariablesInspection.hasNonNewVariables(((GoVarSpec)parent).getVarDefinitionList())) {
            GoNoNewVariablesInspection.replaceWithAssignment(project, parent);
          }
        }
      }
    }
  }

  @Nonnull
  @Override
  public String getFamilyName() {
    return getName();
  }
}
