// This is a generated file. Not intended for manual editing.
package com.plan9.intel.lang.core.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;

import javax.annotation.Nonnull;

import com.plan9.intel.lang.core.psi.*;

public class AsmIntelFunctionImpl extends AsmIntelElementImpl implements AsmIntelFunction {

  public AsmIntelFunctionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@Nonnull AsmIntelVisitor visitor) {
    visitor.visitFunction(this);
  }

  public void accept(@Nonnull PsiElementVisitor visitor) {
    if (visitor instanceof AsmIntelVisitor) accept((AsmIntelVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nonnull
  public AsmIntelFunctionBody getFunctionBody() {
    return findNotNullChildByClass(AsmIntelFunctionBody.class);
  }

  @Override
  @Nonnull
  public AsmIntelFunctionHeader getFunctionHeader() {
    return findNotNullChildByClass(AsmIntelFunctionHeader.class);
  }

}
