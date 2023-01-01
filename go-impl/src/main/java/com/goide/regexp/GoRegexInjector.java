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

package com.goide.regexp;

import com.goide.psi.*;
import com.goide.psi.impl.GoPsiImplUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.inject.InjectedLanguagePlaces;
import consulo.language.inject.LanguageInjector;
import consulo.language.psi.ElementManipulators;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.ObjectUtil;

import javax.annotation.Nonnull;
import java.util.Set;

@ExtensionImpl
public class GoRegexInjector implements LanguageInjector {
  private static final Set<String> REGEXP_FUNCTION_NAMES = Set.of(
    "Compile", "CompilePOSIX", "MustCompile", "MustCompilePOSIX", "Match", "MatchReader", "MatchString"
  );

  @Override
  public void injectLanguages(@Nonnull PsiLanguageInjectionHost host, @Nonnull InjectedLanguagePlaces injectionPlacesRegistrar) {
    if (!(host instanceof GoStringLiteral)) return;
    PsiElement topMostExpression = host;
    PsiElement argumentList = host.getParent();
    while (argumentList instanceof GoParenthesesExpr) {
      topMostExpression = argumentList;
      argumentList = argumentList.getParent();
    }
    if (!(argumentList instanceof GoArgumentList)) return;
    // must be first argument
    if (ContainerUtil.getFirstItem(((GoArgumentList)argumentList).getExpressionList()) != topMostExpression) return;

    GoCallExpr callExpression = ObjectUtil.tryCast(argumentList.getParent(), GoCallExpr.class);
    if (callExpression == null) return;
    GoReferenceExpression referenceExpression = ObjectUtil.tryCast(callExpression.getExpression(), GoReferenceExpression.class);
    if (referenceExpression != null && REGEXP_FUNCTION_NAMES.contains(referenceExpression.getIdentifier().getText())) {
      GoSignatureOwner resolvedCall = GoPsiImplUtil.resolveCall(callExpression);
      if (resolvedCall instanceof GoFunctionDeclaration) {
        PsiFile file = resolvedCall.getContainingFile();
        if (file instanceof GoFile && "regexp".equals(((GoFile)file).getImportPath(false))) {
          injectionPlacesRegistrar.addPlace(GoRegExpLanguage.INSTANCE, ElementManipulators.getValueTextRange(host), null, null);
        }
      }
    }
  }
}
