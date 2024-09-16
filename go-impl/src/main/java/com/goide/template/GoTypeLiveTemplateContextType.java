package com.goide.template;

import com.goide.psi.GoType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoTypeLiveTemplateContextType extends GoLiveTemplateContextType {
  @Inject
  GoTypeLiveTemplateContextType() {
    super("GO_TYPE", LocalizeValue.localizeTODO("Type"), GoEverywhereContextType.class);
  }

  @Override
  protected boolean isInContext(@Nonnull PsiElement element) {
    return element instanceof GoType;
  }
}
