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

package com.goide.psi.impl;

import com.goide.psi.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.PsiTreeUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;

public class GoLabelReference extends GoCachedReference<GoLabelRef> {
  private final GoScopeProcessorBase myProcessor = new GoScopeProcessorBase(myElement) {
    @Override
    protected boolean crossOff(@Nonnull PsiElement e) {
      return !(e instanceof GoLabelDefinition) || ((GoLabelDefinition)e).isBlank();
    }
  };

  public GoLabelReference(@Nonnull GoLabelRef element) {
    super(element);
  }

  @Nonnull
  private Collection<GoLabelDefinition> getLabelDefinitions() {
    GoFunctionLit functionLit = PsiTreeUtil.getParentOfType(myElement, GoFunctionLit.class);
    PsiElement blockToSearch = functionLit != null ? functionLit.getBlock() : PsiTreeUtil.getTopmostParentOfType(myElement, GoBlock.class); 
    return PsiTreeUtil.findChildrenOfType(blockToSearch, GoLabelDefinition.class);
  }

  @Nullable
  @Override
  protected PsiElement resolveInner() {
    return !processResolveVariants(myProcessor) ? myProcessor.getResult() : null;
  }

  @Override
  public boolean processResolveVariants(@Nonnull GoScopeProcessor processor) {
    GoBreakStatement breakStatement = PsiTreeUtil.getParentOfType(myElement, GoBreakStatement.class);
    if (breakStatement != null) {
      return processDefinitionsForBreakReference(breakStatement, processor);
    }
    return processAllDefinitions(processor);
  }

  private boolean processAllDefinitions(@Nonnull GoScopeProcessor processor) {
    Collection<GoLabelDefinition> defs = getLabelDefinitions();
    for (GoLabelDefinition def : defs) {
      if (!processor.execute(def, ResolveState.initial())) {
        return false;
      }
    }
    return true;
  }

  private static boolean processDefinitionsForBreakReference(@Nonnull GoBreakStatement breakStatement,
                                                             @Nonnull GoScopeProcessor processor) {
    PsiElement breakStatementOwner = GoPsiImplUtil.getBreakStatementOwner(breakStatement);
    while (breakStatementOwner != null) {
      PsiElement parent = breakStatementOwner.getParent();
      if (parent instanceof GoLabeledStatement) {
        if (!processor.execute(((GoLabeledStatement)parent).getLabelDefinition(), ResolveState.initial())) {
          return false;
        }
      }
      breakStatementOwner = GoPsiImplUtil.getBreakStatementOwner(breakStatementOwner);
    }
    return true;
  }
}
