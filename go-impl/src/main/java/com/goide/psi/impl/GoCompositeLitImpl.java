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
import com.intellij.psi.PsiElementVisitor;
import com.goide.psi.GoPsiTreeUtil;

import javax.annotation.*;

import com.goide.psi.*;

public class GoCompositeLitImpl extends GoExpressionImpl implements GoCompositeLit {

  public GoCompositeLitImpl(ASTNode node) {
    super(node);
  }

  public void accept(@Nonnull GoVisitor visitor) {
    visitor.visitCompositeLit(this);
  }

  public void accept(@Nonnull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GoLiteralValue getLiteralValue() {
    return GoPsiTreeUtil.getChildOfType(this, GoLiteralValue.class);
  }

  @Override
  @Nullable
  public GoType getType() {
    return GoPsiTreeUtil.getChildOfType(this, GoType.class);
  }

  @Override
  @Nullable
  public GoTypeReferenceExpression getTypeReferenceExpression() {
    return GoPsiTreeUtil.getChildOfType(this, GoTypeReferenceExpression.class);
  }

}