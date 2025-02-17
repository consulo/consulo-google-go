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

package com.goide.psi.impl;

import com.goide.psi.*;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.PsiTreeUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GoCompositeElementImpl extends ASTWrapperPsiElement implements GoCompositeElement {
  public GoCompositeElementImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return getNode().getElementType().toString();
  }

  @Override
  public boolean processDeclarations(@Nonnull PsiScopeProcessor processor,
                                     @Nonnull ResolveState state,
                                     @Nullable PsiElement lastParent,
                                     @Nonnull PsiElement place) {
    return processDeclarationsDefault(this, processor, state, lastParent, place);
  }

  public static boolean processDeclarationsDefault(@Nonnull GoCompositeElement o,
                                                   @Nonnull PsiScopeProcessor processor,
                                                   @Nonnull ResolveState state,
                                                   @Nullable PsiElement lastParent,
                                                   @Nonnull PsiElement place) {
    if (o instanceof GoLeftHandExprList || o instanceof GoExpression) return true;

    if (!o.shouldGoDeeper()) return processor.execute(o, state);
    if (!processor.execute(o, state)) return false;
    if ((
          o instanceof GoSwitchStatement ||
          o instanceof GoIfStatement ||
          o instanceof GoForStatement ||
          o instanceof GoCommClause ||
          o instanceof GoBlock ||
          o instanceof GoCaseClause
        ) 
        && processor instanceof GoScopeProcessorBase) {
      if (!PsiTreeUtil.isAncestor(o, ((GoScopeProcessorBase)processor).myOrigin, false)) return true;
    }

    return o instanceof GoBlock
           ? processBlock((GoBlock)o, processor, state, lastParent, place)
           : ResolveUtil.processChildren(o, processor, state, lastParent, place);
  }

  private static boolean processBlock(@Nonnull GoBlock o,
                                      @Nonnull PsiScopeProcessor processor,
                                      @Nonnull ResolveState state,
                                      @Nullable PsiElement lastParent, @Nonnull PsiElement place) {
    return ResolveUtil.processChildrenFromTop(o, processor, state, lastParent, place) && processParameters(o, processor);
  }

  private static boolean processParameters(@Nonnull GoBlock b, @Nonnull PsiScopeProcessor processor) {
    if (processor instanceof GoScopeProcessorBase && b.getParent() instanceof GoSignatureOwner) {
      return GoPsiImplUtil.processSignatureOwner((GoSignatureOwner)b.getParent(), (GoScopeProcessorBase)processor);
    }
    return true;
  }

  @Override
  public boolean shouldGoDeeper() {
    return true;
  }
}
