// This is a generated file. Not intended for manual editing.
package com.plan9.intel.lang.core.psi.impl;

import java.util.List;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;

import com.plan9.intel.lang.core.psi.*;

public class AsmIntelFrameSizeImpl extends AsmIntelElementImpl implements AsmIntelFrameSize {

  public AsmIntelFrameSizeImpl(ASTNode node) {
    super(node);
  }

  public void accept(@Nonnull AsmIntelVisitor visitor) {
    visitor.visitFrameSize(this);
  }

  public void accept(@Nonnull PsiElementVisitor visitor) {
    if (visitor instanceof AsmIntelVisitor) accept((AsmIntelVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nonnull
  public List<AsmIntelLiteral> getLiteralList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AsmIntelLiteral.class);
  }

}
