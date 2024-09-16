package com.goide.template;

import com.goide.GoTypes;
import com.goide.psi.GoBlock;
import com.goide.psi.GoExpression;
import com.goide.psi.GoLeftHandExprList;
import com.goide.psi.GoStatement;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.localize.LocalizeValue;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoStatementLiveTemplateContextType extends GoLiveTemplateContextType {
  @Inject
  GoStatementLiveTemplateContextType() {
    super("GO_STATEMENT", LocalizeValue.localizeTODO("Statement"), GoEverywhereContextType.class);
  }

  public static boolean onStatementBeginning(@Nonnull PsiElement psiElement) {
    PsiElement prevLeaf = prevVisibleLeafOrNewLine(psiElement);
    if (prevLeaf == null) {
      return false;
    }
    if (prevLeaf instanceof PsiWhiteSpace) {
      return true;
    }
    IElementType type = prevLeaf.getNode().getElementType();
    return type == GoTypes.SEMICOLON || type == GoTypes.LBRACE || type == GoTypes.RBRACE || type == GoTypes.COLON;
  }

  @Override
  protected boolean isInContext(@Nonnull PsiElement element) {
    if (element instanceof PsiComment) {
      return false;
    }

    PsiElement parent = element.getParent();
    if (parent instanceof PsiErrorElement || parent instanceof GoExpression) {
      parent = parent.getParent();
    }
    return (parent instanceof GoStatement || parent instanceof GoLeftHandExprList || parent instanceof GoBlock)
           && onStatementBeginning(element);
  }

  @Override
  protected boolean acceptLeaf() {
    return true;
  }
}
