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

import com.goide.psi.*;
import com.goide.util.GoStringLiteralEscaper;

public class GoStringLiteralImpl extends GoExpressionImpl implements GoStringLiteral {

  public GoStringLiteralImpl(ASTNode node) {
    super(node);
  }

  public void accept(@Nonnull GoVisitor visitor) {
    visitor.visitStringLiteral(this);
  }

  public void accept(@Nonnull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) accept((GoVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getRawString() {
    return findChildByType(RAW_STRING);
  }

  @Override
  @Nullable
  public PsiElement getString() {
    return findChildByType(STRING);
  }

  public boolean isValidHost() {
    return GoPsiImplUtil.isValidHost(this);
  }

  @Nonnull
  public GoStringLiteralImpl updateText(String text) {
    return GoPsiImplUtil.updateText(this, text);
  }

  @Nonnull
  public GoStringLiteralEscaper createLiteralTextEscaper() {
    return GoPsiImplUtil.createLiteralTextEscaper(this);
  }

  @Nonnull
  public String getDecodedText() {
    return GoPsiImplUtil.getDecodedText(this);
  }

}