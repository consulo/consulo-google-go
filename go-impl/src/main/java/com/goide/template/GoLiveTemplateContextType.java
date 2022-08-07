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

import com.goide.GoLanguage;
import com.goide.GoTypes;
import com.goide.highlighting.GoSyntaxHighlighter;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.editor.template.context.TemplateContextType;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.ObjectUtil;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract public class GoLiveTemplateContextType extends TemplateContextType {
  protected GoLiveTemplateContextType(@Nonnull @NonNls String id,
                                      @Nonnull String presentableName,
                                      @Nullable Class<? extends TemplateContextType> baseContextType) {
    super(id, presentableName, baseContextType);
  }

  @Override
  public boolean isInContext(@Nonnull PsiFile file, int offset) {
    if (PsiUtilCore.getLanguageAtOffset(file, offset).isKindOf(GoLanguage.INSTANCE)) {
      PsiElement psiElement = ObjectUtil.notNull(file.findElementAt(offset), file);
      if (!acceptLeaf()) {
        psiElement = getFirstCompositeElement(psiElement);
      }
      return psiElement != null && isInContext(psiElement);
    }

    return false;
  }

  protected boolean acceptLeaf() {
    return false;
  }

  @Nullable
  public static PsiElement prevVisibleLeafOrNewLine(PsiElement element) {
    PsiElement prevLeaf = element;
    while ((prevLeaf = PsiTreeUtil.prevLeaf(prevLeaf)) != null) {
      if (prevLeaf instanceof PsiComment || prevLeaf instanceof PsiErrorElement) {
        continue;
      }
      if (prevLeaf instanceof PsiWhiteSpace) {
        if (prevLeaf.textContains('\n')) {
          return prevLeaf;
        }
        continue;
      }
      break;
    }
    return prevLeaf;
  }

  @Nullable
  private static PsiElement getFirstCompositeElement(@Nullable PsiElement at) {
    if (at instanceof PsiComment || at instanceof LeafPsiElement && ((LeafPsiElement)at).getElementType() == GoTypes.STRING) return at;
    PsiElement result = at;
    while (result != null && (result instanceof PsiWhiteSpace || result.getNode().getChildren(null).length == 0)) {
      result = result.getParent();
    }
    return result;
  }

  protected abstract boolean isInContext(@Nonnull PsiElement element);

  @Override
  public SyntaxHighlighter createHighlighter() {
    return new GoSyntaxHighlighter();
  }

}
