// This is a generated file. Not intended for manual editing.
package com.plan9.intel.lang.core.psi.impl;

import java.util.List;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;

import com.plan9.intel.lang.core.psi.*;

public class AsmIntelFunctionBodyImpl extends AsmIntelElementImpl implements AsmIntelFunctionBody {

  public AsmIntelFunctionBodyImpl(ASTNode node) {
    super(node);
  }

  public void accept(@Nonnull AsmIntelVisitor visitor) {
    visitor.visitFunctionBody(this);
  }

  public void accept(@Nonnull PsiElementVisitor visitor) {
    if (visitor instanceof AsmIntelVisitor) accept((AsmIntelVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nonnull
  public List<AsmIntelInstructionStmt> getInstructionStmtList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AsmIntelInstructionStmt.class);
  }

}
