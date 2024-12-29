package com.goide.template;

import com.goide.psi.GoBlock;
import com.goide.psi.GoLeftHandExprList;
import com.goide.psi.GoSimpleStatement;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import jakarta.inject.Inject;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GoBlockLiveTemplateContextType extends GoLiveTemplateContextType {
  @Inject
  GoBlockLiveTemplateContextType() {
    super("GO_BLOCK", LocalizeValue.localizeTODO("Block"), GoEverywhereContextType.class);
  }

  @Override
  protected boolean isInContext(@Nonnull PsiElement element) {
    return (element instanceof GoLeftHandExprList || element instanceof GoSimpleStatement) &&
        PsiTreeUtil.getParentOfType(element, GoBlock.class) != null;
  }
}
