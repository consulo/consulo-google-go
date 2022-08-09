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
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ExtensionImpl
public class GoDuplicateReturnArgumentInspection extends GoDuplicateArgumentInspection {
  @Override
  public void check(@Nullable GoSignature o, @Nonnull ProblemsHolder holder) {
    if (o == null) return;
    Set<String> names = getParamNames(o);
    GoResult result = o.getResult();
    if (result == null) return;
    GoParameters parameters = result.getParameters();
    if (parameters == null) return;

    checkParameters(holder, parameters, names);
  }

  @Nonnull
  private static Set<String> getParamNames(@Nonnull GoSignature o) {
    List<GoParameterDeclaration> params = o.getParameters().getParameterDeclarationList();
    Set<String> names = new LinkedHashSet<>();
    for (GoParameterDeclaration fp : params) {
      for (GoParamDefinition parameter : fp.getParamDefinitionList()) {
        if (parameter.isBlank()) continue;
        ContainerUtil.addIfNotNull(names, parameter.getName());
      }
    }
    return names;
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Duplicate return argument";
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Redeclared symbols";
  }
}
