package com.goide.template;

import com.goide.psi.GoExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoExpressionLiveTemplateContextType extends GoLiveTemplateContextType {
  @Inject
  GoExpressionLiveTemplateContextType() {
    super("GO_EXPRESSION", "Expression", GoEverywhereContextType.class);
  }

  @Override
  protected boolean isInContext(@Nonnull PsiElement element) {
    return element instanceof GoExpression;
  }
}
