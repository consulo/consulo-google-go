package com.goide.template;

import com.goide.psi.GoExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import jakarta.inject.Inject;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GoExpressionLiveTemplateContextType extends GoLiveTemplateContextType {
  @Inject
  GoExpressionLiveTemplateContextType() {
    super("GO_EXPRESSION", LocalizeValue.localizeTODO("Expression"), GoEverywhereContextType.class);
  }

  @Override
  protected boolean isInContext(@Nonnull PsiElement element) {
    return element instanceof GoExpression;
  }
}
