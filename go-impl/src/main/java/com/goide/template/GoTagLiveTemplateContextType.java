package com.goide.template;

import com.goide.GoTypes;
import com.goide.psi.GoFieldDeclaration;
import com.goide.psi.GoType;
import com.goide.psi.GoTypeReferenceExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ExtensionImpl
public class GoTagLiveTemplateContextType extends GoLiveTemplateContextType {
  @Inject
  GoTagLiveTemplateContextType() {
    super("GO_TAG", "Tag", GoEverywhereContextType.class);
  }

  @Override
  protected boolean isInContext(@Nonnull PsiElement element) {
    if (element.getNode().getElementType() == GoTypes.IDENTIFIER) {
      if (isInsideFieldTypeDeclaration(element)) {
        return true;
      }
      if (isInsideFieldTypeDeclaration(prevVisibleLeafOrNewLine(element))) {
        return true;
      }
    }
    return false;
  }

  private static boolean isInsideFieldTypeDeclaration(@Nullable PsiElement element) {
    if (element != null) {
      PsiElement parent = element.getParent();
      if (parent instanceof GoTypeReferenceExpression) {
        return PsiTreeUtil.skipParentsOfType(parent, GoType.class) instanceof GoFieldDeclaration;
      }
    }
    return false;
  }

  @Override
  protected boolean acceptLeaf() {
    return true;
  }
}
