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

import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.psi.GoNamedElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.ResolveState;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.OrderedSet;
import consulo.util.lang.StringUtil;

import org.jspecify.annotations.Nullable;
import java.util.List;

public abstract class GoScopeProcessorBase extends GoScopeProcessor {
  private final OrderedSet<GoNamedElement> myResult = new OrderedSet<>();
  protected final PsiElement myOrigin;
  private final PsiElement myRequestedNameElement;
  protected final boolean myIsCompletion;

  public GoScopeProcessorBase(PsiElement origin) {
    this(origin, origin, false);
  }

  public GoScopeProcessorBase(PsiElement requestedNameElement, PsiElement origin, boolean completion) {
    myRequestedNameElement = requestedNameElement;
    myOrigin = origin;
    myIsCompletion = completion;
  }

  @Override
  public boolean execute(PsiElement e, ResolveState state) {
    if (e instanceof GoFunctionOrMethodDeclaration) return false;
    if (!(e instanceof GoNamedElement)) return true;
    String name = ((GoNamedElement)e).getName();
    if (StringUtil.isEmpty(name) || !myIsCompletion && !myRequestedNameElement.textMatches(name)) return true;
    if (crossOff(e)) return true;
    if (e.equals(myOrigin)) return true;
    return add((GoNamedElement)e) || myIsCompletion;
  }

  protected boolean add(GoNamedElement psiElement) {
    return !myResult.add(psiElement);
  }

  @Nullable
  public GoNamedElement getResult() {
    return ContainerUtil.getFirstItem(myResult);
  }

  public List<GoNamedElement> getVariants() {
    return myResult;
  }

  protected abstract boolean crossOff(PsiElement e);
}
