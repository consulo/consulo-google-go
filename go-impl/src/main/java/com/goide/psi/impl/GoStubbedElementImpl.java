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

import com.goide.psi.GoCompositeElement;
import com.goide.psi.GoFile;
import com.goide.stubs.TextHolder;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.stub.StubBasedPsiElementBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class GoStubbedElementImpl<T extends StubBase<?>> extends StubBasedPsiElementBase<T> implements GoCompositeElement {
  public GoStubbedElementImpl(@Nonnull T stub, @Nonnull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public GoStubbedElementImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return getElementType().toString();
  }

  @Nullable
  @Override
  public String getText() {
    T stub = getStub();
    if (stub instanceof TextHolder) {
      String text = ((TextHolder)stub).getText();
      if (text != null) return text;
    }
    return super.getText();
  }

  @Override
  public PsiElement getParent() {
    return getParentByStub();
  }

  @Override
  public boolean processDeclarations(@Nonnull PsiScopeProcessor processor,
                                     @Nonnull ResolveState state,
                                     PsiElement lastParent,
                                     @Nonnull PsiElement place) {
    return GoCompositeElementImpl.processDeclarationsDefault(this, processor, state, lastParent, place);
  }

  @Nonnull
  @Override
  public GoFile getContainingFile() {
    return (GoFile)super.getContainingFile();
  }

  @Override
  public boolean shouldGoDeeper() {
    return true;
  }
}
