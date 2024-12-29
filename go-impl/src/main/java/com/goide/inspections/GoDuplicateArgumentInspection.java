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

package com.goide.inspections;

import com.goide.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

@ExtensionImpl
public class GoDuplicateArgumentInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitCompositeElement(@Nonnull GoCompositeElement o) {
        if (o instanceof GoSignatureOwner) {
          check(((GoSignatureOwner)o).getSignature(), holder);
        }
      }
    };
  }

  protected void check(@Nullable GoSignature o, @Nonnull ProblemsHolder holder) {
    if (o != null) {
      checkParameters(holder, o.getParameters(), new LinkedHashSet<>());
    }
  }

  protected static void checkParameters(@Nonnull ProblemsHolder holder,
                                        @Nonnull GoParameters parameters,
                                        @Nonnull Set<String> parameterNames) {
    for (GoParameterDeclaration fp : parameters.getParameterDeclarationList()) {
      for (GoParamDefinition parameter : fp.getParamDefinitionList()) {
        if (parameter.isBlank()) continue;
        String name = parameter.getName();
        if (name != null && parameterNames.contains(name)) {
          holder.registerProblem(parameter, "Duplicate argument <code>#ref</code> #loc", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
        else {
          parameterNames.add(name);
        }
      }
    }
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Redeclared symbols";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Duplicate argument";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}
