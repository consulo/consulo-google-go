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

package com.goide.codeInsight;

import com.goide.GoLanguage;
import com.goide.psi.GoTopLevelDeclaration;
import com.goide.psi.GoType;
import com.goide.psi.GoTypeOwner;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.ExpressionTypeProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.SyntaxTraverser;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.Conditions;

import javax.annotation.Nonnull;
import java.util.List;

@ExtensionImpl
public class GoExpressionTypeProvider extends ExpressionTypeProvider<GoTypeOwner> {
  @Nonnull
  @Override
  public String getInformationHint(@Nonnull GoTypeOwner element) {
    GoType type = element.getGoType(null);
    return StringUtil.escapeXml(StringUtil.notNullize(type != null ? type.getText() : null, "<unknown>"));
  }

  @Nonnull
  @Override
  public String getErrorHint() {
    return "Selection doesn't contain a Go expression";
  }

  @Nonnull
  @Override
  public List<GoTypeOwner> getExpressionsAt(@Nonnull PsiElement at) {
    if (at instanceof PsiWhiteSpace && at.textMatches("\n")) {
      at = PsiTreeUtil.prevLeaf(at);
    }
    return SyntaxTraverser.psiApi().parents(at).takeWhile(Conditions.notInstanceOf(GoTopLevelDeclaration.class))
      .filter(GoTypeOwner.class).toList();
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}