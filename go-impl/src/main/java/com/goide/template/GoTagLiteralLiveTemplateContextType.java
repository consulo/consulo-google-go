package com.goide.template;

import com.goide.psi.GoStringLiteral;
import com.goide.psi.GoTag;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoTagLiteralLiveTemplateContextType extends GoLiveTemplateContextType {
  @Inject
  GoTagLiteralLiveTemplateContextType() {
    super("GO_TAG_LITERAL", "Tag literal", GoEverywhereContextType.class);
  }

  @Override
  protected boolean isInContext(@Nonnull PsiElement element) {
    return element instanceof GoStringLiteral && element.getParent() instanceof GoTag;
  }
}
