package com.goide.template;

import com.goide.psi.GoStringLiteral;
import com.goide.psi.GoTag;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import jakarta.inject.Inject;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GoTagLiteralLiveTemplateContextType extends GoLiveTemplateContextType {
  @Inject
  GoTagLiteralLiveTemplateContextType() {
    super("GO_TAG_LITERAL", LocalizeValue.localizeTODO("Tag literal"), GoEverywhereContextType.class);
  }

  @Override
  protected boolean isInContext(@Nonnull PsiElement element) {
    return element instanceof GoStringLiteral && element.getParent() instanceof GoTag;
  }
}
