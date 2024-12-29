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

package com.goide.usages;

import com.goide.GoLanguage;
import com.goide.GoParserDefinition;
import com.goide.GoTypes;
import com.goide.lexer.GoLexer;
import com.goide.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.TokenSet;
import consulo.language.cacheBuilder.DefaultWordsScanner;
import consulo.language.cacheBuilder.WordsScanner;
import consulo.language.findUsage.FindUsagesProvider;
import consulo.language.psi.ElementDescriptionUtil;
import consulo.language.psi.PsiElement;
import consulo.usage.UsageViewLongNameLocation;
import consulo.usage.UsageViewShortNameLocation;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoFindUsagesProvider implements FindUsagesProvider {
  @Nullable
  @Override
  public WordsScanner getWordsScanner() {
    return new DefaultWordsScanner(new GoLexer(), TokenSet.create(GoTypes.IDENTIFIER),
                                   GoParserDefinition.COMMENTS, GoParserDefinition.STRING_LITERALS);
  }

  @Override
  public boolean canFindUsagesFor(@Nonnull PsiElement element) {
    if (element instanceof GoImportSpec) {
      GoImportSpec importSpec = (GoImportSpec)element;
      return importSpec.getAlias() != null && !importSpec.isDot() && !importSpec.isForSideEffects();
    }
    return element instanceof GoNamedElement;
  }

  @Nonnull
  @Override
  public String getType(@Nonnull PsiElement element) {
    if (element instanceof GoMethodDeclaration) return "method";
    if (element instanceof GoFunctionDeclaration) return "function";
    if (element instanceof GoConstDefinition || element instanceof GoConstDeclaration) return "constant";
    if (element instanceof GoVarDefinition || element instanceof GoVarDeclaration) return "variable";
    if (element instanceof GoParamDefinition) return "parameter";
    if (element instanceof GoFieldDefinition) return "field";
    if (element instanceof GoAnonymousFieldDefinition) return "anonymous field";
    if (element instanceof GoTypeSpec || element instanceof GoTypeDeclaration) return "type";
    if (element instanceof GoImportDeclaration) return "import";
    if (element instanceof GoImportSpec) return "import alias";
    if (element instanceof GoReceiver) return "receiver";
    if (element instanceof GoMethodSpec) return "method specification";
    if (element instanceof GoLabelDefinition) return "label";
    if (element instanceof GoPackageClause) return "package statement";

    // should be last
    if (element instanceof GoStatement) return "statement";
    if (element instanceof GoTopLevelDeclaration) return "declaration";
    if (element instanceof GoCommClause || element instanceof GoCaseClause) return "case";
    return "";
  }

  @Nonnull
  @Override
  public String getDescriptiveName(@Nonnull PsiElement element) {
    return ElementDescriptionUtil.getElementDescription(element, UsageViewLongNameLocation.INSTANCE);
  }

  @Nonnull
  @Override
  public String getNodeText(@Nonnull PsiElement element, boolean useFullName) {
    return ElementDescriptionUtil.getElementDescription(element, UsageViewShortNameLocation.INSTANCE);
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}