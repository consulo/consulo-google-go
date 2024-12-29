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

package com.goide.template;

import com.goide.psi.GoFieldDeclaration;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.template.Expression;
import consulo.language.editor.template.ExpressionContext;
import consulo.language.editor.template.Result;
import consulo.language.editor.template.TextResult;
import consulo.language.editor.template.context.TemplateContextType;
import consulo.language.editor.template.macro.Macro;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ExtensionImpl
public class GoFieldNameMacro extends Macro {
  @Override
  public String getName() {
    return "fieldName";
  }

  @Override
  public String getPresentableName() {
    return "fieldName()";
  }

  @Nullable
  @Override
  public Result calculateResult(@Nonnull Expression[] params, ExpressionContext context) {
    String name = ContainerUtil.getFirstItem(fieldNames(context));
    return StringUtil.isNotEmpty(name) ? new TextResult(name) : null;
  }

  @Nullable
  @Override
  public LookupElement[] calculateLookupItems(@Nonnull Expression[] params, ExpressionContext context) {
    return ContainerUtil.map2Array(fieldNames(context), LookupElement.class, LookupElementBuilder::create);
  }

  @Override
  public boolean isAcceptableInContext(TemplateContextType context) {
    return context instanceof GoTagLiveTemplateContextType || context instanceof GoTagLiteralLiveTemplateContextType;
  }

  private static Set<String> fieldNames(ExpressionContext context) {
    PsiElement psiElement = context != null ? context.getPsiElementAtStartOffset() : null;
    GoFieldDeclaration fieldDeclaration = PsiTreeUtil.getNonStrictParentOfType(psiElement, GoFieldDeclaration.class);
    if (fieldDeclaration == null) {
      return Collections.emptySet();
    }
    return fieldDeclaration.getFieldDefinitionList().stream().map(PsiNamedElement::getName).collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
