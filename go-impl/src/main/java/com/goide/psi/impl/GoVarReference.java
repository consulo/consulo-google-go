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

import com.goide.psi.GoBlock;
import com.goide.psi.GoFieldDefinition;
import com.goide.psi.GoStatement;
import com.goide.psi.GoVarDefinition;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.PsiTreeUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GoVarReference extends GoCachedReference<GoVarDefinition> {
  private final GoBlock myPotentialStopBlock;

  public GoVarReference(@Nonnull GoVarDefinition element) {
    super(element);
    myPotentialStopBlock = PsiTreeUtil.getParentOfType(element, GoBlock.class);
  }

  @Nullable
  @Override
  public PsiElement resolveInner() {
    GoVarProcessor p = new GoVarProcessor(myElement, false) {
      @Override
      protected boolean crossOff(@Nonnull PsiElement e) {
        return e instanceof GoFieldDefinition || super.crossOff(e);
      }
    };
    processResolveVariants(p);
    return p.getResult();
  }

  @Override
  public boolean processResolveVariants(@Nonnull GoScopeProcessor processor) {
    GoVarProcessor p = processor instanceof GoVarProcessor
                       ? (GoVarProcessor)processor
                       : new GoVarProcessor(myElement, processor.isCompletion()) {
                         @Override
                         public boolean execute(@Nonnull PsiElement e, @Nonnull ResolveState state) {
                           return super.execute(e, state) && processor.execute(e, state);
                         }
                       };

    if (myPotentialStopBlock != null) {
      myPotentialStopBlock.processDeclarations(p, ResolveState.initial(), PsiTreeUtil.getParentOfType(myElement, GoStatement.class), myElement);
      return true;
    }
    return false;
  }
}
