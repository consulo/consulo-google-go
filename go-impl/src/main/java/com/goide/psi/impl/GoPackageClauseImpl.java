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

// This is a generated file. Not intended for manual editing.
package com.goide.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import static com.goide.GoTypes.*;

import javax.annotation.*;

import com.goide.stubs.GoPackageClauseStub;
import com.goide.psi.*;
import com.intellij.psi.stubs.IStubElementType;

public class GoPackageClauseImpl extends GoStubbedElementImpl<GoPackageClauseStub> implements GoPackageClause {

  public GoPackageClauseImpl(GoPackageClauseStub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public GoPackageClauseImpl(ASTNode node) {
    super(node);
  }

  public void accept(@Nonnull GoVisitor visitor) {
    visitor.visitPackageClause(this);
  }

  public void accept(@Nonnull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getIdentifier() {
    return findChildByType(IDENTIFIER);
  }

  @Override
  @Nonnull
  public PsiElement getPackage() {
    return notNullChild(findChildByType(PACKAGE));
  }

  @Nullable
  public String getName() {
    return GoPsiImplUtil.getName(this);
  }

}