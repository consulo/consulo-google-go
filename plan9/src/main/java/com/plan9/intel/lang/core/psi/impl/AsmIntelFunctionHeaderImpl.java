// This is a generated file. Not intended for manual editing.
package com.plan9.intel.lang.core.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;

import javax.annotation.*;

import com.plan9.intel.lang.core.psi.*;

public class AsmIntelFunctionHeaderImpl extends AsmIntelElementImpl implements AsmIntelFunctionHeader {

  public AsmIntelFunctionHeaderImpl(ASTNode node) {
    super(node);
  }

  public void accept(@Nonnull AsmIntelVisitor visitor) {
    visitor.visitFunctionHeader(this);
  }

  public void accept(@Nonnull PsiElementVisitor visitor) {
    if (visitor instanceof AsmIntelVisitor) accept((AsmIntelVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public AsmIntelFrameSize getFrameSize() {
    return findChildByClass(AsmIntelFrameSize.class);
  }

  @Override
  @Nonnull
  public AsmIntelFunctionFlags getFunctionFlags() {
    return findNotNullChildByClass(AsmIntelFunctionFlags.class);
  }

}
