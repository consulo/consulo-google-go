// This is a generated file. Not intended for manual editing.
package com.plan9.intel.lang.core.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;

import javax.annotation.*;

import com.plan9.intel.lang.core.psi.*;

public class AsmIntelStatementImpl extends AsmIntelElementImpl implements AsmIntelStatement {

  public AsmIntelStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@Nonnull AsmIntelVisitor visitor) {
    visitor.visitStatement(this);
  }

  public void accept(@Nonnull PsiElementVisitor visitor) {
    if (visitor instanceof AsmIntelVisitor) accept((AsmIntelVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public AsmIntelFunction getFunction() {
    return findChildByClass(AsmIntelFunction.class);
  }

  @Override
  @Nullable
  public AsmIntelPreprocessorDirective getPreprocessorDirective() {
    return findChildByClass(AsmIntelPreprocessorDirective.class);
  }

}
